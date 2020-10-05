package io.github.atoko.webcrawler.crawl.model;

import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class CrawlResult {
    public String crawlId;
    public ConcurrentMap<String, List<String>> outboundLinks = new ConcurrentHashMap<>();
    public ConcurrentMap<String, List<String>> externalLinks = new ConcurrentHashMap<>();
    public ConcurrentMap<String, List<String>> images = new ConcurrentHashMap<>();

    public CrawlResult(String crawlId) {
        this.crawlId = crawlId;
    }
}
