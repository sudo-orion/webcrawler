package io.github.atoko.webcrawler.crawl.service;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.crawl.model.CrawlStatus;
import io.github.atoko.webcrawler.crawl.model.CrawlVisitedTotals;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class CrawlStatusService {
    private ConcurrentMap<String, CrawlStatus> crawls =
            new ConcurrentHashMap<>();

    public Mono<CrawlStatus> getCrawlStatusById(String id) {
        return Mono.just(this.crawls.get(id));
    }

    public Mono<Boolean> markUrlAsPending(Crawl crawl) {
        if (!this.crawls.containsKey(crawl.crawlId)) {
            this.crawls.put(crawl.crawlId, new CrawlStatus(crawl.crawlId));
        }

        return getCrawlStatusById(crawl.crawlId).map((crawlStatus) -> {
            crawlStatus.linksVisited.put(normalizeUrl(crawl.url), false);
            return true;
        });
    }

    public Mono<Boolean> markUrlAsVisited(Crawl crawl) {
        if (!this.crawls.containsKey(crawl.crawlId)) {
            this.crawls.put(crawl.crawlId, new CrawlStatus(crawl.crawlId));
        }

        return getCrawlStatusById(crawl.crawlId).map((crawlStatus) -> {
            crawlStatus.linksVisited.put(normalizeUrl(crawl.url), true);
            return true;
        });
    }

    public Mono<Boolean> isUrlKnown(Crawl crawl) {
        CrawlStatus status = this.crawls.get(crawl.crawlId);

        if (status != null) {
            ConcurrentMap<String, Boolean> crawlLinksVisited = this.crawls.get(crawl.crawlId).linksVisited;
            if (crawlLinksVisited != null) {
                return Mono.just(crawlLinksVisited.get(normalizeUrl(crawl.url)) != null);
            } else {
                return Mono.just(false);
            }
        } else {
            return Mono.just(false);
        }
    }

    private String normalizeUrl(String url) {
        String normalizedUri = url;
        try {
            URI uri = new URI(url);
            normalizedUri =
                    String.format("%s%s", uri.getAuthority(), uri.getPath());
            if (normalizedUri.endsWith("/")) {
                normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
            }
        } catch (Exception e) {
        }

        return normalizedUri;
    }

    public Mono<CrawlVisitedTotals> getVisitedTotals(String crawlId) {
        if (this.crawls.containsKey(crawlId)) {
            CrawlStatus status = this.crawls.get(crawlId);
            return Mono.just(new CrawlVisitedTotals(
                    status.linksVisited.entrySet().stream().filter(kv -> !kv.getValue()).count(),
                    status.linksVisited.entrySet().size()
            ));
        } else {
            return Mono.empty();
        }
    }
}
