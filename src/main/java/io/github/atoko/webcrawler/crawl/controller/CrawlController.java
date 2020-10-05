package io.github.atoko.webcrawler.crawl.controller;

import io.github.atoko.webcrawler.crawl.model.request.CrawlRequest;
import io.github.atoko.webcrawler.crawl.model.response.CrawlAcceptedResponse;
import io.github.atoko.webcrawler.crawl.model.response.CrawlResultResponse;
import io.github.atoko.webcrawler.crawl.service.CrawlResultService;
import io.github.atoko.webcrawler.crawl.service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final CrawlResultService crawlResultService;
    @Autowired
    public CrawlController(CrawlService crawlService,
                           CrawlResultService crawlResultService) {
        this.crawlService = crawlService;
        this.crawlResultService = crawlResultService;
    }

    @PostMapping
    public Mono<ResponseEntity<CrawlAcceptedResponse>> createCrawl(
            @RequestBody @Valid CrawlRequest request
    ) {
        return crawlService.startCrawl(request.getUrl()).map(crawl ->
                ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new CrawlAcceptedResponse(crawl))
        );
    }

    @GetMapping("/{crawlId}/result")
    public Mono<ResponseEntity<CrawlResultResponse>> readCrawl(
            @PathVariable String crawlId) {
        return crawlResultService.getCrawlResultById(crawlId).map(results ->
                ResponseEntity.status(HttpStatus.OK)
                        .body(new CrawlResultResponse(results))
                ).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
