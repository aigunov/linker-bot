package backend.academy.scrapper.service;

import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    public void storeUpdate(Set<Chat> deferredChats, Link link, UpdateInfo updateInfo) {

    }
}
