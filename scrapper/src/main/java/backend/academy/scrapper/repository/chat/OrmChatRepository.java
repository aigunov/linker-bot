package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="orm")
public interface OrmChatRepository extends ChatRepository, JpaRepository<Chat, UUID> {
    @Override
    @Query("""
        SELECT c
        FROM Chat c
        WHERE c.tgId = :tgId
        """)
    Optional<Chat> findByTgId(final @Param("tgId") Long tgId);

    @Override
    @Modifying
    @Query("""
        DELETE
        FROM Chat c
        WHERE c.tgId = :tgId
        """)
    void deleteByTgId(final @Param("tgId") Long tgId);
}
