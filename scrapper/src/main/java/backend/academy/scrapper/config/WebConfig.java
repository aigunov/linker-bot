package backend.academy.scrapper.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Bean
    public WebClient webClient(@Value("${bot.api.url}") String botUrl) {
        return WebClient.builder()
            .baseUrl(botUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    @Qualifier("trackClient")
    public RestClient trackClient() {
        return RestClient.builder().requestFactory(clientHttpRequestFactory()).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(90000);
        return factory;
    }
}
