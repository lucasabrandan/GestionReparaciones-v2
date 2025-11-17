package com.example.gestionreparacionesapp.data.repository;

import android.os.AsyncTask;

import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;

/**
 * Repositorio que maneja las operaciones de persistencia de Clientes.
 */
public class ClienteRepository {
    private final ClienteDao clienteDao;

    public ClienteRepository(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    // ==========================================
    // MÉTODO DE INSERCIÓN
    // ==========================================

    /**
     * Inserta un nuevo cliente de forma asíncrona.
     */
    public void insertarCliente(Cliente cliente, RepositoryCallback<InsertResult> callback) {
        new InsertClienteAsyncTask(clienteDao, callback).execute(cliente);
    }

    private static class InsertClienteAsyncTask extends AsyncTask<Cliente, Void, InsertResult> {
        private final ClienteDao asyncClienteDao;
        private final RepositoryCallback<InsertResult> callback;

        InsertClienteAsyncTask(ClienteDao dao, RepositoryCallback<InsertResult> callback) {
            this.asyncClienteDao = dao;
            this.callback = callback;
        }

        @Override
        protected InsertResult doInBackground(Cliente... clientes) {
            Cliente nuevoCliente = clientes[0];

            // Simulación: No verificamos unicidad aquí para simplificar.

            long id = asyncClienteDao.insert(nuevoCliente);

            if (id > 0) {
                return new InsertResult(true, "Cliente guardado exitosamente");
            } else {
                return new InsertResult(false, "Error al guardar el cliente");
            }
        }

        @Override
        protected void onPostExecute(InsertResult result) {
            if (callback != null) {
                callback.onComplete(result);
            }
        }
    }

    public static class InsertResult {
        public final boolean isSuccess;
        public final String message;

        public InsertResult(boolean isSuccess, String message) {
            this.isSuccess = isSuccess;
            this.message = message;
        }
    }

    // Pendiente: Agregar métodos para getAll, update, delete
}