package io.github.atoko.webcrawler.download.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Download {
    public List<String> links;
    public List<String> images;
}
