package backend.academy.bot.clients;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.ListLinkResponse;
import backend.academy.bot.model.RegisterChatRequest;
import backend.academy.bot.model.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScrapperClient {

    private final WebClient scrapperWebClient;

    //TODO: Поменять возвращаемый тип
    public Mono<Void> registerChat(final RegisterChatRequest chat){
        return scrapperWebClient.post()
            .uri("/tg-chat/{id}", chat.id())
            .bodyValue(chat)
            .retrieve()
            .bodyToMono(Void.class);
    }


    public Mono<Void> deleteChat(final Long chatId){
        return scrapperWebClient.delete()
            .uri("tg-chat/{id}", chatId)
            .retrieve()
            .bodyToMono(Void.class);
    }

    public Mono<ListLinkResponse> getAllTrackedLinks(final Long chatId){
        return scrapperWebClient.get()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .retrieve()
            .bodyToMono(ListLinkResponse.class);
    }

    public Mono<ListLinkResponse> addTrackedLink(final Long chatId, AddLinkRequest request){
        return scrapperWebClient.post()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ListLinkResponse.class);
    }

    public Mono<ListLinkResponse> removeTrackedLink(Long chatId, RemoveLinkRequest request){
        return scrapperWebClient.method(HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(ListLinkResponse.class);
    }
}
