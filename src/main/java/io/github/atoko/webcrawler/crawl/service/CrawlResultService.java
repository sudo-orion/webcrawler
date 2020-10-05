package io.github.atoko.webcrawler.crawl.service;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.crawl.model.CrawlResult;
import io.github.atoko.webcrawler.download.model.Download;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class CrawlResultService {
    private ConcurrentMap<String, CrawlResult> crawls =
            new ConcurrentHashMap<>();

    public Mono<CrawlResult> getCrawlResultById(String id) {
        CrawlResult result = this.crawls.get(id);
        if (result != null) {
            return Mono.just(result);
        } else {
            return Mono.empty();
        }
    }

    public Mono<Boolean> ingest(Crawl crawl, Download download) {
        if (!this.crawls.containsKey(crawl.crawlId)) {
            this.crawls.put(crawl.crawlId, new CrawlResult(crawl.crawlId));
        }

        return this.getCrawlResultById(crawl.crawlId)
                .doOnNext(crawlResult -> {
                    crawlResult.externalLinks.put(
                            crawl.url,
                            download.links.stream()
                                    .filter(l -> !crawl.isWithinSameDomain(l) && !l.isEmpty())
                                    .collect(Collectors.toList())
                    );

                    crawlResult.outboundLinks.put(
                            crawl.url,
                            download.links.stream()
                                    .filter(l -> crawl.isWithinSameDomain(l) && !l.isEmpty())
                                    .collect(Collectors.toList())
                    );

                    crawlResult.images.put(
                            crawl.url,
                            new ArrayList<>(download.images)
                    );
                })
                .map(x -> true);
    }
}
