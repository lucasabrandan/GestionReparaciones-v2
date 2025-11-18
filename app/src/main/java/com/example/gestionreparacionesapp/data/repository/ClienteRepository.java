package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
// ELIMINADOS: Geocoder, Address, IOException, Log
import android.os.AsyncTask;

import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;
import java.util.Locale;

public class ClienteRepository {

    private final ClienteDao clienteDao;
    // ELIMINADO: Geocoder
    private final Context context;

    public ClienteRepository(ClienteDao clienteDao, Context context) {
        this.clienteDao = clienteDao;
        this.context = context;
        // ELIMINADO: Inicialización de Geocoder
    }

    // --- OBTENER TODOS (READ) ---

    public void getAllClientes(RepositoryCallback<List<Cliente>> callback) {
        new GetAllClientesAsyncTask(clienteDao, callback, context).execute();
    }

    private static class GetAllClientesAsyncTask extends AsyncTask<Void, Void, List<Cliente>> {
        private final ClienteDao asyncDao;
        private final RepositoryCallback<List<Cliente>> callback;
        private final int userId;

        GetAllClientesAsyncTask(ClienteDao dao, RepositoryCallback<List<Cliente>> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Cliente> doInBackground(Void... voids) {
            if (userId == -1) return null;
            return asyncDao.getAll(userId);
        }
        @Override
        protected void onPostExecute(List<Cliente> clientes) {
            if (callback != null) callback.onComplete(clientes);
        }
    }

    // --- INSERTAR (CREATE) ---

    public void insertCliente(Cliente cliente, RepositoryCallback<ResultadoRegistro> callback) {
        new InsertClienteAsyncTask(clienteDao, callback, context).execute(cliente); // ELIMINADO: Geocoder
    }

    private static class InsertClienteAsyncTask extends AsyncTask<Cliente, Void, ResultadoRegistro> {
        private final ClienteDao asyncDao;
        // ELIMINADO: Geocoder
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId;

        InsertClienteAsyncTask(ClienteDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao;
            // ELIMINADO: Geocoder
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }

        @Override
        protected ResultadoRegistro doInBackground(Cliente... clientes) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");

            Cliente nuevoCliente = clientes[0];
            nuevoCliente.setUserId(userId);

            if (asyncDao.getByDni(nuevoCliente.getDni(), userId) != null) {
                return new ResultadoRegistro(false, "Error: El DNI ya está registrado para este usuario.");
            }

            // ELIMINADO: Bloque de Geocoder

            long id = asyncDao.insert(nuevoCliente);
            if (id > 0) {
                return new ResultadoRegistro(true, "Cliente guardado");
            } else {
                return new ResultadoRegistro(false, "Error al guardar cliente");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) {
            if (callback != null) callback.onComplete(resultado);
        }
    }

    // --- ACTUALIZAR (UPDATE) ---

    public void updateCliente(Cliente cliente, RepositoryCallback<ResultadoRegistro> callback) {
        new UpdateClienteAsyncTask(clienteDao, callback, context).execute(cliente); // ELIMINADO: Geocoder
    }

    private static class UpdateClienteAsyncTask extends AsyncTask<Cliente, Void, ResultadoRegistro> {
        private final ClienteDao asyncDao;
        // ELIMINADO: Geocoder
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId;

        UpdateClienteAsyncTask(ClienteDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao;
            // ELIMINADO: Geocoder
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected ResultadoRegistro doInBackground(Cliente... clientes) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");

            Cliente cliente = clientes[0];
            cliente.setUserId(userId);

            // ELIMINADO: Bloque de Geocoder

            try {
                asyncDao.update(cliente);
                return new ResultadoRegistro(true, "Cliente actualizado");
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

    public void deleteCliente(Cliente cliente, RepositoryCallback<ResultadoRegistro> callback) {
        new DeleteClienteAsyncTask(clienteDao, callback).execute(cliente);
    }

    private static class DeleteClienteAsyncTask extends AsyncTask<Cliente, Void, ResultadoRegistro> {
        private final ClienteDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        DeleteClienteAsyncTask(ClienteDao dao, RepositoryCallback<ResultadoRegistro> callback) {
            this.asyncDao = dao;
            this.callback = callback;
        }
        @Override
        protected ResultadoRegistro doInBackground(Cliente... clientes) {
            try {
                asyncDao.delete(clientes[0]);
                return new ResultadoRegistro(true, "Cliente eliminado");
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

    public void buscarClientesPorNombre(String query, RepositoryCallback<List<Cliente>> callback) {
        new BuscarClientesAsyncTask(clienteDao, callback, context).execute(query);
    }

    private static class BuscarClientesAsyncTask extends AsyncTask<String, Void, List<Cliente>> {
        private final ClienteDao asyncDao;
        private final RepositoryCallback<List<Cliente>> callback;
        private final int userId;

        BuscarClientesAsyncTask(ClienteDao dao, RepositoryCallback<List<Cliente>> callback, Context context) {
            this.asyncDao = dao;
            this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Cliente> doInBackground(String... queries) {
            if (userId == -1) return null;
            return asyncDao.buscarPorNombre(queries[0], userId);
        }
        @Override
        protected void onPostExecute(List<Cliente> clientes) {
            if (callback != null) callback.onComplete(clientes);
        }
    }

    // --- FILTROS DE MAPA (Limpiados) ---

    public void getClientesConVentas(RepositoryCallback<List<Cliente>> callback) {
        new GetClientesConVentasAsyncTask(clienteDao, callback, context).execute();
    }

    private static class GetClientesConVentasAsyncTask extends AsyncTask<Void, Void, List<Cliente>> {
        private final ClienteDao asyncDao;
        private final RepositoryCallback<List<Cliente>> callback;
        private final int userId;

        GetClientesConVentasAsyncTask(ClienteDao dao, RepositoryCallback<List<Cliente>> callback, Context context) {
            this.asyncDao = dao; this.callback = callback; this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Cliente> doInBackground(Void... voids) {
            if (userId == -1) return null;
            return asyncDao.getClientesConVentas(userId);
        }
        @Override
        protected void onPostExecute(List<Cliente> clientes) {
            if (callback != null) callback.onComplete(clientes);
        }
    }

    public void getClientesConReparaciones(RepositoryCallback<List<Cliente>> callback) {
        new GetClientesConReparacionesAsyncTask(clienteDao, callback, context).execute();
    }

    private static class GetClientesConReparacionesAsyncTask extends AsyncTask<Void, Void, List<Cliente>> {
        private final ClienteDao asyncDao;
        private final RepositoryCallback<List<Cliente>> callback;
        private final int userId;

        GetClientesConReparacionesAsyncTask(ClienteDao dao, RepositoryCallback<List<Cliente>> callback, Context context) {
            this.asyncDao = dao; this.callback = callback; this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Cliente> doInBackground(Void... voids) {
            if (userId == -1) return null;
            return asyncDao.getClientesConReparaciones(userId);
        }
        @Override
        protected void onPostExecute(List<Cliente> clientes) {
            if (callback != null) callback.onComplete(clientes);
        }
    }
}