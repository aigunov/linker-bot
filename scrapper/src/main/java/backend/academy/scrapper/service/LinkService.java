package backend.academy.scrapper.service;

import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RemoveLinkRequest;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    public ListLinkResponse getAllTrackedLinks(Long chatId) {
        return null;
    }

    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        return null;
    }

    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        return null;
    }
}
