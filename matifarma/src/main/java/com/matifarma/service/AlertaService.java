package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.AlertaDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.AlertaVista;
import com.matifarma.model.AtenderAlertaRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class AlertaService {
    private static final Set<String> TIPOS = Set.of("STOCK_BAJO", "POR_VENCER", "VENCIDO");
    private final AlertaDao alertaDao;
    @Value("${matifarma.alertas.dias-vencimiento:30}") private int diasVencimiento;

    public AlertaService(AlertaDao alertaDao) { this.alertaDao = alertaDao; }

    public List<AlertaVista> listar() {
        LocalDate hoy = Fechas.hoy();
        return alertaDao.listar(hoy, hoy.plusDays(diasVencimiento));
    }

    public int diasVencimiento() { return diasVencimiento; }

    @Transactional
    public void atender(AtenderAlertaRequest r) {
        if (!TIPOS.contains(r.tipo())) throw ReglaNegocioException.invalido("Tipo de alerta no permitido");
        boolean vigente = listar().stream().anyMatch(a -> a.idProducto().equals(r.idProducto()) && a.tipo().equals(r.tipo()));
        if (!vigente) throw ReglaNegocioException.conflicto("La alerta ya no está vigente o ya fue atendida.");
        alertaDao.atender(r.idProducto(), r.tipo(), SesionActual.idEmpleado(), Fechas.ahora());
    }
}
