package com.matifarma.controller;

import com.matifarma.model.VentaComprobante;
import com.matifarma.model.VentaRequest;
import com.matifarma.model.VentaResumen;
import com.matifarma.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaApiController {
    private final VentaService ventaService;

    public VentaApiController(VentaService ventaService) { this.ventaService = ventaService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaComprobante registrar(@Valid @RequestBody VentaRequest r) { return ventaService.registrar(r); }

    @GetMapping("/recientes")
    public List<VentaResumen> recientes() { return ventaService.recientes(); }

    @GetMapping("/{id}/comprobante")
    public VentaComprobante comprobante(@PathVariable int id) { return ventaService.comprobante(id); }
}
