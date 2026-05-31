package com.searchtools;

import com.searchtools.controller.SearchController;
import com.searchtools.controller.CrawlController;
import com.searchtools.controller.StatusController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchToolsApplicationTests {

    @Test
    void contextLoads() {
        // 验证应用主类和控制器类可以加载
        assertDoesNotThrow(() -> Class.forName("com.searchtools.SearchToolsApplication"));
        assertDoesNotThrow(() -> Class.forName("com.searchtools.controller.SearchController"));
        assertDoesNotThrow(() -> Class.forName("com.searchtools.controller.CrawlController"));
        assertDoesNotThrow(() -> Class.forName("com.searchtools.controller.StatusController"));
    }
}
