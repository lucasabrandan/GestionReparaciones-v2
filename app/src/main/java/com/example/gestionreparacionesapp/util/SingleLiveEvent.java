package com.example.gestionreparacionesapp.util;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Un LiveData optimizado que solo envía la actualización una vez.
 * Útil para eventos de navegación, Toasts, Snackbars, etc.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Múltiples observadores registrados pero solo uno será notificado de los cambios.");
        }

        // Observar el MutableLiveData interno
        super.observe(owner, t -> {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Usado para llamadas desde hilos de fondo (Repository).
     */
    public void postValue(@Nullable T t) {
        mPending.set(true);
        super.postValue(t);
    }

    /**
     * Método de conveniencia para cuando no necesitas pasar datos, solo avisar (Void).
     * Ejemplo: Un botón que solo navega sin pasar argumentos.
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}