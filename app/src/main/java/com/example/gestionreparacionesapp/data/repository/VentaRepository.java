package com.example.gestionreparacionesapp.data.repository;

import android.app.Application;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.VentaDao;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class VentaRepository {

    private final VentaDao ventaDao;
    private final AppDatabase db;
    private final ExecutorService databaseWriteExecutor;
    private final int userId;

    public VentaRepository(VentaDao ventaDao, Application application) {
        this.ventaDao = ventaDao;
        this.db = AppDatabase.getInstance(application);
        this.databaseWriteExecutor = AppDatabase.databaseWriteExecutor;
        this.userId = SessionManager.getUserId(application);
    }

    /**
     * Inserta una nueva venta y actualiza el stock de productos de forma transaccional.
     */
    public void insertVenta(Venta venta, List<ProductoVenta> productos, RepositoryCallback<ResultadoRegistro> callback) {
        if (userId == -1) {
            callback.onComplete(new ResultadoRegistro(false, "Error de sesión de usuario."));
            return;
        }
        venta.setUserId(userId);

        databaseWriteExecutor.execute(() -> {
            // Ya no se necesita try-catch para la lógica de negocio, solo para errores inesperados de DB.
            try {
                // --- LLAMADA AL NUEVO MÉTODO DEL DAO ---
                long id = db.ventaConProductosDao().insertarVentaYActualizarStock(venta, productos, db);

                if (id > 0) {
                    // ÉXITO: El ID es positivo
                    callback.onComplete(new ResultadoRegistro(true, "Venta #" + id + " guardada y stock actualizado."));
                } else {
                    // FALLO POR STOCK: El ID es -1L
                    String errorMsg = db.ventaConProductosDao().getMensajeErrorStock();
                    callback.onComplete(new ResultadoRegistro(false, errorMsg));
                }
            } catch (Exception e) {
                // Este catch es para errores de base de datos inesperados, no para la lógica de stock
                callback.onComplete(new ResultadoRegistro(false, "Error inesperado en la base de datos: " + e.getMessage()));
            }
        });
    }

    // --- El resto de los métodos no cambian ---

    public void getAllVentas(RepositoryCallback<List<Venta>> callback) {
        databaseWriteExecutor.execute(() -> {
            List<Venta> ventas = ventaDao.getAll(userId);
            callback.onComplete(ventas);
        });
    }

    public void buscarVentas(String query, RepositoryCallback<List<Venta>> callback) {
        databaseWriteExecutor.execute(() -> {
            List<Venta> ventas = ventaDao.buscar(query, userId);
            callback.onComplete(ventas);
        });
    }

    public void updateVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        venta.setUserId(userId);
        databaseWriteExecutor.execute(() -> {
            try {
                ventaDao.update(venta);
                callback.onComplete(new ResultadoRegistro(true, "Venta actualizada."));
            } catch (Exception e) {
                callback.onComplete(new ResultadoRegistro(false, "Error al actualizar."));
            }
        });
    }

    public void deleteVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                ventaDao.delete(venta);
                callback.onComplete(new ResultadoRegistro(true, "Venta eliminada."));
            } catch (Exception e) {
                callback.onComplete(new ResultadoRegistro(false, "Error al eliminar."));
            }
        });
    }
}
