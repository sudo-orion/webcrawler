package io.github.atoko.webcrawler.crawl.model;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class CrawlStatus {
    public String crawlId;
    public ConcurrentMap<String, Boolean> linksVisited =
            new ConcurrentHashMap<>();
    public ConcurrentMap<String, Boolean> images = new ConcurrentHashMap<>();

    public CrawlStatus(String crawlId) {
        this.crawlId = crawlId;
    }
}
