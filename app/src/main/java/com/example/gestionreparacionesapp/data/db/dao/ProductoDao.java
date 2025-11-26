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

    // Obtener todos los productos del usuario
    @Query("SELECT * FROM productos WHERE userId = :userId ORDER BY nombre ASC")
    List<Producto> getAll(int userId);

    // Buscar productos por nombre o SKU
    @Query("SELECT * FROM productos WHERE userId = :userId AND (nombre LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')")
    List<Producto> buscar(String query, int userId);

    // Obtener por SKU (para validaciones)
    @Query("SELECT * FROM productos WHERE sku = :sku AND userId = :userId LIMIT 1")
    Producto getBySku(String sku, int userId);

    // Obtener disponibles (Stock > 0)
    @Query("SELECT * FROM productos WHERE cantidad > 0 AND userId = :userId ORDER BY nombre ASC")
    List<Producto> getDisponibles(int userId);

    // --- ESTE ES EL MÉTODO QUE CAUSABA EL ERROR ---
    // Ahora acepta 2 parámetros: id y userId
    @Query("SELECT * FROM productos WHERE id = :id AND userId = :userId LIMIT 1")
    Producto getById(int id, int userId);
}