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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    //TODO: В дальнейшем переработать этот метод чтобы происходила фильтрация поиска и по фильтрам
    @Transactional(readOnly=true)
    public ListLinkResponse getAllTrackedLinks(Long chatId, GetLinksRequest linksRequest) {
        chatRepository.findByTgId(chatId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", chatId));
        List<Link> links;

        if (linksRequest.tags() != null && !linksRequest.tags().isEmpty()) {
            links = (List<Link>) linkRepository.findLinksByChatIdAndTags(chatId, linksRequest.tags(), (long) linksRequest.tags().size());
        } else {
            links = (List<Link>) linkRepository.findAllByChatId(chatId);
        }
        var linksResponse = links.stream().map(Mapper::linkToLinkResponse).toList();
        log.info("Get links: {}", linksResponse);
        return ListLinkResponse.builder().linkResponses(linksResponse).build();
    }

    @Transactional
    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        chatRepository.findByTgId(chatId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", chatId));

        linkRepository.findByChatIdAndUrl(chatId, request.uri()).ifPresent(_ -> {
            var message = String.format("У пользователя с tg-id %d, уже существует ссылка %s в отслеживании", chatId, request.uri());
            log.error(message);
            throw new LinkException(message);
        });

        var tags = new HashSet<Tag>();
        if (request.tags() != null){
            for(var tagName: request.tags()){
                 var tag = tagRepository.findByChatIdAndTag(chatId, tagName).orElseGet(() -> Mapper.tagNameToTag(tagName));
                 tags.add(tag);
            }
        }

        var filters = new HashSet<Filter>();
        if (request.filters() != null){
            for(var filterName: request.filters()){
                var filter = filterRepository.findByChatIdAndFilter(chatId, filterName).orElseGet(() -> Mapper.filterNameToFilter(filterName));
                filters.add(filter);
            }
        }

        var linkToSave = Mapper.linkRequestToLink(request.uri(), chatId, tags, filters);
        log.info("Link t o save: {}", linkToSave);
        var link = linkRepository.save(linkToSave);
        return Mapper.linkToLinkResponse(link);
    }

    @Transactional
    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        chatRepository.findByTgId(chatId)
            .orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", chatId));

        Link link = linkRepository.findByChatIdAndUrl(chatId, request.uri())
            .orElseThrow(() -> new LinkException("Ссылка с uri %s не найдена для чата с id %d", request.uri(), chatId));

        log.info("User tg-id: {} will delete link with id: {} and uri: {}", chatId, link.id(), request.uri());

        linkRepository.deleteById(link.id());

        return Mapper.linkToLinkResponse(link);
    }
}
