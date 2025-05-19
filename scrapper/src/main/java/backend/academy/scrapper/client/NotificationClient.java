package backend.academy.scrapper.client;

import dto.Digest;
import dto.LinkUpdate;

public interface NotificationClient {

    void sendLinkUpdate(LinkUpdate linkUpdate);

    void sendDigest(Digest digest);
}
