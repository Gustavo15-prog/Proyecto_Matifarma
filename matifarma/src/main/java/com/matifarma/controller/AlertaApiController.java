package com.matifarma.controller;

import com.matifarma.model.AlertaVista;
import com.matifarma.model.AtenderAlertaRequest;
import com.matifarma.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaApiController {
    private final AlertaService alertaService;

    public AlertaApiController(AlertaService alertaService) { this.alertaService = alertaService; }

    @GetMapping
    public Map<String, Object> listar() {
        List<AlertaVista> alertas = alertaService.listar();
        return Map.of("diasVencimiento", alertaService.diasVencimiento(), "alertas", alertas);
    }

    @PostMapping("/atender")
    public Map<String, String> atender(@Valid @RequestBody AtenderAlertaRequest r) {
        alertaService.atender(r);
        return Map.of("mensaje", "Alerta marcada como atendida");
    }
}
