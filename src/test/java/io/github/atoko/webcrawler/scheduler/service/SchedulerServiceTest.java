package io.github.atoko.webcrawler.scheduler.service;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.crawl.service.CrawlService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SchedulerServiceTest {

    @InjectMocks
    private SchedulerService schedulerService;

    @Mock
    private CrawlService crawlService;

    @Test
    public void it_polls_queue_every_5_seconds() throws InterruptedException {
        Crawl crawl = new Crawl("anyId", "anyUrl");
        Mockito.doReturn(Mono.just(new ArrayList())).when(crawlService).fetchPageAndCrawlOutboundLinks(any());

        schedulerService.scheduleCrawl(crawl);

        Assertions.assertEquals(1, schedulerService.getTotalItemsInQueue());
        StepVerifier.create(schedulerService.crawlEmitter).expectNextMatches(crawl::equals);
        Thread.sleep(SchedulerService.POLL_INTERVAL.toMillis());
        Assertions.assertEquals(0, schedulerService.getTotalItemsInQueue());
    }
}
