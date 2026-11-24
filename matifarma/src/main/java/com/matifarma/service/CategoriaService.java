package com.matifarma.service;

import com.matifarma.dao.CategoriaDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.Categoria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaDao categoriaDao;

    public CategoriaService(CategoriaDao categoriaDao) { this.categoriaDao = categoriaDao; }

    public List<Categoria> listar() { return categoriaDao.listar(); }

    public Categoria crear(String nombre) {
        String n = Texto.limpiar(nombre);
        if (n == null) throw ReglaNegocioException.invalido("nombre", "El nombre de la categoría es obligatorio");
        if (categoriaDao.existeNombre(n)) throw ReglaNegocioException.conflicto("nombre", "Ya existe una categoría con ese nombre");
        return categoriaDao.insertar(n);
    }
}
