package com.example.gestionreparacionesapp.data.util;

/**
 * Clase estática que encapsula el resultado de la operación de Registro.
 * Ahora es pública y externa al Repository.
 */
public class ResultadoRegistro {
    public final boolean isSuccess;
    public final String message;

    public ResultadoRegistro(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}