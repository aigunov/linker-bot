package backend.academy.scrapper.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    @Transactional(readOnly=true)
    public ListLinkResponse getAllTrackedLinks(Long chatId, GetLinksRequest linksRequest) {
        return null;
    }

    @Transactional
    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        
        return null;
    }

    @Transactional
    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        return null;
    }
}
