package backend.academy.scrapper.service;

import backend.academy.scrapper.model.AddLinkRequest;
import backend.academy.scrapper.model.LinkResponse;
import backend.academy.scrapper.model.ListLinkResponse;
import backend.academy.scrapper.model.RegisterChatRequest;
import backend.academy.scrapper.model.RemoveLinkRequest;
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
