package backend.academy.bot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public RestClient scrapperRestClient(@Value("${scrapper.api.url}") String scrapperApiUrl) {
        return RestClient.builder()
            .baseUrl(scrapperApiUrl)
            .build();
    }
}
