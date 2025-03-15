package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="jpa")
public interface OrmChatRepository extends ChatRepository, JpaRepository<Chat, UUID> {
    @Override
    @Query(value = """
        """, nativeQuery = true)
    Optional<Chat> findByTgId(Long chatId);
}
