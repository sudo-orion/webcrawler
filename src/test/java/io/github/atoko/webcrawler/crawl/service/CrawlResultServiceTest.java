package io.github.atoko.webcrawler.crawl.service;


import io.github.atoko.webcrawler.crawl.model.Crawl;
import io.github.atoko.webcrawler.download.model.Download;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CrawlResultServiceTest {

    private CrawlResultService crawlResultService  = new CrawlResultService();

    @Test
    public void it_ingests_crawl_download_data() {
        String url = "http://example.com";
        Crawl crawl = new Crawl("anyId", url);
        String outboundUrl = "http://example.com/outboundLink";
        String outboundUrl2 = "http://anotherdomain.com/link";

        List<String> outboundLinks = new ArrayList();
        outboundLinks.add(outboundUrl);
        outboundLinks.add(outboundUrl2);

        Download download = new Download(
                outboundLinks,
                Collections.singletonList("http://imagetolink")
        );

        crawlResultService.ingest(crawl, download)
                .flatMap(x -> crawlResultService.getCrawlResultById("anyId"))
                .subscribe(crawlResult -> {
                    Assertions.assertEquals(1, crawlResult.outboundLinks.get(url).size());
                    Assertions.assertEquals(1, crawlResult.externalLinks.get(url).size());
                    Assertions.assertEquals(1, crawlResult.images.get(url).size());
                });
    }
}
