package com.example.gestionreparacionesapp.data.util;

import com.example.gestionreparacionesapp.data.db.entity.Usuario;

/**
 * Clase estática que encapsula el resultado de la operación de Login.
 * Ahora es pública y externa al Repository, resolviendo conflictos de visibilidad.
 */
public class ResultadoLogin {
    public final boolean isSuccess;
    public final String message;
    public final Usuario usuario;

    public ResultadoLogin(boolean isSuccess, String message, Usuario usuario) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.usuario = usuario;
    }
}