package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.ImMemoryChatRepository;
import backend.academy.scrapper.repository.ImMemoryLinkRepository;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperService {

    private final ImMemoryChatRepository chatRepository;
    private final ImMemoryLinkRepository linkRepository;
    private final LinkToApiRequestConverter converter;
    private final Mapper mapper;

    public String registerChat(Long id, RegisterChatRequest request) {
        var chatOpt = chatRepository.findByChatId(id);
        if (chatOpt.isPresent()) {
            var message = String.format("Чат с id %d с пользователем %s уже существует", request.id(), request.name());
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        var chat = mapper.chatDtoToEntity(chatOpt);
        chat = chatRepository.save(chat);
        log.info("Registered new chat: {}", chat);
        return "Вы зарегистрированы";
    }

    public String deleteChat(Long id) {
        var chat = chatRepository.findByChatId(id).orElseThrow(() -> {
            var message = String.format("Чат с id %d не существует", id);
            log.error(message);
            return new NoSuchElementException(message);
        });
        chat = chatRepository.deleteById(chat.id());
        log.info("Deleted chat: {}", chat);
        return "Чат Успешно удален";
    }

    public ListLinkResponse getAllTrackedLinks(Long chatId) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с id %d не существует", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });

        var links = linkRepository.findAllByChatId(chat.id())
            .stream()
            .map(mapper::linkToLinkResponse)
            .toList();
        if (links.isEmpty()) {
            var message = String.format("Никаких ссылок не найдено для чата id %d", chatId);
            log.error(message);
            throw new NoSuchElementException(message);
        }
        log.info("Get all tracked links: {}", links);
        return ListLinkResponse.builder()
            .linkResponses(links)
            .size(links.size())
            .build();
    }

    //TODO: Реализовать трекинг линков
    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с id %d не существует", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });
        var link = mapper.linkRequestToLink(request);
        //
        //Где-то тут должна произойти та самая LinkTrackingMagicFucking)
        //
        link = linkRepository.save(link);
        log.info("Added tracked link: {}", link);
        return mapper.linkToLinkResponse(link);
    }

    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        var chat = chatRepository.findByChatId(chatId).orElseThrow(() -> {
            var message = String.format("Чат с id %d не существует", chatId);
            log.error(message);
            return new NoSuchElementException(message);
        });
        var link = linkRepository.deleteByChatIdAndLinkUrl(chat.id(), request.uri())
            .orElseThrow(() -> {
                var message = String.format("Ссылка %s в чате с id %d для удаления", request.uri(), chatId);
                log.error(message);
                return new NoSuchElementException(message);
            });
         log.info("Removed tracked link: {}", link);
         return mapper.linkToLinkResponse(link);
    }



    @Scheduled(fixedRate = 300000)
    public void scrapper() {}


    public static <T> void throwIfPresent(Optional<T> optional, Supplier<? extends RuntimeException> exceptionSupplier)
        throws RuntimeException {
        optional.ifPresent(value -> {
            throw exceptionSupplier.get();
        });
    }
}
