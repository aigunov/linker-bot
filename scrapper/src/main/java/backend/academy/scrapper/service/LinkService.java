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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
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
    public ListLinkResponse getAllTrackedLinks(Long tgId, GetLinksRequest linksRequest) {
        chatRepository.findByTgId(tgId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", tgId));
        List<Link> links;

        if (linksRequest.tags() != null && !linksRequest.tags().isEmpty()) {
            links = (List<Link>) linkRepository.findLinksByTgIdAndTags(tgId, linksRequest.tags());
        } else {
            links = (List<Link>) linkRepository.findAllByTgId(tgId);
        }
        var linksResponse = links.stream().map(Mapper::linkToLinkResponse).toList();
        log.info("Get links: {}", linksResponse);
        return ListLinkResponse.builder().linkResponses(linksResponse).build();
    }

    @Transactional
    public LinkResponse addTrackedLink(Long tgId, AddLinkRequest request) {
        var chat =
                chatRepository.findByTgId(tgId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", tgId));

        linkRepository.findByTgIdAndUrl(tgId, request.uri()).ifPresent(x -> {
            var message = String.format(
                    "У пользователя с tg-id %d, уже существует ссылка %s в отслеживании", tgId, request.uri());
            log.error(message);
            throw new LinkException(message);
        });
        Set<Tag> tags = new HashSet<>();

        if (!request.tags().isEmpty()) {
            var res = tagRepository.saveAll(request.tags().stream()
                    .map(tag -> Mapper.tagDtoToTag(tag, chat))
                    .toList());

            for (var tag : res) {
                tags.add(tag);
            }
        }

        Set<Filter> filters = new HashSet<>();

        if (!request.filters().isEmpty()) {
            var res = filterRepository.saveAll(request.filters().stream()
                    .map(filter -> filter.split(":"))
                    .map(filterValues -> {
                        var param = filterValues[0];
                        var value = filterValues[1];
                        return Mapper.filterDtoToFilter(param, value, chat);
                    })
                    .toList());

            for (var filter : res) {
                filters.add(filter);
            }
        }

        var linkToSave = Mapper.linkRequestToLink(request.uri(), chat, tags, filters);
        log.info("Link to save: {}", linkToSave);
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
