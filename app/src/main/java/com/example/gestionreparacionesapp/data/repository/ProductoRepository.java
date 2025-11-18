package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;

public class ProductoRepository {

    private final ProductoDao productoDao;
    private final Context context;

    // CAMBIO: El constructor ahora necesita Context
    public ProductoRepository(ProductoDao productoDao, Context context) {
        this.productoDao = productoDao;
        this.context = context;
    }

    // --- OBTENER TODOS (READ) ---

    public void getAllProductos(RepositoryCallback<List<Producto>> callback) {
        // Pasamos el contexto al AsyncTask
        new GetAllProductosAsyncTask(productoDao, callback, context).execute();
    }

    private static class GetAllProductosAsyncTask extends AsyncTask<Void, Void, List<Producto>> {
        private final ProductoDao asyncDao;
        private final RepositoryCallback<List<Producto>> callback;
        private final int userId; // <-- NUEVO: ID del usuario

        GetAllProductosAsyncTask(ProductoDao dao, RepositoryCallback<List<Producto>> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context); // Obtenemos el ID de la sesión
        }
        @Override
        protected List<Producto> doInBackground(Void... voids) {
            if (userId == -1) return null; // No hay usuario, no devolver nada
            return asyncDao.getAll(userId); // CAMBIO: Filtramos por ID
        }
        @Override
        protected void onPostExecute(List<Producto> productos) {
            if (callback != null) callback.onComplete(productos);
        }
    }

    // --- INSERTAR (CREATE) ---

    public void insertProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        new InsertProductoAsyncTask(productoDao, callback, context).execute(producto);
    }

    private static class InsertProductoAsyncTask extends AsyncTask<Producto, Void, ResultadoRegistro> {
        private final ProductoDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId; // <-- NUEVO: ID del usuario

        InsertProductoAsyncTask(ProductoDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected ResultadoRegistro doInBackground(Producto... productos) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");

            Producto nuevoProducto = productos[0];
            nuevoProducto.setUserId(userId); // ¡Asignamos el producto al usuario!

            // CAMBIO: Verificamos SKU + userId
            if (asyncDao.getBySku(nuevoProducto.getSku(), userId) != null) {
                return new ResultadoRegistro(false, "Error: El SKU ya existe para este usuario.");
            }
            long id = asyncDao.insert(nuevoProducto);
            if (id > 0) {
                return new ResultadoRegistro(true, "Producto guardado");
            } else {
                return new ResultadoRegistro(false, "Error al guardar");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) {
            if (callback != null) callback.onComplete(resultado);
        }
    }

    // --- ACTUALIZAR (UPDATE) ---

    public void updateProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        new UpdateProductoAsyncTask(productoDao, callback, context).execute(producto);
    }

    private static class UpdateProductoAsyncTask extends AsyncTask<Producto, Void, ResultadoRegistro> {
        private final ProductoDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId; // <-- NUEVO: ID del usuario

        UpdateProductoAsyncTask(ProductoDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected ResultadoRegistro doInBackground(Producto... productos) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");

            Producto producto = productos[0];
            producto.setUserId(userId); // Aseguramos que el producto pertenezca al usuario

            try {
                asyncDao.update(producto);
                return new ResultadoRegistro(true, "Producto actualizado");
            } catch (Exception e) {
                return new ResultadoRegistro(false, "Error al actualizar");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) {
            if (callback != null) callback.onComplete(resultado);
        }
    }

    // --- ELIMINAR (DELETE) ---

    public void deleteProducto(Producto producto, RepositoryCallback<ResultadoRegistro> callback) {
        new DeleteProductoAsyncTask(productoDao, callback).execute(producto);
    }

    private static class DeleteProductoAsyncTask extends AsyncTask<Producto, Void, ResultadoRegistro> {
        private final ProductoDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        DeleteProductoAsyncTask(ProductoDao dao, RepositoryCallback<ResultadoRegistro> callback) {
            this.asyncDao = dao;
            this.callback = callback;
        }
        @Override
        protected ResultadoRegistro doInBackground(Producto... productos) {
            // (DELETE no necesita filtrar por userId, ya que el objeto 'producto' tiene el ID PK)
            try {
                asyncDao.delete(productos[0]);
                return new ResultadoRegistro(true, "Producto eliminado");
            } catch (Exception e) {
                return new ResultadoRegistro(false, "Error al eliminar");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) {
            if (callback != null) callback.onComplete(resultado);
        }
    }

    // --- BUSCAR (SEARCH) ---

    public void buscarProductosPorNombre(String query, RepositoryCallback<List<Producto>> callback) {
        new BuscarProductosAsyncTask(productoDao, callback, context).execute(query);
    }

    private static class BuscarProductosAsyncTask extends AsyncTask<String, Void, List<Producto>> {
        private final ProductoDao asyncDao;
        private final RepositoryCallback<List<Producto>> callback;
        private final int userId; // <-- NUEVO: ID del usuario

        BuscarProductosAsyncTask(ProductoDao dao, RepositoryCallback<List<Producto>> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }

        @Override
        protected List<Producto> doInBackground(String... queries) {
            if (userId == -1) return null;
            return asyncDao.buscarPorNombre(queries[0], userId); // CAMBIO: Filtramos por ID
        }
        @Override
        protected void onPostExecute(List<Producto> productos) {
            if (callback != null) callback.onComplete(productos);
        }
    }
}