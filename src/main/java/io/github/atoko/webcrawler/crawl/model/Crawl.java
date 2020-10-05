package io.github.atoko.webcrawler.crawl.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;

@Data
@AllArgsConstructor
public class Crawl {
    public String crawlId;
    public String url;

    public boolean isWithinSameDomain(Crawl other) {
        return isWithinSameDomain(other.url);
    }

    public boolean isWithinSameDomain(String url) {
        try {
            return new URI(this.url).getAuthority().equals(new URI(url).getAuthority());
        } catch (Exception e) {
            return false;
        }
    }
}
