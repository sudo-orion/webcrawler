package io.github.atoko.webcrawler.crawl.service;


import io.github.atoko.webcrawler.crawl.model.Crawl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CrawlStatusServiceTest {

    private CrawlStatusService crawlStatusService = new CrawlStatusService();

    @Test
    public void it_marks_urls_as_pending() {
        Crawl crawl = new Crawl("anyId", "anyUrl");
        crawlStatusService.markUrlAsPending(crawl).flatMap((success) ->
                crawlStatusService.isUrlKnown(crawl)
                        .doOnNext(visited -> Assertions.assertEquals(true, visited))
        ).subscribe();
    }

    @Test
    public void it_marks_urls_as_visited() {
        Crawl crawl = new Crawl("anyId", "anyUrl");
        crawlStatusService.markUrlAsVisited(crawl).flatMap((success) ->
                crawlStatusService.isUrlKnown(crawl)
                        .doOnNext(visited -> Assertions.assertEquals(true, visited))
        ).subscribe();
    }

    @Test
    public void it_can_retrieve_crawl_completion_status() {
        Crawl crawl = new Crawl("anyId", "anyUrl");
        crawlStatusService.markUrlAsVisited(crawl).flatMap((success) ->
                crawlStatusService.getVisitedTotals(crawl.getCrawlId())
                        .doOnNext(totals -> Assertions.assertEquals(1, totals.visited))
        ).subscribe();
    }
}
