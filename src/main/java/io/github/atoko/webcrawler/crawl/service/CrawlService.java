package io.github.atoko.webcrawler.crawl.service;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.download.service.DownloadService;
import io.github.atoko.webcrawler.scheduler.service.SchedulerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Component
public class CrawlService {
    private final SchedulerService schedulerService;
    private final DownloadService downloadService;
    private final CrawlStatusService crawlStatusService;
    private final CrawlResultService crawlResultService;
    private Logger logger = Logger.getLogger(CrawlService.class.getName());


    public CrawlService(@Lazy SchedulerService schedulerService,
                        DownloadService downloadService,
                        CrawlStatusService crawlStatusService,
                        CrawlResultService crawlResultService) {
        this.schedulerService = schedulerService;
        this.downloadService = downloadService;
        this.crawlStatusService = crawlStatusService;
        this.crawlResultService = crawlResultService;
    }

    public Mono<Crawl> startCrawl(String url) {
        Crawl crawl = new Crawl(UUID.randomUUID().toString(), url);
        logger.info(
                String.format("Starting crawl %s with url: %s", crawl.crawlId,
                        crawl.url));

        return crawlStatusService.markUrlAsPending(crawl)
                .flatMap((any) -> {
                    return schedulerService.scheduleCrawl(crawl);
                });
    }

    public Mono<List<Crawl>> fetchPageAndCrawlOutboundLinks(Crawl crawl) {
        return downloadService.fetch(crawl.url)
                .flatMap(
                        (download) -> crawlStatusService.markUrlAsVisited(crawl)
                                .map(x -> download))
                .flatMap((download) -> crawlStatusService
                        .getVisitedTotals(crawl.crawlId).doOnNext(
                                statistics -> logger.info(String.format(
                                        "Crawl %s progress: %s queued %s total",
                                        crawl.crawlId,
                                        statistics.pending,
                                        statistics.visited
                                ))
                        ).map(x -> download))
                .flatMap(download -> crawlResultService.ingest(crawl, download).map(x -> download))
                .flatMap(download -> {
                    return Flux.fromStream(download.links.stream().map(link -> {
                        Crawl outboundCrawl = new Crawl(crawl.crawlId, link);

                        return crawlStatusService.isUrlKnown(outboundCrawl)
                                .flatMap((visited) -> {
                                    if (!visited && crawl.isWithinSameDomain(outboundCrawl)) {
                                        return crawlStatusService
                                            .markUrlAsPending(outboundCrawl)
                                            .flatMap(marked ->
                                                    this.schedulerService.scheduleCrawl(outboundCrawl)
                                            );
                                    } else {
                                        return Mono.empty();
                                    }
                                });
                    })).flatMap(c -> c).collectList();
                });
    }

}
