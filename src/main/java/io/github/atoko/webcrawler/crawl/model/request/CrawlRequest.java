package io.github.atoko.webcrawler.crawl.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Data
public class CrawlRequest {
    @URL(message = "must be a valid url")
    @NotEmpty(message = "url must not be empty")
    public String url;
}
