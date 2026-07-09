package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AdminController {

    @GetMapping
    public void getAuditLogs() {
    }
}
