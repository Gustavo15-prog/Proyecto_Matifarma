package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.DashboardDao;
import com.matifarma.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final DashboardDao dashboardDao;
    private final AlertaService alertaService;

    public DashboardService(DashboardDao dashboardDao, AlertaService alertaService) {
        this.dashboardDao = dashboardDao;
        this.alertaService = alertaService;
    }

    public DashboardResumen resumen() {
        LocalDate hoy = Fechas.hoy();
        List<AlertaVista> alertas = alertaService.listar();
        int bajo = (int) alertas.stream().filter(a -> a.tipo().equals("STOCK_BAJO")).count();
        int porVencer = (int) alertas.stream().filter(a -> a.tipo().equals("POR_VENCER")).count();
        int vencidos = (int) alertas.stream().filter(a -> a.tipo().equals("VENCIDO")).count();

        VentasDashboard ventas = null;
        if (!"ALMACENERO".equals(SesionActual.rol())) {
            LocalDate manana = hoy.plusDays(1);
            LocalDate desde = hoy.minusDays(6);
            Map<LocalDate, BigDecimal> porDia = new HashMap<>();
            dashboardDao.totalesPorDia(desde, manana).forEach(v -> porDia.put(v.fecha(), v.total()));
            List<VentaDia> ultimos = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate dia = desde.plusDays(i);
                ultimos.add(new VentaDia(dia, porDia.getOrDefault(dia, BigDecimal.ZERO)));
            }
            ventas = new VentasDashboard(dashboardDao.cantidadVentas(hoy, manana), dashboardDao.totalVentas(hoy, manana),
                    dashboardDao.totalVentas(hoy.withDayOfMonth(1), manana), ultimos);
        }
        List<AlertaVista> top = alertas.size() > 6 ? alertas.subList(0, 6) : alertas;
        return new DashboardResumen(dashboardDao.totalProductosActivos(), bajo, porVencer, vencidos, ventas, top);
    }
}
