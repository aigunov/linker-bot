package backend.academy.scrapper.client;

import dto.LinkUpdate;

public interface NotificationClient {

    void sendLinkUpdate(LinkUpdate linkUpdate);
}
