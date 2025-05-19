package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.db", name = "access-type", havingValue = "orm")
public interface OrmChatRepository extends ChatRepository, JpaRepository<Chat, UUID> {
    Optional<Chat> findByTgId(Long tgId);

    void deleteByTgId(Long tgId);

    @Override
    @Query("UPDATE Chat c SET c.digestTime = :time WHERE c.tgId = :tgId")
    void setDigestTime(Long chatId, LocalTime time);
}
