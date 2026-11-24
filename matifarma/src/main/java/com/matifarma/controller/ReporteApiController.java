package com.matifarma.controller;

import com.matifarma.model.ReporteResultado;
import com.matifarma.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteApiController {
    private final ReporteService reporteService;

    public ReporteApiController(ReporteService reporteService) { this.reporteService = reporteService; }

    @GetMapping("/{tipo}")
    public ReporteResultado generar(@PathVariable String tipo,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.generar(tipo, desde, hasta);
    }
}
