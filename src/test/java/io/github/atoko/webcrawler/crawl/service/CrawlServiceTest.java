package io.github.atoko.webcrawler.crawl.service;


import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.crawl.model.CrawlVisitedTotals;
import io.github.atoko.webcrawler.download.model.Download;
import io.github.atoko.webcrawler.download.service.DownloadService;
import io.github.atoko.webcrawler.scheduler.service.SchedulerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CrawlServiceTest {

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private DownloadService downloadService;

    @Mock
    private CrawlStatusService crawlStatusService;

    @InjectMocks
    private CrawlService crawlService;

    @Captor
    private ArgumentCaptor<Crawl> crawlCaptor;


    @Captor
    private ArgumentCaptor<Crawl> outboundCrawlCaptor;

    @Test
    public void it_schedules_new_crawls_using_schedulerservice() {
        String url = "example.com";
        Mockito.doReturn(Mono.just(true)).when(crawlStatusService).markUrlAsPending(crawlCaptor.capture());
        Mockito.doAnswer(invocation -> Mono.just(crawlCaptor.getValue())).when(schedulerService)
                .scheduleCrawl(Mockito.any());

        this.crawlService.startCrawl(url)
                .subscribe(crawl -> {
                    Assertions.assertEquals(crawl.url, url);
                });
    }

    @Test
    public void it_recursively_crawls_using_downloadService() {
        String url = "http://example.com";
        String outboundUrl = "http://example.com/outboundLink";
        String outboundUrl2 = "http://example.com/outboundLink2";

        List<String> outboundLinks = new ArrayList();
        outboundLinks.add(outboundUrl);
        outboundLinks.add(outboundUrl2);

        Mockito.doReturn(Mono.just(new Download(
                outboundLinks,
                Collections.emptyList()
        ))).when(downloadService).fetch(url);

        Mockito.doReturn(Mono.just(true)).when(crawlStatusService).markUrlAsVisited(crawlCaptor.capture());
        Mockito.doReturn(Mono.just(new CrawlVisitedTotals(0, 1))).when(crawlStatusService)
                .getVisitedTotals(Mockito.any());

        Mockito.doReturn(Mono.just(false)).when(crawlStatusService).isUrlKnown(outboundCrawlCaptor.capture());
        Mockito.doReturn(Mono.just(true)).when(crawlStatusService).markUrlAsPending(Mockito.argThat((crawl) -> {
            return crawl.equals(outboundCrawlCaptor.getValue());
        }));
        Mockito.doAnswer(invocation -> Mono.just(outboundCrawlCaptor.getValue())).when(schedulerService)
                .scheduleCrawl(Mockito.argThat((crawl) -> {
                    return crawl.equals(outboundCrawlCaptor.getValue());
                }));

        this.crawlService.fetchPageAndCrawlOutboundLinks(new Crawl("uuid", url))
                .subscribe(pendingCrawls -> {
                    Assertions.assertEquals(2, pendingCrawls.stream().count());
                });
    }

    @Test
    public void it_does_not_crawl_outside_the_original_domain() {
        String url = "http://example.com";
        String outboundUrl = "http://example.com/outboundLink";
        String outboundUrl2 = "http://anotherdomain.com/link";

        List<String> outboundLinks = new ArrayList();
        outboundLinks.add(outboundUrl);
        outboundLinks.add(outboundUrl2);

        Mockito.doReturn(Mono.just(new Download(
                outboundLinks,
                Collections.emptyList()
        ))).when(downloadService).fetch(url);

        Mockito.doReturn(Mono.just(true)).when(crawlStatusService).markUrlAsVisited(crawlCaptor.capture());
        Mockito.doReturn(Mono.just(new CrawlVisitedTotals(0, 1))).when(crawlStatusService)
                .getVisitedTotals(Mockito.any());

        Mockito.doReturn(Mono.just(false)).when(crawlStatusService).isUrlKnown(outboundCrawlCaptor.capture());
        Mockito.doReturn(Mono.just(true)).when(crawlStatusService).markUrlAsPending(Mockito.argThat((crawl) -> {
            return crawl.equals(outboundCrawlCaptor.getValue());
        }));
        Mockito.doAnswer(invocation -> Mono.just(outboundCrawlCaptor.getValue())).when(schedulerService)
                .scheduleCrawl(Mockito.argThat((crawl) -> {
                    return crawl.equals(outboundCrawlCaptor.getValue());
                }));

        this.crawlService.fetchPageAndCrawlOutboundLinks(new Crawl("uuid", url))
                .subscribe(pendingCrawls -> {
                    Assertions.assertEquals(1, pendingCrawls.stream().count());
                });
    }
}
