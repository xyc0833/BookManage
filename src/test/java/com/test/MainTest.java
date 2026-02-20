package com.test;

import book.manage.sql.SqlUtil;
import org.junit.jupiter.api.Test;

public class MainTest {
    @Test
    public void test1(){
        SqlUtil.doSqlWork(mapper -> {
            mapper.getBorrowList().forEach(System.out::println);
        });
    }
}
