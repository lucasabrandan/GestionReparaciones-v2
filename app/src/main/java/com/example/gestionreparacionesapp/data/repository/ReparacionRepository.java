// REEMPLAZA TODO EL ARCHIVO CON ESTE CÓDIGO
package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReparacionRepository {

    private final ReparacionDao reparacionDao;
    private final ExecutorService executorService;
    private final int userId;

    public ReparacionRepository(ReparacionDao reparacionDao, Context context) {
        this.reparacionDao = reparacionDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.userId = SessionManager.getUserId(context);
    }

    public void obtenerTodasLasReparaciones(Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> {
            List<Reparacion> reparaciones = reparacionDao.getAll(userId);
            callback.accept(reparaciones);
        });
    }

    public void buscarReparacionesPorTermino(String query, Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> {
            String searchQuery = (query == null || query.trim().isEmpty()) ? "" : query;
            List<Reparacion> reparaciones = reparacionDao.buscar(searchQuery, userId);
            callback.accept(reparaciones);
        });
    }

    // --- MÉTODO INSERTAR CORREGIDO ---
    public void insertarReparacion(int clienteId, String marca, String modelo, String serie, String descripcion, String repuestos, String costoRepuestosStr, String costoManoObraStr, String estado, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                if (marca.isEmpty() || modelo.isEmpty() || descripcion.isEmpty() || estado.isEmpty()) {
                    callback.accept(new ResultadoRegistro(false, "Los campos principales son obligatorios."));
                    return;
                }
                // Convertimos los costos, si están vacíos los tratamos como 0
                double costoRepuestos = costoRepuestosStr.isEmpty() ? 0.0 : Double.parseDouble(costoRepuestosStr);
                double costoManoObra = costoManoObraStr.isEmpty() ? 0.0 : Double.parseDouble(costoManoObraStr);

                // Creamos el objeto Reparacion con el NUEVO constructor
                Reparacion nuevaReparacion = new Reparacion(userId, clienteId, marca, modelo, serie, descripcion, repuestos, costoRepuestos, costoManoObra, estado);
                reparacionDao.insert(nuevaReparacion);
                callback.accept(new ResultadoRegistro(true, "Reparación guardada con éxito."));

            } catch (NumberFormatException e) {
                callback.accept(new ResultadoRegistro(false, "Los costos deben ser números válidos."));
            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error al guardar: " + e.getMessage()));
            }
        });
    }

    // --- MÉTODO ACTUALIZAR CORREGIDO ---
    public void actualizarReparacion(int reparacionId, int clienteId, String marca, String modelo, String serie, String descripcion, String repuestos, String costoRepuestosStr, String costoManoObraStr, String estado, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                double costoRepuestos = costoRepuestosStr.isEmpty() ? 0.0 : Double.parseDouble(costoRepuestosStr);
                double costoManoObra = costoManoObraStr.isEmpty() ? 0.0 : Double.parseDouble(costoManoObraStr);

                Reparacion reparacionActualizada = new Reparacion(userId, clienteId, marca, modelo, serie, descripcion, repuestos, costoRepuestos, costoManoObra, estado);
                reparacionActualizada.setId(reparacionId); // ¡Muy importante para actualizar!
                reparacionDao.update(reparacionActualizada);
                callback.accept(new ResultadoRegistro(true, "Reparación actualizada."));

            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error al actualizar la reparación."));
            }
        });
    }

    public void eliminarReparacionPorId(int reparacionId, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                Reparacion reparacion = reparacionDao.getReparacionById(reparacionId, userId);
                if (reparacion != null) {
                    reparacionDao.delete(reparacion);
                    callback.accept(new ResultadoRegistro(true, "Reparación eliminada."));
                } else {
                    callback.accept(new ResultadoRegistro(false, "Error: No se encontró la reparación."));
                }
            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error al eliminar la reparación."));
            }
        });
    }
}