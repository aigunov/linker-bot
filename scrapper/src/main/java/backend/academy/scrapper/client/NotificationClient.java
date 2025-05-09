package backend.academy.scrapper.client;

import dto.LinkUpdate;
import java.util.List;

public interface NotificationClient {

    void sendLinkUpdate(LinkUpdate linkUpdate);

    void sendDigest(List<LinkUpdate> linkUpdates);
}
