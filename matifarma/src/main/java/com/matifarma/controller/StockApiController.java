package com.matifarma.controller;

import com.matifarma.model.ProductoVista;
import com.matifarma.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consulta de stock y precios (CU05) disponible para todos los roles. El precio de compra solo lo ve el administrador. */
@RestController
@RequestMapping("/api/stock")
public class StockApiController {
    private final ProductoService productoService;

    public StockApiController(ProductoService productoService) { this.productoService = productoService; }

    @GetMapping
    public List<ProductoVista> consultar(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) Integer idCategoria) {
        return productoService.listar(q, idCategoria, true);
    }
}
