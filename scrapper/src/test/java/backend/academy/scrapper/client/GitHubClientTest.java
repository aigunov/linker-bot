package backend.academy.scrapper.client;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

//@SpringBootTest(classes = {
//    String.class,
//    StackOverflowConfig.class,
//    GitHubConfig.class,
//    LinkToApiRequestConverter.class,
//    TestClientConfig.class,
//    GitHubClient.class
//})
@SpringBootTest
@EnableConfigurationProperties({GitHubConfig.class, StackOverflowConfig.class})
@TestPropertySource(properties = {
    "app.github.url=http://localhost:8089/repos",
    "app.github.token=test-token",
    "app.stackoverflow.key=test-key",
    "app.stackoverflow.access_token=test-access",
    "app.stackoverflow.url=http://localhost:8089/stackoverflow",
})
class GitHubClientTest {
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
        .withDatabaseName("scrapper_db")
        .withUsername("aigunov")
        .withPassword("12345");

    //    @MockitoBean
    @Autowired
    private LinkToApiRequestConverter converterApi;

    @Autowired
    private GitHubClient gitHubClient;

    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        postgresContainer.start();
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("app.scrapper.page-size", () -> 10);
        registry.add("app.scrapper.threads-count", () -> 1);
        registry.add("app.scrapper.scheduled-time", () -> 100000);
        registry.add("app.db.access-type", () -> "orm");
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void checkUpdates_ShouldReturnLatestUpdateInfoFromGitHub() {
        //Given
        String repoPath = "/repos/aigunov/java-shareit";
        String fullUrl = "https://github.com/aigunov/java-shareit";

        //Mock issues response
        String issuesJson = """
            [
                            {
                                "title": "Issue #1",
                                "created_at": "2024-04-01T10:00:00",
                                "user": { "login": "issue-author" },
                                "body": "Issue body"
                            }
                        ]
            """;

        // Mock PRs response
        String prsJson = """
                [
                    {
                        "title": "PR #1",
                        "created_at": "2024-04-02T12:00:00",
                        "user": { "login": "pr-author" },
                        "body": "Pull request body"
                    }
                ]
            """;

        //When
        stubFor(get(urlEqualTo(repoPath + "/issues?state=all"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(issuesJson)));

        stubFor(get(urlEqualTo(repoPath + "/pulls?state=all"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prsJson)));


        Optional<UpdateInfo> updateInfoOpt = gitHubClient.checkUpdates(fullUrl);
//        Mockito.when(converterApi.convertGithubUrlToApi(any()))
//            .thenReturn(fullUrl);

        //Then
        assertThat(updateInfoOpt).isPresent();
        UpdateInfo updateInfo = updateInfoOpt.get();

        assertThat(updateInfo.title()).isEqualTo("PR #1");
        assertThat(updateInfo.username()).isEqualTo("pr-author");
        assertThat(updateInfo.date()).isEqualTo(LocalDateTime.of(2024, 4, 2, 12, 0));
        assertThat(updateInfo.type()).isEqualTo("pull-request");
        assertThat(updateInfo.preview()).isEqualTo("Pull request body");
    }
}
