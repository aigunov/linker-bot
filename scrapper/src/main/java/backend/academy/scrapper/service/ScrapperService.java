package backend.academy.scrapper.service;

import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.InMemoryChatRepository;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.LinkUpdate;
import dto.ListLinkResponse;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Scope(proxyMode = ScopedProxyMode.NO)
@RequiredArgsConstructor
public class ScrapperService {

    private final Mapper mapper;
    private final InMemoryChatRepository chatRepository;
    private final InMemoryLinkRepository linkRepository;
    private final LinkToApiRequestConverter converter;
    private final UpdateCheckingClient stackOverflowClient;
    private final UpdateCheckingClient gitHubClient;
    private final NotificationService notificationService;


    public String registerChat(Long id, RegisterChatRequest request) {
        var chatOpt = chatRepository.findByChatId(id);
        if (chatOpt.isPresent()) {
            var message = String.format("Чат с chatId %d с пользователем %s уже существует", request.chatId(), request.name());
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        var chat = mapper.chatDtoToEntity(request);
        chat = chatRepository.save(chat);
        log.info("Registered new chat: {}", chat);
        return "Вы зарегистрированы";
    }

    public String deleteChat(Long id) {
        var chat = chatRepository.findByChatId(id).orElseThrow(() -> {
            var message = String.format("Чат с chatId %d не существует", id);
            log.error(message);
            return new NoSuchElementException(message);
        });
        chat = chatRepository.deleteById(chat.id());
        log.info("Deleted chat: {}", chat);
        return "Чат Успешно удален";
    }

    public ListLinkResponse getAllTrackedLinks(Long chatId) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с chatId %d не существует. Пользователь не зарегистрирован", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });

        var links = linkRepository.findAllByChatId(chat.id())
            .stream()
            .map(mapper::linkToLinkResponse)
            .toList();
        if (links.isEmpty()) {
            var message = String.format("Никаких ссылок не найдено для чата chatId %d", chatId);
            log.error(message);
            throw new NoSuchElementException(message);
        }
        log.info("Get all tracked links: {}", links);
        return ListLinkResponse.builder()
            .linkResponses(links)
            .size(links.size())
            .build();
    }


    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с chatId %d не существует. Пользователь не зарегистрирован", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });
        var existingLink = linkRepository.findByChatIdAndLink(chat.id(), request.uri());
        if (existingLink.isPresent()) {
            var message = String.format("Ссылка %s в чате с chatId %d уже существует", request.uri(), chatId);
            log.error(message);
            throw new NoSuchElementException(message);
        }

        log.info("Ссылка не найдена, добавляем новую: {}", request.uri());
        var link = mapper.linkRequestToLink(request, chat);
        link = linkRepository.save(link);
        log.info("Added tracked link: {}", link);

        return mapper.linkToLinkResponse(link);
    }

    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с chatId %d не существует. Пользователь не зарегистрирован", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });
        var link = linkRepository.findByChatIdAndLink(chat.id(), request.uri())
            .orElseThrow(() -> {
                var message = String.format("Ссылки в чате с chatId %d не существует", chatId);
                log.error(message);
                return new NoSuchElementException(message);
            });


        linkRepository.deleteById(link.id());
        log.info("Removed tracked link: {}", link);
        return mapper.linkToLinkResponse(link);
    }


    @Scheduled(fixedRate = 100000)
    public void scrapper() {
        log.info("Scrapper scheduled started");
        List<Link> allLinks = linkRepository.findAll();
        for (Link link : allLinks) {
            processLink(link);
        }
    }

    private void processLink(Link link) {
        LocalDateTime lastUpdate = null;
        if (converter.isGithubUrl(link.url())) {
            lastUpdate = gitHubClient.checkUpdates(link.url()).orElse(null);
        } else if (converter.isStackOverflowUrl(link.url())) {
            lastUpdate = stackOverflowClient.checkUpdates(link.url()).orElse(null);
        }

        if (lastUpdate != null) {
            if (link.lastUpdate() == null || lastUpdate.isAfter(link.lastUpdate())) {
                log.info("Link {} updated at {}", link.url(), lastUpdate);
                link.lastUpdate(lastUpdate);
                linkRepository.save(link);
                try {
                    sendNotification(link);
                } catch (BotServiceInternalErrorException e) {
                    log.error("Bot service returned INTERNAL_SERVER_ERROR for link: {}", link.url(), e);
                } catch (BotServiceException e) {
                    log.error("Failed to send notification for link: {}", link.url(), e);
                }
            }
        } else {
            log.warn("Failed to retrieve updates for {}", link.url());
        }
    }

    private void sendNotification(Link link) {
        chatRepository.findById(link.chatId()).ifPresent(chat -> {
            LinkUpdate linkUpdate = new LinkUpdate(
                link.id(),
                link.url(),
                "Link Updated",
                List.of(chat.chatId())
            );
            notificationService.sendLinkUpdate(linkUpdate);
        });
    }
}
