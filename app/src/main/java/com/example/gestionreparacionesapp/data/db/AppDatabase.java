package com.example.gestionreparacionesapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.dao.VentaDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.db.entity.Venta;

/**
 * Clase abstracta que define la configuración de la base de datos Room.
 * Incluye todas las entidades y DAOs necesarios para la aplicación.
 * Implementa el patrón Singleton.
 */
@Database(entities = {
        Usuario.class,
        Cliente.class,
        Producto.class,
        Venta.class,
        Reparacion.class
}, version = 2, exportSchema = false) // <--- ¡VERSIÓN INCREMENTADA A 2!
public abstract class AppDatabase extends RoomDatabase {

    // Lista de DAOs que Room debe proporcionar
    public abstract UsuarioDao usuarioDao();
    public abstract ClienteDao clienteDao();
    public abstract ProductoDao productoDao();
    public abstract VentaDao ventaDao();
    public abstract ReparacionDao reparacionDao();

    // Usamos 'volatile' para garantizar la visibilidad del Singleton
    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "gestion_reparaciones_db";

    /**
     * Devuelve la instancia Singleton de la base de datos.
     */
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            // Borra la DB antigua porque cambiamos la versión
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}