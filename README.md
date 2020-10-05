# Introduction
Webcrawler is a Java based server that when given a url, will crawl across all it's links recursively, only visiting links within the same domain.

All interactions with the webcrawler are via the HTTP protocol by exposing an API that follows RESTful principles. The webcrawling process is parallelized and automatically uses all available CPU cores.

## Getting Started

### Requirements
- Java 8 JDK
- cURL or any other HTTP request tooling

To start the server, you can run `./gradlew bootRun` which will build the application and run it.

#### Docker Image

A dockerfile is provided, but it requires the application JAR to be built first. To use the docker image, you must first `./gradlew build` and then use the appropriate `docker build .` command.


Once the server is running, it listens to commands on port `8080`.
## API

### POST /v1/crawl
Initiates a crawl for a given URL. 

#### Request Body
```
{
    "url": "https://wiprodigital.com"
}
```

#### Response Body
```
{
    "crawlId": "eac2b5a9-275a-48e2-accd-5cdf33e6c125",
    "url": "https://wiprodigital.com"
}
```
#### Example cURL
```
curl --location --request POST 'localhost:8080/v1/crawl' \
--header 'Content-Type: application/json' \
--data-raw '{
    "url": "https://wiprodigital.com"
}'
```
### GET /v1/crawl/{id}/results
Fetches the result of a specific crawl. A valid crawlId must be passed into the path of the URL, otherwise this endpoint returns a `404 Not Found`

This endpoint can be called at any time during the crawl process, and it will return all data collected thus far.

#### Response Body
```
{
    "crawlId": "7f5275ba-2901-407d-a78f-4b0ce25668f3",
    "urls": {
        "https://wiprodigital.com/what-we-think/": {
            "externalLinks": [
                "https://www.facebook.com/WiproDigital/",
                "https://twitter.com/wiprodigital",
                "https://www.linkedin.com/company/wipro-digital",
                "https://digistories.wiprodigital.com/?utm_source=Subscribe_Link&utm_medium=WD_Internal_clicks&utm_campaign=Digi%20Stories_%20Subscription_Internal_Clicks&utm_content=7010K000001eahI"
            ],
            "outboundLinks": [
                "https://wiprodigital.com",
                "https://wiprodigital.com/who-we-are/"
            ],
            "images": [
                "https://s17776.pcdn.co/wp-content/themes/wiprodigital/images/logo.png",
                "https://s17776.pcdn.co/wp-content/themes/wiprodigital/images/logo-dk-2X.png",
                "https://px.ads.linkedin.com/collect/?pid=696835&fmt=gif"
            ]        
        }
    }
}
```
#### Example cURL
```
curl --location --request GET 'localhost:8080/v1/crawl/7f5275ba-2901-407d-a78f-4b0ce25668f3/result' 
```
## Testing

To run test cases, use the `./gradlew test` task. 

The test task also runs a lint (Checkstyle) and produces a test coverage report, found in `/build/jacoco/test/html`

# Architecture

- CrawlService
    - The main engine for crawls. It accepts new crawls and queues them into the scheduler. Once a page is scheduled to be processed, it coordinates with all other services to recursively crawl through a domain.
- SchedulerService
    - Polls a queue every 5 seconds. All polled items will be sent to the crawl service for further processing.
- DownloadService
    - Handles downloading a webpage and parsing all the relevant data including links and images.
- CrawlResultService
    - Incrementally stores the result of a page download, associating all data collected for a given URL
- CrawlStatusService
    - Keeps track of links visited for a given crawl and is able to calculate it's progress

## Libraries used
- Spring Webflux
    - The webflux library is used to quickly set up an http server using the reactive paradigm. This handles the external facing API that is used to interact with the server.
- Reactor 
    - The application uses the Reactor library for handling concurrency, allowing it to efficiently parallelize the crawling process.
- JSoup
    - Jsoup is used to parse downloaded webpages and extract key elements.

## Tradeoffs

- Web Server based
    - The webcrawler is designed to be a long lived process that can handle multiple crawls. It is not limited to a single page but rather meant to be reusable.

- Usage of Mono (Deferrables) even in synchronous code
    - Some methods (particularly the CrawlStatusService) use the reactive Mono types even in code that simply adds to an in-memory map. The reasoning is that if we were to enhance the server to use a real database, it would be possible to do so without affecting other areas of the codebase.

# Future Development

- Use database instead of in-memory structures
    - All the services use in memory hashmaps to store their data. Because it's designed to be asynchronous-first, the application can be enhanced to take advantage of appropriate data stores for each module (E.g: queue for the scheduler, cache for the status and document store for the results)
- Web Interface
    - Because the webcrawler is implemented as a server with an HTTP api, it would be possible to create a frontend that is accessible via the browser. This webapp would use the provided endpoints to initiate a crawl, display crawl progress and show results of a crawl.
- Improve dockerfile
    - The provided dockerfile can be improved so that the application JAR is built inside the docker process, allowing the app to be built without requiring the user install the JDK.
- No integration tests
    - All current tests are done at the unit level, verifying that the application components execute as expected. Integration tests are possible, but would require setting up a mock webpage to be crawled through.
