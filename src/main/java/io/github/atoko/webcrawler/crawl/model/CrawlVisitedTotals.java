package io.github.atoko.webcrawler.crawl.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrawlVisitedTotals {
    public long pending;
    public long visited;
}