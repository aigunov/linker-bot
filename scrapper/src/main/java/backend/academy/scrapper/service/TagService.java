package backend.academy.scrapper.service;

import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.exception.ChatException;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import dto.GetTagsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {
    private final ChatRepository chatRepository;
    private final TagRepository tagRepository;

    public GetTagsResponse getUserTags(final Long tgId) {
        chatRepository.findByTgId(tgId).orElseThrow(() -> new ChatException("Чат с tg-id %d не найден", tgId));

        var tags = ((List<Tag>) tagRepository.findAllByTgId(tgId))
                .stream().map(Tag::tag).toList();
        log.info("Found tg-id chat {} tags {}", tgId, tags);
        return GetTagsResponse.builder().tags(tags).build();
    }
}
