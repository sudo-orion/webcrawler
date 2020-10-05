FROM azul/zulu-openjdk:8

WORKDIR /app
COPY /build/libs/webcrawler-0.0.1-SNAPSHOT.jar .

EXPOSE 8080
CMD java -Xms1024m -Xmx1024m -jar webcrawler-0.0.1-SNAPSHOT.jar