package io.github.atoko.webcrawler.crawl.model.response;

import io.github.atoko.webcrawler.crawl.model.Crawl;
import lombok.Data;

@Data
public class CrawlAcceptedResponse {
    public String crawlId;
    public String url;

    public CrawlAcceptedResponse(Crawl crawl) {
        this.crawlId = crawl.crawlId;
        this.url = crawl.url;
    }
}
