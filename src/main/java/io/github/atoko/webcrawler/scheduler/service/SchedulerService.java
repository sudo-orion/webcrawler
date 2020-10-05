package io.github.atoko.webcrawler.scheduler.service;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.crawl.service.CrawlService;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.logging.Logger;

@Component
public class SchedulerService {
    DirectProcessor<Crawl> crawlEmitter;
    private Queue<Crawl> queue = new ConcurrentLinkedDeque();
    private Logger logger = Logger.getLogger("SchedulerService");
    private CrawlService crawlService;
    static final Duration POLL_INTERVAL = Duration.ofSeconds(5);

    SchedulerService(CrawlService crawlService) {
        this.crawlService = crawlService;
        crawlEmitter = DirectProcessor.create();

        //Check for new urls on an interval
        Flux.interval(POLL_INTERVAL)
                .doOnNext((timestamp) -> {
                    // Fetch urls waiting to be crawled
                    Crawl current = queue.poll();

                    while (current != null) {
                        logger.info(String.format(
                                "Scheduled crawl %s : %s from queue",
                                current.crawlId, current.url));
                        crawlEmitter.sink().next(current);

                        //Remove url from queue
                        current = queue.poll();
                    }
                }).subscribe();


        //Run crawl in parallel
        crawlEmitter
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(
                        ((Function<Crawl, Publisher<?>>) this.crawlService::fetchPageAndCrawlOutboundLinks))
                .subscribe();
    }

    public Mono<Crawl> scheduleCrawl(Crawl crawl) {
        //Add crawl to queue
        queue.add(crawl);
        return Mono.just(crawl);
    }

    public int getTotalItemsInQueue() {
        return queue.size();
    }
}
