package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager; // Asegúrate de tener esta clase

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReparacionRepository {

    private final ReparacionDao reparacionDao;
    private final ExecutorService executorService;
    private final int userId;

    // Interfaz de callback genérica para devolver resultados al ViewModel
    public interface RepositorioCallback<T> {
        void enExito(T resultado);
    }

    public ReparacionRepository(ReparacionDao reparacionDao, Context context) {
        this.reparacionDao = reparacionDao;
        this.executorService = Executors.newSingleThreadExecutor();
        // Obtenemos el userId desde tu SessionManager
        this.userId = SessionManager.getUserId(context);
    }

    // --- ¡MÉTODO CON EL NOMBRE QUE EL VIEWMODEL ESPERA! ---
    public void obtenerTodasLasReparaciones(Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> {
            List<Reparacion> reparaciones = reparacionDao.getAll(userId);
            callback.accept(reparaciones);
        });
    }

    // --- ¡MÉTODO CON EL NOMBRE QUE EL VIEWMODEL ESPERA! ---
    public void buscarReparacionesPorTermino(String query, Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> {
            String searchQuery = (query == null || query.trim().isEmpty()) ? "" : query;
            List<Reparacion> reparaciones = reparacionDao.buscar(searchQuery, userId);
            callback.accept(reparaciones);
        });
    }

    // --- ¡MÉTODO CON LA FIRMA QUE EL VIEWMODEL ESPERA! ---
    public void insertarReparacion(int clienteId, String productoNombre, String descripcion, String presupuestoStr, String estado, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                if (descripcion.isEmpty() || productoNombre.isEmpty() || estado.isEmpty() || presupuestoStr.isEmpty()) {
                    callback.accept(new ResultadoRegistro(false, "Todos los campos son obligatorios."));
                    return;
                }
                double presupuesto = Double.parseDouble(presupuestoStr);
                // Creamos el objeto Reparacion aquí
                Reparacion nuevaReparacion = new Reparacion(userId, clienteId, productoNombre, descripcion, presupuesto, estado);
                reparacionDao.insert(nuevaReparacion);
                callback.accept(new ResultadoRegistro(true, "Reparación guardada con éxito."));
            } catch (NumberFormatException e) {
                callback.accept(new ResultadoRegistro(false, "El presupuesto debe ser un número válido."));
            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error al guardar: " + e.getMessage()));
            }
        });
    }

    // --- ¡MÉTODO CON LA FIRMA QUE EL VIEWMODEL ESPERA! ---
    public void actualizarReparacion(int reparacionId, int clienteId, String productoNombre, String descripcion, String presupuestoStr, String estado, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                double presupuesto = Double.parseDouble(presupuestoStr);
                Reparacion reparacionActualizada = new Reparacion(userId, clienteId, productoNombre, descripcion, presupuesto, estado);
                reparacionActualizada.setId(reparacionId); // ¡Muy importante para actualizar!
                reparacionDao.update(reparacionActualizada);
                callback.accept(new ResultadoRegistro(true, "Reparación actualizada."));
            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error al actualizar la reparación."));
            }
        });
    }

    // --- ¡MÉTODO CON EL NOMBRE QUE EL VIEWMODEL ESPERA! ---
    public void eliminarReparacionPorId(int reparacionId, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                // Obtenemos la reparación para asegurarnos de que existe antes de borrarla
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