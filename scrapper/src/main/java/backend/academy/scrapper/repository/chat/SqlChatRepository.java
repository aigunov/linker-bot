package backend.academy.scrapper.repository.chat;


import backend.academy.scrapper.data.model.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="sql")
public class SqlChatRepository implements ChatRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Chat save(Chat chat) {
        return null;
    }

    @Override
    public void deleteById(UUID id) {

    }

    @Override
    public Optional<Chat> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<Chat> findByTgId(Long chatId) {
        return Optional.empty();
    }

    @Override
    public List<Chat> findAll() {
        return null;
    }
}
