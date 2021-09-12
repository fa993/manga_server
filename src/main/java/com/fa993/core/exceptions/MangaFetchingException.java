package com.fa993.core.exceptions;

import com.fa993.retrieval.pojos.MangaDTO;

public class MangaFetchingException extends RuntimeException {
	
	private final String url;
	private final MangaDTO partiallyProcessed;
	
	public MangaFetchingException(String url, MangaDTO dto, Throwable t) {
		super(t);
		this.url = url;
		this.partiallyProcessed = dto;
	}
	
	public String getURL() {
		return this.url;
	}
	
	public MangaDTO getPartialManga() {
		return this.partiallyProcessed;
	}
	
}
