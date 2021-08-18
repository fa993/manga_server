package com.fa993.core.exceptions;

import com.fa993.core.pojos.Source;

public class PageProcessingException extends RuntimeException {

	private int pageNumber;
	private Source s;
	
	public PageProcessingException(int pageNumber, Source s, Throwable t) {
		super(t);
		this.pageNumber = pageNumber;
		this.s = s;
	}
	
	public int getPageNumber() {
		return this.pageNumber;
	}
	
	public Source getSource() {
		return this.s;
	}
}
