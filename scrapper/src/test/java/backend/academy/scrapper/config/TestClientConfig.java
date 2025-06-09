package backend.academy.scrapper.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                //                .baseUrl("http://localhost:8089/repos")
                .requestFactory(clientHttpRequestFactory())
                .requestInterceptor((request, body, execution) -> {
                    System.out.println(">>> " + request.getURI());
                    return execution.execute(request, body);
                })
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
