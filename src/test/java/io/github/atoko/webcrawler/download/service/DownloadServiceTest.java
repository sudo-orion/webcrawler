package io.github.atoko.webcrawler.download.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.atoko.webcrawler.download.model.Download;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class DownloadServiceTest {

    static MockWebServer mockServer;
    @InjectMocks
    private DownloadService downloadService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockServer.getPort());
    }

    @Test
    void it_fetches_url_and_parses_into_model() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString("<html><a href=abc>link1</a><a href=cde>link2</a>"
                        + "<img src=fgh>image</img></html>"))
        );

        Mono<Download> downloadMono = downloadService.fetch(String.format("http://localhost:%s", mockServer.getPort()));

        StepVerifier.create(downloadMono)
                .assertNext(download -> {
                    Assertions.assertEquals(2, download.links.stream().count());
                    Assertions.assertEquals(1, download.images.stream().count());
                })
                .verifyComplete();
    }
}
