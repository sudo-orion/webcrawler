package io.github.atoko.webcrawler.crawl.controller;

import io.github.atoko.webcrawler.crawl.model.request.CrawlRequest;
import io.github.atoko.webcrawler.crawl.model.response.CrawlAcceptedResponse;
import io.github.atoko.webcrawler.crawl.service.CrawlService;
import io.github.atoko.webcrawler.scheduler.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController()
@RequestMapping("/v1/crawl")
public class CrawlController {

    private final CrawlService crawlService;
    @Autowired
    public CrawlController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @PostMapping
    public Mono<ResponseEntity<CrawlAcceptedResponse>> requestCrawl(
            @RequestBody @Valid CrawlRequest request
    ) {
        return crawlService.startCrawl(request.getUrl()).map(crawl ->
                ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new CrawlAcceptedResponse(crawl))
        );
    }

}
