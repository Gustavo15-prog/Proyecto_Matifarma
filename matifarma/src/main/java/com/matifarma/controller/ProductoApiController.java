package com.matifarma.controller;

import com.matifarma.model.*;
import com.matifarma.service.CategoriaService;
import com.matifarma.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductoApiController {
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoApiController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/productos")
    public List<ProductoVista> listar(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Integer idCategoria,
                                      @RequestParam(defaultValue = "false") boolean todos) {
        return productoService.listar(q, idCategoria, !todos);
    }

    /** Búsqueda rápida de productos activos para ventas e ingresos. */
    @GetMapping("/productos/buscar")
    public List<ProductoVista> buscar(@RequestParam(required = false) String q) { return productoService.buscarParaOperar(q); }

    @GetMapping("/productos/{id}")
    public ProductoVista obtener(@PathVariable int id) { return productoService.obtener(id); }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoVista crear(@Valid @RequestBody ProductoRequest r) { return productoService.crear(r); }

    @PutMapping("/productos/{id}")
    public ProductoVista actualizar(@PathVariable int id, @Valid @RequestBody ProductoRequest r) { return productoService.actualizar(id, r); }

    @PatchMapping("/productos/{id}/estado")
    public ProductoVista estado(@PathVariable int id, @Valid @RequestBody EstadoRequest r) { return productoService.cambiarEstado(id, r.activo()); }

    @GetMapping("/categorias")
    public List<Categoria> categorias() { return categoriaService.listar(); }

    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crearCategoria(@Valid @RequestBody CategoriaRequest r) { return categoriaService.crear(r.nombre()); }
}
