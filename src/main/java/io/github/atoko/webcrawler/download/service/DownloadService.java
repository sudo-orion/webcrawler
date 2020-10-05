package io.github.atoko.webcrawler.download.service;

import io.github.atoko.webcrawler.download.model.Download;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class DownloadService {
    private Logger logger = Logger.getLogger(DownloadService.class.getName());
    private WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create().followRedirect(true)
            ))
            .build();

    public Mono<Download> fetch(String url) {
        logger.info(String.format("Fetching url: %s", url));
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    Document parsedHtml = Jsoup.parse(response);

                    Elements links = parsedHtml.select("a[href]");
                    Elements images = parsedHtml.select("img[src]");

                    return new Download(
                            links.stream().map(a -> a.attr("abs:href"))
                                    .collect(Collectors.toList()),
                            images.stream().map(img -> img.attr("abs:src"))
                                    .collect(Collectors.toList())
                    );
                });
    }
}
