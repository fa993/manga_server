package com.fa993.core.exceptions;

public class NoSuchMangaException extends RuntimeException implements WebException {

    private String body;

    public NoSuchMangaException(String id) {
        super("No Manga Exists with the given ID : " + id);
        this.body = this.getMessage();
    }

    @Override
    public String getResponseBody() {
        return this.body;
    }
}
