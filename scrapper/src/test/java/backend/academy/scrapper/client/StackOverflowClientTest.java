package backend.academy.scrapper.client;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "app.stackoverflow.url=http://localhost:8089/questions"
})
class StackOverflowClientTest {

}
