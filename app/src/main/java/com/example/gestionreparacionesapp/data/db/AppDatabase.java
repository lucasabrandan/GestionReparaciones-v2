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


@Database(entities = {
        Usuario.class,
        Cliente.class,
        Producto.class,
        Venta.class,
        Reparacion.class
}, version = 15, exportSchema = false) // <--- ¡VERSIÓN INCREMENTADA A 15!
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract ClienteDao clienteDao();
    public abstract ProductoDao productoDao();
    public abstract VentaDao ventaDao();
    public abstract ReparacionDao reparacionDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "gestion_reparaciones_db";

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            // Borrará la DB v14 y creará la v15
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}