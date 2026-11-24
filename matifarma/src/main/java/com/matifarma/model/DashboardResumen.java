package com.matifarma.model;

import java.util.List;

public record DashboardResumen(int totalProductos, int stockBajo, int porVencer, int vencidos,
                               VentasDashboard ventas, List<AlertaVista> alertas) {}
