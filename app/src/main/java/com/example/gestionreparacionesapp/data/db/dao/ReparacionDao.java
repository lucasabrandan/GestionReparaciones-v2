package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestionreparacionesapp.data.db.entity.Reparacion;

import java.util.List;

@Dao
public interface ReparacionDao {

    @Insert
    long insert(Reparacion reparacion);

    @Update
    void update(Reparacion reparacion);

    @Delete
    void delete(Reparacion reparacion);

    // CAMBIO: Filtrado por Usuario
    @Query("SELECT * FROM reparaciones WHERE userId = :userId ORDER BY id DESC")
    List<Reparacion> getAll(int userId);

    @Query("SELECT * FROM reparaciones WHERE id = :id LIMIT 1")
    Reparacion getById(int id);

    // CAMBIO: Filtrado por Usuario
    @Query("SELECT * FROM reparaciones WHERE clienteId = :clienteId AND userId = :userId ORDER BY fecha DESC")
    List<Reparacion> getByCliente(int clienteId, int userId);

    // CAMBIO: Búsqueda filtrada por usuario
    @Query("SELECT * FROM reparaciones WHERE userId = :userId AND (descripcion LIKE '%' || :query || '%' OR fecha LIKE '%' || :query || '%') ORDER BY id DESC")
    List<Reparacion> buscar(String query, int userId);
}