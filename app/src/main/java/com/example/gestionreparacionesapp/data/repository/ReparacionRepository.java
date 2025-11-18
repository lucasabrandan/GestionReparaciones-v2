package com.example.gestionreparacionesapp.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro; // Reusamos esta clase
import com.example.gestionreparacionesapp.util.SessionManager;

import java.util.List;

/**
 * Repositorio para la entidad Reparacion. Maneja la asincronía y el filtrado por usuario.
 */
public class ReparacionRepository {

    private final ReparacionDao reparacionDao;
    private final Context context;

    public ReparacionRepository(ReparacionDao reparacionDao, Context context) {
        this.reparacionDao = reparacionDao;
        this.context = context;
    }

    // --- OBTENER TODAS (READ) ---
    public void getAllReparaciones(RepositoryCallback<List<Reparacion>> callback) {
        new GetAllReparacionesAsyncTask(reparacionDao, callback, context).execute();
    }

    private static class GetAllReparacionesAsyncTask extends AsyncTask<Void, Void, List<Reparacion>> {
        private final ReparacionDao asyncDao;
        private final RepositoryCallback<List<Reparacion>> callback;
        private final int userId;

        GetAllReparacionesAsyncTask(ReparacionDao dao, RepositoryCallback<List<Reparacion>> callback, Context context) {
            this.asyncDao = dao; this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Reparacion> doInBackground(Void... voids) {
            if (userId == -1) return null;
            return asyncDao.getAll(userId);
        }
        @Override
        protected void onPostExecute(List<Reparacion> result) { if (callback != null) callback.onComplete(result); }
    }

    // --- INSERTAR (CREATE) ---
    public void insertReparacion(Reparacion reparacion, RepositoryCallback<ResultadoRegistro> callback) {
        new InsertReparacionAsyncTask(reparacionDao, callback, context).execute(reparacion);
    }

    private static class InsertReparacionAsyncTask extends AsyncTask<Reparacion, Void, ResultadoRegistro> {
        private final ReparacionDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId;

        InsertReparacionAsyncTask(ReparacionDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao; this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected ResultadoRegistro doInBackground(Reparacion... reparaciones) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");
            reparaciones[0].setUserId(userId); // Asignamos al usuario

            long id = asyncDao.insert(reparaciones[0]);
            if (id > 0) {
                return new ResultadoRegistro(true, "Reparación guardada");
            } else {
                return new ResultadoRegistro(false, "Error al guardar reparación");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) { if (callback != null) callback.onComplete(resultado); }
    }

    // --- ACTUALIZAR (UPDATE) ---
    public void updateReparacion(Reparacion reparacion, RepositoryCallback<ResultadoRegistro> callback) {
        new UpdateReparacionAsyncTask(reparacionDao, callback, context).execute(reparacion);
    }

    private static class UpdateReparacionAsyncTask extends AsyncTask<Reparacion, Void, ResultadoRegistro> {
        private final ReparacionDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        private final int userId;

        UpdateReparacionAsyncTask(ReparacionDao dao, RepositoryCallback<ResultadoRegistro> callback, Context context) {
            this.asyncDao = dao; this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected ResultadoRegistro doInBackground(Reparacion... reparaciones) {
            if (userId == -1) return new ResultadoRegistro(false, "Error de sesión");
            reparaciones[0].setUserId(userId); // Aseguramos el userId

            try {
                asyncDao.update(reparaciones[0]);
                return new ResultadoRegistro(true, "Reparación actualizada");
            } catch (Exception e) {
                return new ResultadoRegistro(false, "Error al actualizar");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) { if (callback != null) callback.onComplete(resultado); }
    }

    // --- ELIMINAR (DELETE) ---
    public void deleteReparacion(Reparacion reparacion, RepositoryCallback<ResultadoRegistro> callback) {
        new DeleteReparacionAsyncTask(reparacionDao, callback).execute(reparacion);
    }

    private static class DeleteReparacionAsyncTask extends AsyncTask<Reparacion, Void, ResultadoRegistro> {
        private final ReparacionDao asyncDao;
        private final RepositoryCallback<ResultadoRegistro> callback;
        DeleteReparacionAsyncTask(ReparacionDao dao, RepositoryCallback<ResultadoRegistro> callback) {
            this.asyncDao = dao; this.callback = callback;
        }
        @Override
        protected ResultadoRegistro doInBackground(Reparacion... reparaciones) {
            try {
                asyncDao.delete(reparaciones[0]);
                return new ResultadoRegistro(true, "Reparación eliminada");
            } catch (Exception e) {
                return new ResultadoRegistro(false, "Error al eliminar");
            }
        }
        @Override
        protected void onPostExecute(ResultadoRegistro resultado) { if (callback != null) callback.onComplete(resultado); }
    }

    // --- BUSCAR (SEARCH) ---
    public void buscarReparaciones(String query, RepositoryCallback<List<Reparacion>> callback) {
        new BuscarReparacionesAsyncTask(reparacionDao, callback, context).execute(query);
    }

    private static class BuscarReparacionesAsyncTask extends AsyncTask<String, Void, List<Reparacion>> {
        private final ReparacionDao asyncDao;
        private final RepositoryCallback<List<Reparacion>> callback;
        private final int userId;

        BuscarReparacionesAsyncTask(ReparacionDao dao, RepositoryCallback<List<Reparacion>> callback, Context context) {
            this.asyncDao = dao; this.callback = callback;
            this.userId = SessionManager.getUserId(context);
        }
        @Override
        protected List<Reparacion> doInBackground(String... queries) {
            if (userId == -1) return null;
            return asyncDao.buscar(queries[0], userId);
        }
        @Override
        protected void onPostExecute(List<Reparacion> result) { if (callback != null) callback.onComplete(result); }
    }
}