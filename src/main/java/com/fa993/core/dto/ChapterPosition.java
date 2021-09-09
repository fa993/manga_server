package com.fa993.core.dto;

import java.math.BigInteger;

public class ChapterPosition {

    private BigInteger index;
    private BigInteger length;

    public ChapterPosition() {
    }

    public ChapterPosition(BigInteger index, BigInteger length) {
        this.index = index;
        this.length = length;
    }

    public BigInteger getIndex() {
        return index;
    }

    public void setIndex(BigInteger index) {
        this.index = index;
    }

    public BigInteger getLength() {
        return length;
    }

    public void setLength(BigInteger length) {
        this.length = length;
    }
}
