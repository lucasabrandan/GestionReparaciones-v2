package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoVentaDao;
import com.example.gestionreparacionesapp.data.db.dao.VentaDao;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VentaRepository {

    private final VentaDao ventaDao;
    private final ProductoDao productoDao;
    private final ProductoVentaDao productoVentaDao;
    private final ExecutorService executorService;
    private final int userId;

    public VentaRepository(VentaDao ventaDao, Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.ventaDao = ventaDao;
        this.productoDao = db.productoDao();
        this.productoVentaDao = db.productoVentaDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.userId = SessionManager.getUserId(context);
    }

    public void getAllVentas(RepositoryCallback<List<Venta>> callback) {
        executorService.execute(() -> {
            List<Venta> ventas = ventaDao.getAll(userId);
            if (callback != null) callback.onComplete(ventas);
        });
    }

    public void buscarVentas(String query, RepositoryCallback<List<Venta>> callback) {
        executorService.execute(() -> {
            List<Venta> ventas = ventaDao.buscar(query, userId);
            if (callback != null) callback.onComplete(ventas);
        });
    }

    // --- INSERTAR VENTA: Guarda Venta, Items y Descuenta Stock ---
    public void insertVenta(Venta venta, List<ProductoVenta> productos, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                venta.setUserId(userId);
                long ventaId = ventaDao.insert(venta);

                if (ventaId > 0) {
                    // Recorrer productos para asignar ID de venta y descontar stock
                    for (ProductoVenta item : productos) {
                        item.setVentaId((int) ventaId);

                        // Descontar stock
                        // Importante: getById debe verificar userId para seguridad
                        Producto p = productoDao.getById(item.getProductoId(), userId);
                        if (p != null) {
                            int nuevoStock = p.getCantidad() - item.getCantidad();
                            p.setCantidad(Math.max(0, nuevoStock)); // Evitar negativos
                            productoDao.update(p);
                        }
                    }
                    // Insertar todos los items en la tabla intermedia
                    productoVentaDao.insertAll(productos);

                    if (callback != null) callback.onComplete(new ResultadoRegistro(true, "Venta registrada con éxito"));
                } else {
                    if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al guardar venta"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error: " + e.getMessage()));
            }
        });
    }

    public void updateVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                ventaDao.update(venta);
                if (callback != null) callback.onComplete(new ResultadoRegistro(true, "Venta actualizada"));
            } catch (Exception e) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al actualizar"));
            }
        });
    }

    public void deleteVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                ventaDao.delete(venta);
                if (callback != null) callback.onComplete(new ResultadoRegistro(true, "Venta eliminada"));
            } catch (Exception e) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al eliminar"));
            }
        });
    }
}