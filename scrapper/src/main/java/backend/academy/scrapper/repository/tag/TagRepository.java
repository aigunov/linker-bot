package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository {
    Tag save(Tag tag);
    void deleteById(UUID id);
    Optional<Tag> findById(UUID id);
    Iterable<Tag> findAllByChatId(Long chatId);
    Iterable<Tag> findAll();
}
