package com.fa993.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MangaHeading(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("coverURL") String coverURL, @JsonProperty("smallDescription") String descriptionSmall, @JsonProperty("genres") String allGenres) {
}
