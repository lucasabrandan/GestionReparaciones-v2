package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestionreparacionesapp.data.db.entity.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    @Insert
    long insert(Producto producto);

    @Update
    void update(Producto producto);

    @Delete
    void delete(Producto producto);

    // CAMBIO: Ahora solo trae los del usuario logueado
    @Query("SELECT * FROM productos WHERE userId = :userId ORDER BY nombre ASC")
    List<Producto> getAll(int userId);

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    Producto getById(int id);

    // CAMBIO: El SKU debe ser único por usuario
    @Query("SELECT * FROM productos WHERE sku = :sku AND userId = :userId LIMIT 1")
    Producto getBySku(String sku, int userId);

    // CAMBIO: La búsqueda ahora se filtra por usuario
    @Query("SELECT * FROM productos WHERE userId = :userId AND (nombre LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')")
    List<Producto> buscarPorNombre(String query, int userId);

    // CAMBIO: Los disponibles también se filtran por usuario
    @Query("SELECT * FROM productos WHERE cantidad > 0 AND userId = :userId ORDER BY nombre ASC")
    List<Producto> getDisponibles(int userId);
}