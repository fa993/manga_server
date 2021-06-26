package com.fa993.core.dto;

import com.fa993.core.pojos.Genre;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MangaHeadingProper(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("coverURL") String coverURL, @JsonProperty("smallDescription") String descriptionSmall, @JsonProperty("genres") Genre genres) {
}
