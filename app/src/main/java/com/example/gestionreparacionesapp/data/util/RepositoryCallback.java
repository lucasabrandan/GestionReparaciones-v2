package com.example.gestionreparacionesapp.data.util;

/**
 * Interfaz pública para manejar el callback de operaciones asíncronas entre el ViewModel y el Repository.
 */
public interface RepositoryCallback<T> {
    void onComplete(T result);
}