package com.searchtools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 全网盘搜索引擎应用主类
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SearchToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchToolsApplication.class, args);
    }
}
