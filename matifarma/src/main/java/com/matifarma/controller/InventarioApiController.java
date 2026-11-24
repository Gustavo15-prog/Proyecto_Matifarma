package com.matifarma.controller;

import com.matifarma.model.IngresoRequest;
import com.matifarma.model.IngresoResultado;
import com.matifarma.model.MovimientoVista;
import com.matifarma.model.SalidaRequest;
import com.matifarma.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioApiController {
    private final InventarioService inventarioService;

    public InventarioApiController(InventarioService inventarioService) { this.inventarioService = inventarioService; }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoResultado ingreso(@Valid @RequestBody IngresoRequest r) { return inventarioService.registrarIngreso(r); }

    @PostMapping("/salidas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> salida(@Valid @RequestBody SalidaRequest r) {
        return Map.of("stockResultante", inventarioService.registrarSalida(r));
    }

    @GetMapping("/movimientos")
    public List<MovimientoVista> movimientos(@RequestParam(required = false) String q) { return inventarioService.movimientos(q); }
}
