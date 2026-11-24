package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.dao.ReporteDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.ReporteResultado;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReporteService {
    private final ReporteDao reporteDao;

    public ReporteService(ReporteDao reporteDao) { this.reporteDao = reporteDao; }

    public ReporteResultado generar(String tipo, LocalDate desde, LocalDate hasta) {
        if (tipo == null) throw ReglaNegocioException.invalido("tipo", "Seleccione el tipo de reporte");
        switch (tipo) {
            case "stock":
                return reporteDao.stock(Fechas.hoy());
            case "ventas":
            case "movimientos":
                validarRango(desde, hasta);
                return tipo.equals("ventas") ? reporteDao.ventas(desde, hasta) : reporteDao.movimientos(desde, hasta);
            default:
                throw ReglaNegocioException.invalido("tipo", "Tipo de reporte no válido");
        }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null) throw ReglaNegocioException.invalido("desde", "Seleccione la fecha inicial");
        if (hasta == null) throw ReglaNegocioException.invalido("hasta", "Seleccione la fecha final");
        if (desde.isAfter(hasta)) throw ReglaNegocioException.invalido("desde", "La fecha inicial no puede ser mayor que la fecha final");
        if (hasta.isAfter(Fechas.hoy())) throw ReglaNegocioException.invalido("hasta", "La fecha final no puede ser futura");
        if (ChronoUnit.DAYS.between(desde, hasta) > 366) throw ReglaNegocioException.invalido("hasta", "El rango máximo permitido es de 366 días");
    }
}
