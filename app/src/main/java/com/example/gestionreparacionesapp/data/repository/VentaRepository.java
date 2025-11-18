package com.example.gestionreparacionesapp.data.repository;

import android.app.Application;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.VentaDao;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.Collections;
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
        // Obtenemos el ID del usuario de la sesión al crear el repositorio
        this.userId = SessionManager.getUserId(application);
    }

    /**
     * Inserta una nueva venta y actualiza el stock de productos de forma transaccional.
     */
    public void insertVenta(Venta venta, List<ProductoVenta> productos, RepositoryCallback<ResultadoRegistro> callback) {
        if (userId == -1) {
            callback.onComplete(new ResultadoRegistro(false, "Error de sesión de usuario. Inicie sesión de nuevo."));
            return;
        }
        venta.setUserId(userId);

        databaseWriteExecutor.execute(() -> {
            try {
                // Comprobamos si el DAO transaccional está disponible
                if (db.ventaConProductosDao() != null) {
                    long id = db.ventaConProductosDao().insertarVentaYActualizarStock(venta, productos, db);
                    if (id > 0) {
                        callback.onComplete(new ResultadoRegistro(true, "Venta #" + id + " guardada y stock actualizado."));
                    } else {
                        // Si el DAO transaccional devuelve un error (ej. por falta de stock), lo notificamos.
                        String errorMsg = db.ventaConProductosDao().getMensajeErrorStock();
                        callback.onComplete(new ResultadoRegistro(false, errorMsg));
                    }
                } else {
                    // Fallback por si la lógica transaccional no está implementada.
                    long id = ventaDao.insert(venta);
                    callback.onComplete(new ResultadoRegistro(true, "Venta #" + id + " guardada (sin transacción de stock)."));
                }
            } catch (Exception e) {
                // Captura para errores inesperados de la base de datos (ej. violaciones de constraints)
                callback.onComplete(new ResultadoRegistro(false, "Error inesperado en la base de datos: " + e.getMessage()));
            }
        });
    }

    /**
     * Obtiene todas las ventas asociadas al usuario actual.
     */
    public void getAllVentas(RepositoryCallback<List<Venta>> callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // La llamada al DAO ahora coincide con su definición (pasando el userId)
                List<Venta> ventas = ventaDao.getAll(userId);
                callback.onComplete(ventas);
            } catch (Exception e) {
                // En caso de un error de base de datos, devolvemos una lista vacía para evitar crasheos.
                callback.onComplete(Collections.emptyList());
            }
        });
    }

    /**
     * Busca ventas por nombre de cliente o fecha para el usuario actual.
     */
    public void buscarVentas(String query, RepositoryCallback<List<Venta>> callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // La llamada al DAO ahora coincide con su definición (pasando query y userId)
                List<Venta> ventas = ventaDao.buscar(query, userId);
                callback.onComplete(ventas);
            } catch (Exception e) {
                callback.onComplete(Collections.emptyList());
            }
        });
    }

    /**
     * Actualiza una venta existente, asegurando que pertenece al usuario actual.
     */
    public void updateVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        if (userId == -1) {
            callback.onComplete(new ResultadoRegistro(false, "Error de sesión de usuario."));
            return;
        }
        // Aseguramos que la venta que se actualiza tiene el ID de usuario correcto.
        venta.setUserId(userId);

        databaseWriteExecutor.execute(() -> {
            try {
                ventaDao.update(venta);
                callback.onComplete(new ResultadoRegistro(true, "Venta actualizada correctamente."));
            } catch (Exception e) {
                callback.onComplete(new ResultadoRegistro(false, "Error al actualizar la venta."));
            }
        });
    }

    /**
     * Elimina una venta.
     */
    public void deleteVenta(Venta venta, RepositoryCallback<ResultadoRegistro> callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                ventaDao.delete(venta);
                callback.onComplete(new ResultadoRegistro(true, "Venta eliminada correctamente."));
            } catch (Exception e) {
                callback.onComplete(new ResultadoRegistro(false, "Error al eliminar la venta."));
            }
        });
    }
}