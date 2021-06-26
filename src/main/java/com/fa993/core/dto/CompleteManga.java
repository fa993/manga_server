package com.fa993.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompleteManga(@JsonProperty("main") MainMangaData main, @JsonProperty("related") List<LinkedMangaData> related) {
}
