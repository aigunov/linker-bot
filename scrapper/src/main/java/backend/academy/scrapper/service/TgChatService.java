package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.ChatException;
import backend.academy.scrapper.repository.chat.ChatRepository;
import dto.RegisterChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatService {
    private final ChatRepository chatRepository;

    @Transactional
    public String registerChat(Long tgId, RegisterChatRequest request) {
        chatRepository.findByTgId(tgId).ifPresent(_ -> {
            var message = String.format("Чат с id %d уже зарегистрирован", tgId);
            log.error(message);
            throw new ChatException(message);
        });

        var chat = chatRepository.save(Mapper.chatDtoToEntity(request));
        log.info("Saved chat: {}", chat);
        return "Chat successfully registered";
    }

    @Transactional
    public String deleteChat(Long chatId) {
        var chatToDelete = chatRepository.findByTgId(chatId)
            .orElseThrow(() -> new ChatException("chat %d not found", chatId));

        chatRepository.deleteById(chatToDelete.id());
        log.info("Deleted chat: {}", chatToDelete);
        return "Chat successfully deleted";
    }
}
