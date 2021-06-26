package com.fa993.core.corrections;

public enum LinkageType {

    ENSURE("ENSURE"), CREATE("CREATE"), DESTROY("DESTROY");

    private String value;

    private LinkageType(String value){
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
