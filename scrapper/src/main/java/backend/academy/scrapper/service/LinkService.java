package backend.academy.scrapper.service;

import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.exception.ChatException;
import backend.academy.scrapper.exception.LinkException;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import dto.AddLinkRequest;
import dto.GetLinksRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RemoveLinkRequest;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    // TODO: В дальнейшем переработать этот метод чтобы происходила фильтрация поиска и по фильтрам
    @Transactional(readOnly = true)
    public ListLinkResponse getAllTrackedLinks(Long chatId, GetLinksRequest linksRequest) {
        chatRepository.findByTgId(chatId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", chatId));
        List<Link> links;

        if (linksRequest.tags() != null && !linksRequest.tags().isEmpty()) {
            links = (List<Link>) linkRepository.findLinksByTgIdAndTags(chatId, linksRequest.tags());
        } else {
            links = (List<Link>) linkRepository.findAllByTgId(chatId);
        }
        var linksResponse = links.stream().map(Mapper::linkToLinkResponse).toList();
        log.info("Get links: {}", linksResponse);
        return ListLinkResponse.builder().linkResponses(linksResponse).build();
    }

    // Да, я знаю что в этом методе идет запросы в циклах,
    // но хоть убейте ничего другого из-за сложной логики ничего лучше не придумал
    @Transactional
    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        var chat = chatRepository
                .findByTgId(chatId)
                .orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", chatId));

        linkRepository.findByTgIdAndUrl(chatId, request.uri()).ifPresent(x -> {
            var message = String.format(
                    "У пользователя с tg-id %d, уже существует ссылка %s в отслеживании", chatId, request.uri());
            log.error(message);
            throw new LinkException(message);
        });

        var tags = new HashSet<Tag>();
        if (request.tags() != null) {
            for (var tagName : request.tags()) {
                var tag = tagRepository.findByTgIdAndTag(chatId, tagName).orElseGet(() -> {
                    log.info("Сохранение нового тега переданного с ссылкой: {}", tagName);
                    return tagRepository.save(Mapper.tagDtoToTag(tagName, chat));
                });
                tags.add(tag);
            }
        }

        var filters = new HashSet<Filter>();
        if (request.filters() != null) {
            for (var filt : request.filters()) {
                var filterEntry = filt.split(":");
                var filterParam = filterEntry[0];
                var filterValue = filterEntry[1];
                var filter = filterRepository
                        .findByTgIdAndFilter(chatId, filterParam, filterValue)
                        .orElseGet(() -> {
                            log.info("Сохранение нового фильтра переданного с ссылкой: {}", filterEntry);
                            return filterRepository.save(Mapper.filterDtoToFilter(filterParam, filterValue, chat));
                        });
                filters.add(filter);
            }
        }

        var linkToSave = Mapper.linkRequestToLink(request.uri(), chat, tags, filters);
        log.info("Link t o save: {}", linkToSave);
        var link = linkRepository.save(linkToSave);
        return Mapper.linkToLinkResponse(link);
    }

    @Transactional
    public LinkResponse removeTrackedLink(Long tgId, RemoveLinkRequest request) {
        var chat =
                chatRepository.findByTgId(tgId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", tgId));

        Link link = linkRepository
                .findByTgIdAndUrl(tgId, request.uri())
                .orElseThrow(
                        () -> new LinkException("Ссылка с uri %s не найдена для чата с id %d", request.uri(), tgId));
        log.info("User tg-id: {} will delete link with id: {} and uri: {}", tgId, link.id(), request.uri());
        linkRepository.deleteById(link.id());

        // удаление тегов и фильтров которые "опустошились" после удаления ссылки
        var tagsToDelete = (List<Tag>) tagRepository.findAllByChatIdAndNotInTagToLinkTable(chat.id());
        if (!tagsToDelete.isEmpty()) {
            log.info(
                    "List of tags: {} -- will delete, because after delete link: {} they don't use",
                    tagsToDelete,
                    link.url());
            tagRepository.deleteAll(tagsToDelete);
        }
        var filtersToDelete = (List<Filter>) filterRepository.findAllByChatIdAndNotInLinkToFilterTable(chat.id());
        if (!filtersToDelete.isEmpty()) {
            log.info(
                    "List of filters: {} -- will delete, because after delete link: {} they don't use",
                    filtersToDelete,
                    link.url());
            filterRepository.deleteAll(filtersToDelete);
        }

        return Mapper.linkToLinkResponse(link);
    }
}
