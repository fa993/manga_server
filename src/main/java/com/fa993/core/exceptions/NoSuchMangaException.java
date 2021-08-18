package com.fa993.core.exceptions;

public class NoSuchMangaException extends RuntimeException implements WebException {

    private String body;
    
    public static NoSuchMangaException constructFromURL(String url) {
    	 return new NoSuchMangaException("No Manga Exists with the given URL : " + url);
    }
    
    public static NoSuchMangaException constructFromID(String id) {
    	return new NoSuchMangaException("No Manga Exists with the given ID : " + id);

    }

    private NoSuchMangaException(String message) {
        super(message);
        this.body = this.getMessage();
    }
    
    @Override
    public String getResponseBody() {
        return this.body;
    }
}
