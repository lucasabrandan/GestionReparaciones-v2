package com.example.gestionreparacionesapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Maneja la sesión del usuario (guarda el ID del usuario logueado).
 * Esto es clave para la arquitectura Multi-Tenant (multi-usuario).
 */
public class SessionManager {

    private static final String PREF_NAME = "GestionReparacionesPref";
    private static final String KEY_USER_ID = "user_id";

    /**
     * Guarda el ID del usuario logueado en SharedPreferences.
     * Llamado por LoginActivity al iniciar sesión.
     */
    public static void saveUserId(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Obtiene el ID del usuario logueado.
     * Llamado por los Repositories para filtrar los datos.
     * Devuelve -1 si no hay ningún usuario logueado.
     */
    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Devuelve -1 si KEY_USER_ID no existe
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Limpia la sesión guardada (usado en Logout).
     * Llamado por HomeActivity al cerrar sesión.
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();
    }
}