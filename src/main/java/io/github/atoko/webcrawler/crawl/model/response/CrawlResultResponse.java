package io.github.atoko.webcrawler.crawl.model.response;

import io.github.atoko.webcrawler.crawl.model.CrawlResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CrawlResultResponse {
    public String crawlId;
    public Map<String, CrawlResultResponseItem> urls;
    public CrawlResultResponse(CrawlResult crawlResult) {
        this.crawlId = crawlResult.crawlId;
        this.urls = crawlResult.outboundLinks.keySet().stream().map((k) -> {
            return new AbstractMap.SimpleEntry<String, CrawlResultResponseItem>(k, new CrawlResultResponseItem(
                    crawlResult.externalLinks.get(k),
                    crawlResult.outboundLinks.get(k),
                    crawlResult.images.get(k)
            ));
        }).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));
    }

    @AllArgsConstructor
    private class CrawlResultResponseItem {
        public List<String> externalLinks;
        public List<String> outboundLinks;
        public List<String> images;
    }
}
