package io.github.atoko.webcrawler.crawl.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Crawl {
    public String crawlId;
    public String url;
}
