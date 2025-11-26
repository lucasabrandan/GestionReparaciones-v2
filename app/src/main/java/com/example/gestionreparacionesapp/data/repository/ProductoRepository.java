package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductoRepository {

    private final ProductoDao productoDao;
    private final ExecutorService executorService;
    private final int userId;

    public ProductoRepository(ProductoDao productoDao, Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.productoDao = productoDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.userId = SessionManager.getUserId(context);
    }

    // --- OBTENER TODOS (READ) ---
    public void getAllProductos(RepositoryCallback<List<Producto>> callback) {
        executorService.execute(() -> {
            if (userId != -1) {
                List<Producto> productos = productoDao.getAll(userId);
                if (callback != null) callback.onComplete(productos);
            }
        });
    }

    // --- BUSCAR (SEARCH) ---
    public void buscarProductosPorNombre(String query, RepositoryCallback<List<Producto>> callback) {
        executorService.execute(() -> {
            if (userId != -1) {
                // CORRECCIÓN: Llamamos a 'buscar', que es como se llama en el DAO
                List<Producto> productos = productoDao.buscar(query, userId);
                if (callback != null) callback.onComplete(productos);
            }
        });
    }

    // --- INSERTAR (CREATE) ---
    public void insertProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            if (userId == -1) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error de sesión"));
                return;
            }

            try {
                producto.setUserId(userId);

                // Verificar SKU único
                if (productoDao.getBySku(producto.getSku(), userId) != null) {
                    if (callback != null) callback.onComplete(new ResultadoRegistro(false, "El SKU ya existe."));
                    return;
                }

                long id = productoDao.insert(producto);
                if (id > 0) {
                    // Devolvemos el ID en el mensaje para que el ViewModel pueda usarlo
                    if (callback != null) callback.onComplete(new ResultadoRegistro(true, String.valueOf(id)));
                } else {
                    if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al guardar"));
                }
            } catch (Exception e) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error: " + e.getMessage()));
            }
        });
    }

    // --- ACTUALIZAR (UPDATE) ---
    public void updateProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                producto.setUserId(userId); // Asegurar pertenencia
                productoDao.update(producto);
                if (callback != null) callback.onComplete(new ResultadoRegistro(true, "Producto actualizado"));
            } catch (Exception e) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al actualizar"));
            }
        });
    }

    // --- ELIMINAR (DELETE) ---
    public void deleteProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        executorService.execute(() -> {
            try {
                productoDao.delete(producto);
                if (callback != null) callback.onComplete(new ResultadoRegistro(true, "Producto eliminado"));
            } catch (Exception e) {
                if (callback != null) callback.onComplete(new ResultadoRegistro(false, "Error al eliminar"));
            }
        });
    }
}