package com.example.gestionreparacionesapp.data.repository;

import android.os.AsyncTask;

import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoLogin;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.PasswordUtils;

public class UsuarioRepository {
    private final UsuarioDao usuarioDao;

    public UsuarioRepository(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    // ==========================================
    // 1. MÉTODOS DE REGISTRO
    // ==========================================

    public void insertarUsuario(Usuario usuario, RepositoryCallback<ResultadoRegistro> callback) {
        new InsertUsuarioAsyncTask(usuarioDao, callback).execute(usuario);
    }

    private static class InsertUsuarioAsyncTask extends AsyncTask<Usuario, Void, ResultadoRegistro> {
        private final UsuarioDao asyncUsuarioDao;
        private final RepositoryCallback<ResultadoRegistro> callback;

        InsertUsuarioAsyncTask(UsuarioDao dao, RepositoryCallback<ResultadoRegistro> callback) {
            this.asyncUsuarioDao = dao;
            this.callback = callback;
        }

        @Override
        protected ResultadoRegistro doInBackground(Usuario... usuarios) {
            Usuario nuevoUsuario = usuarios[0];
            if (asyncUsuarioDao.getByEmail(nuevoUsuario.getEmail()) != null) {
                return new ResultadoRegistro(false, "Este correo ya está registrado.");
            }

            String rawPassword = nuevoUsuario.getPassword();
            try {
                String hashedPassword = PasswordUtils.hashPassword(rawPassword);
                nuevoUsuario.setPassword(hashedPassword);
            } catch (RuntimeException e) {
                return new ResultadoRegistro(false, "Error de seguridad al registrar contraseña.");
            }

            long id = asyncUsuarioDao.insert(nuevoUsuario);

            if (id > 0) {
                return new ResultadoRegistro(true, "Registro exitoso");
            } else {
                return new ResultadoRegistro(false, "Error al registrar.");
            }
        }

        @Override
        protected void onPostExecute(ResultadoRegistro result) {
            if (callback != null) {
                callback.onComplete(result);
            }
        }
    }

    // ==========================================
    // 2. MÉTODOS DE LOGIN
    // ==========================================

    public void loginUsuario(String email, String rawPassword, RepositoryCallback<ResultadoLogin> callback) {
        new LoginUsuarioAsyncTask(usuarioDao, callback, rawPassword).execute(email);
    }

    private static class LoginUsuarioAsyncTask extends AsyncTask<String, Void, ResultadoLogin> {
        private final UsuarioDao asyncUsuarioDao;
        private final RepositoryCallback<ResultadoLogin> callback;
        private final String rawPassword;

        LoginUsuarioAsyncTask(UsuarioDao dao, RepositoryCallback<ResultadoLogin> callback, String rawPassword) {
            this.asyncUsuarioDao = dao;
            this.callback = callback;
            this.rawPassword = rawPassword;
        }

        @Override
        protected ResultadoLogin doInBackground(String... emails) {
            String email = emails[0];
            Usuario usuario = asyncUsuarioDao.getUsuarioByEmail(email);

            if (usuario == null) {
                return new ResultadoLogin(false, "Credenciales incorrectas", null);
            }

            String hashedPassword = usuario.getPassword();

            if (PasswordUtils.checkPassword(rawPassword, hashedPassword)) {
                return new ResultadoLogin(true, "Login exitoso", usuario);
            } else {
                return new ResultadoLogin(false, "Credenciales incorrectas", null);
            }
        }

        @Override
        protected void onPostExecute(ResultadoLogin result) {
            if (callback != null) {
                callback.onComplete(result);
            }
        }
    }

    // ==========================================
    // 3. MÉTODOS DE PERSISTENCIA (RECORDARME)
    // ==========================================

    public void getUsuarioRecordado(RepositoryCallback<Usuario> callback) {
        new GetUsuarioRecordadoAsyncTask(usuarioDao, callback).execute();
    }

    private static class GetUsuarioRecordadoAsyncTask extends AsyncTask<Void, Void, Usuario> {
        private final UsuarioDao asyncUsuarioDao;
        private final RepositoryCallback<Usuario> callback;

        GetUsuarioRecordadoAsyncTask(UsuarioDao dao, RepositoryCallback<Usuario> callback) {
            this.asyncUsuarioDao = dao;
            this.callback = callback;
        }

        @Override
        protected Usuario doInBackground(Void... voids) {
            return asyncUsuarioDao.getUsuarioRecordado();
        }

        @Override
        protected void onPostExecute(Usuario usuario) {
            if (callback != null) {
                callback.onComplete(usuario);
            }
        }
    }

    public void limpiarRecordarme() {
        new LimpiarRecordarmeAsyncTask(usuarioDao).execute();
    }

    private static class LimpiarRecordarmeAsyncTask extends AsyncTask<Void, Void, Void> {
        private final UsuarioDao asyncUsuarioDao;

        LimpiarRecordarmeAsyncTask(UsuarioDao dao) {
            this.asyncUsuarioDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            asyncUsuarioDao.limpiarRecordarme();
            return null;
        }
    }

    public void setRecordarme(int userId, boolean recordarme) {
        new SetRecordarmeAsyncTask(usuarioDao, userId, recordarme).execute();
    }

    private static class SetRecordarmeAsyncTask extends AsyncTask<Void, Void, Void> {
        private final UsuarioDao asyncUsuarioDao;
        private final int userId;
        private final boolean recordarme;

        SetRecordarmeAsyncTask(UsuarioDao dao, int userId, boolean recordarme) {
            this.asyncUsuarioDao = dao;
            this.userId = userId;
            this.recordarme = recordarme;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // ¡AQUÍ ES DONDE SE USA EL MÉTODO DEL DAO!
            Usuario usuario = asyncUsuarioDao.getUsuarioById(userId);
            if (usuario != null) {
                usuario.setRecordarme(recordarme);
                asyncUsuarioDao.update(usuario);
            }
            return null;
        }
    }
}