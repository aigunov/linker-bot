package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository {
    Tag save(Tag tag);

    void deleteById(UUID id);

    void deleteAll(Iterable<? extends Tag> tags);

    void deleteAll();

    Optional<Tag> findById(UUID id);

    Optional<Tag> findByTgIdAndTag(Long tgId, String tag);

    Iterable<Tag> findAllByChatIdAndNotInTagToLinkTable(UUID chatId);

    Iterable<Tag> findAllByTgId(Long chatId);

    Iterable<Tag> findAll();
}
