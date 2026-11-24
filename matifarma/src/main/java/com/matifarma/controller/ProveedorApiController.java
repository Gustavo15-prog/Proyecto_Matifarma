package com.matifarma.controller;

import com.matifarma.model.EstadoRequest;
import com.matifarma.model.Proveedor;
import com.matifarma.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorApiController {
    private final ProveedorService proveedorService;

    public ProveedorApiController(ProveedorService proveedorService) { this.proveedorService = proveedorService; }

    @GetMapping
    public List<Proveedor> listar(@RequestParam(required = false) String q, @RequestParam(defaultValue = "true") boolean todos) {
        return proveedorService.listar(q, !todos);
    }

    @GetMapping("/activos")
    public List<Proveedor> activos() { return proveedorService.listar(null, true); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor crear(@Valid @RequestBody Proveedor p) { return proveedorService.crear(p); }

    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable int id, @Valid @RequestBody Proveedor p) { return proveedorService.actualizar(id, p); }

    @PatchMapping("/{id}/estado")
    public Proveedor estado(@PathVariable int id, @Valid @RequestBody EstadoRequest r) { return proveedorService.cambiarEstado(id, r.activo()); }
}
