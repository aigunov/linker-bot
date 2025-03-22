package backend.academy.bot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    @Bean
    public RestClient scrapperRestClient(@Value("${scrapper.api.url}") String scrapperApiUrl) {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .baseUrl(scrapperApiUrl)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(90000);
        return factory;
    }
}
