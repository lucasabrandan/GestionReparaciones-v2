package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import java.util.List;

@Dao
public interface VentaDao {

    @Insert
    long insert(Venta venta);

    @Update
    void update(Venta venta);

    @Delete
    void delete(Venta venta);

    // CAMBIO: Filtrado por Usuario
    @Query("SELECT * FROM ventas WHERE userId = :userId ORDER BY id DESC")
    List<Venta> getAll(int userId);

    @Query("SELECT * FROM ventas WHERE id = :id LIMIT 1")
    Venta getById(int id);

    // Mantenemos los otros métodos, pero también los filtramos por usuario
    @Query("SELECT * FROM ventas WHERE clienteId = :clienteId AND userId = :userId ORDER BY fecha DESC")
    List<Venta> getByCliente(int clienteId, int userId);

    @Query("SELECT * FROM ventas WHERE fecha = :fecha AND userId = :userId ORDER BY id DESC")
    List<Venta> getByFecha(String fecha, int userId);
}