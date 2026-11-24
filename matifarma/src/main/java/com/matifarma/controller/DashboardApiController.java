package com.matifarma.controller;

import com.matifarma.model.DashboardResumen;
import com.matifarma.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {
    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping
    public DashboardResumen resumen() { return dashboardService.resumen(); }
}
