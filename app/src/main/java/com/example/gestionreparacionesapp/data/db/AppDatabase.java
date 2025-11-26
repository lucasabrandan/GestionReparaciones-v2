package com.example.gestionreparacionesapp.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoReparacionDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoVentaDao; // IMPORTANTE
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.dao.VentaDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta; // IMPORTANTE
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.db.entity.Venta;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Usuario.class,
        Cliente.class,
        Producto.class,
        Venta.class,
        Reparacion.class,
        ProductoReparacion.class,
        ProductoVenta.class // <--- AGREGADO
}, version = 20, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract ClienteDao clienteDao();
    public abstract ProductoDao productoDao();
    public abstract VentaDao ventaDao();
    public abstract ReparacionDao reparacionDao();
    public abstract ProductoReparacionDao productoReparacionDao();

    // ESTE ES EL MÉTODO QUE FALTABA O DABA ERROR:
    public abstract ProductoVentaDao productoVentaDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "gestion_reparaciones_db";
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}