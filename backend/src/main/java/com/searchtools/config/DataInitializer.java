package com.searchtools.config;

import com.searchtools.repository.ResourceRepository;
import com.searchtools.search.SearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 启动时确保搜索索引是最新的
 * 资源通过实际爬取获取，不预置假数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ResourceRepository resourceRepository;
    private final SearchEngine searchEngine;

    @Override
    public void run(String... args) {
        log.info("数据库当前有{}条资源记录", resourceRepository.count());
        
        // 确保索引是最新的
        searchEngine.buildIndex();
        log.info("搜索索引已更新");
    }
}
