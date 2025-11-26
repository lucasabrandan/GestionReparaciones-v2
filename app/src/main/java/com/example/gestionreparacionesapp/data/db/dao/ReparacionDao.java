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

    @Query("SELECT * FROM reparaciones WHERE userId = :userId ORDER BY id DESC")
    List<Reparacion> getAll(int userId);

    @Query("SELECT * FROM reparaciones WHERE userId = :userId AND " +
            "(descripcionProblema LIKE '%' || :query || '%' OR " +
            "equipoModelo LIKE '%' || :query || '%') " +
            "ORDER BY id DESC")
    List<Reparacion> buscar(String query, int userId);

    @Query("SELECT * FROM reparaciones WHERE id = :id AND userId = :userId")
    Reparacion getReparacionById(int id, int userId);

    // --- NUEVO MÉTODO PARA EL PDF ---
    @Query("SELECT * FROM reparaciones WHERE clienteId = :clienteId ORDER BY id DESC")
    List<Reparacion> getAllByCliente(int clienteId);
}