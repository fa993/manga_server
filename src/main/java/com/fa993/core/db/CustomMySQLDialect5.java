package com.fa993.core.db;

import org.hibernate.dialect.MySQL5Dialect;

public class CustomMySQLDialect5 extends MySQL5Dialect {

    public CustomMySQLDialect5() {
        super();
        this.registerFunction("group_concat_distinct", new GroupConcatDistinctFunction());
    }
}
