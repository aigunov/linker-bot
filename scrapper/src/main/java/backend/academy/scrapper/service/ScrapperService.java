package backend.academy.scrapper.service;

import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import org.springframework.stereotype.Component;

@Component
public class ScrapperService {
    public void registerChat(Long id, RegisterChatRequest request) {

    }

    public void deleteChat(Long id) {
    }

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
