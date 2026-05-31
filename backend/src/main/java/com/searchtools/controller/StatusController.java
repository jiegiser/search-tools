package com.searchtools.controller;

import com.searchtools.model.SystemStatus;
import com.searchtools.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 系统状态控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    @GetMapping("/status")
    public ResponseEntity<SystemStatus> getSystemStatus() {
        SystemStatus status = statusService.getSystemStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
