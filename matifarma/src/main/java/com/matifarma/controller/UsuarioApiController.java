package com.matifarma.controller;

import com.matifarma.model.EstadoRequest;
import com.matifarma.model.UsuarioRequest;
import com.matifarma.model.UsuarioVista;
import com.matifarma.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {
    private final UsuarioService usuarioService;

    public UsuarioApiController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @GetMapping
    public List<UsuarioVista> listar() { return usuarioService.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioVista crear(@Valid @RequestBody UsuarioRequest r) { return usuarioService.crear(r); }

    @PutMapping("/{id}")
    public UsuarioVista actualizar(@PathVariable int id, @Valid @RequestBody UsuarioRequest r) { return usuarioService.actualizar(id, r); }

    @PatchMapping("/{id}/estado")
    public UsuarioVista estado(@PathVariable int id, @Valid @RequestBody EstadoRequest r) { return usuarioService.cambiarEstado(id, r.activo()); }

    @PostMapping("/{id}/desbloquear")
    public UsuarioVista desbloquear(@PathVariable int id) { return usuarioService.desbloquear(id); }
}
