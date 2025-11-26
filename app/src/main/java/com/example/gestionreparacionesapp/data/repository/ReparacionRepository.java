package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoReparacionDao;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReparacionRepository {

    private final ReparacionDao reparacionDao;
    private final ProductoDao productoDao;
    private final ProductoReparacionDao productoReparacionDao;
    private final ExecutorService executorService;
    private final int userId;

    public ReparacionRepository(ReparacionDao reparacionDao, Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.reparacionDao = reparacionDao;
        this.productoDao = db.productoDao();
        this.productoReparacionDao = db.productoReparacionDao();

        this.executorService = Executors.newSingleThreadExecutor();
        this.userId = SessionManager.getUserId(context);
    }

    public void obtenerTodasLasReparaciones(Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> callback.accept(reparacionDao.getAll(userId)));
    }

    public void buscarReparacionesPorTermino(String query, Consumer<List<Reparacion>> callback) {
        executorService.execute(() -> {
            String searchQuery = (query == null || query.trim().isEmpty()) ? "" : query;
            callback.accept(reparacionDao.buscar(searchQuery, userId));
        });
    }

    public void insertarReparacionConRepuestos(
            int clienteId, String marca, String modelo, String serie,
            String descripcion, double costoManoObra, String estado,
            List<ProductoReparacion> listaRepuestos,
            Consumer<ResultadoRegistro> callback) {

        executorService.execute(() -> {
            try {
                if (marca.isEmpty() || modelo.isEmpty() || descripcion.isEmpty()) {
                    callback.accept(new ResultadoRegistro(false, "Datos incompletos."));
                    return;
                }

                double costoTotalRepuestos = 0;
                StringBuilder nombresRepuestos = new StringBuilder();

                for (ProductoReparacion item : listaRepuestos) {
                    costoTotalRepuestos += (item.getCantidad() * item.getPrecioUnitarioCobrado());
                    nombresRepuestos.append(item.getCantidad()).append("x ").append(item.getNombreProductoSnapshot()).append(", ");
                }

                Reparacion nuevaReparacion = new Reparacion(
                        userId, clienteId, marca, modelo, serie, descripcion,
                        nombresRepuestos.toString(),
                        costoTotalRepuestos, costoManoObra, estado
                );

                long reparacionId = reparacionDao.insert(nuevaReparacion);

                if (reparacionId > 0) {
                    for (ProductoReparacion item : listaRepuestos) {
                        item.setReparacionId((int) reparacionId);

                        // --- CORRECCIÓN AQUÍ: Pasamos userId al getById ---
                        Producto prodReal = productoDao.getById(item.getProductoId(), userId);
                        if (prodReal != null) {
                            int nuevoStock = prodReal.getCantidad() - item.getCantidad();
                            prodReal.setCantidad(nuevoStock < 0 ? 0 : nuevoStock);
                            productoDao.update(prodReal);
                        }
                    }

                    if (!listaRepuestos.isEmpty()) {
                        productoReparacionDao.insertAll(listaRepuestos);
                    }

                    callback.accept(new ResultadoRegistro(true, "Reparación guardada y stock actualizado."));
                } else {
                    callback.accept(new ResultadoRegistro(false, "Error al guardar cabecera."));
                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(new ResultadoRegistro(false, "Error: " + e.getMessage()));
            }
        });
    }

    public void actualizarReparacionSimple(int id, int clienteId, String marca, String modelo, String serie, String desc, String repuestosTxt, String costoRepStr, String costoManoStr, String estado, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                double cRep = costoRepStr.isEmpty() ? 0 : Double.parseDouble(costoRepStr);
                double cMano = costoManoStr.isEmpty() ? 0 : Double.parseDouble(costoManoStr);
                Reparacion r = new Reparacion(userId, clienteId, marca, modelo, serie, desc, repuestosTxt, cRep, cMano, estado);
                r.setId(id);
                reparacionDao.update(r);
                callback.accept(new ResultadoRegistro(true, "Actualizado (Stock no modificado en edición)"));
            } catch (Exception e) {
                callback.accept(new ResultadoRegistro(false, "Error"));
            }
        });
    }

    public void eliminarReparacionPorId(int id, Consumer<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                Reparacion r = reparacionDao.getReparacionById(id, userId);
                if(r != null) { reparacionDao.delete(r); callback.accept(new ResultadoRegistro(true, "Eliminado")); }
            } catch (Exception e) { callback.accept(new ResultadoRegistro(false, "Error")); }
        });
    }
}