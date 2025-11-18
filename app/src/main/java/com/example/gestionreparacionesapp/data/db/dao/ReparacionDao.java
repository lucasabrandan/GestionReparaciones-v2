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

    /**
     * Obtiene todas las reparaciones de un usuario, ordenadas por fecha descendente.
     */
    @Query("SELECT * FROM reparaciones WHERE userId = :userId ORDER BY id DESC")
    List<Reparacion> getAll(int userId);

    /**
     * Busca reparaciones de un usuario cuya descripción coincida con la consulta.
     */
    @Query("SELECT * FROM reparaciones WHERE userId = :userId AND " +
            "(descripcionProblema LIKE '%' || :query || '%' OR productoNombre LIKE '%' || :query || '%') " +
            "ORDER BY id DESC")
    List<Reparacion> buscar(String query, int userId);

    /**
     * Obtiene una reparación por su ID, asegurando que pertenezca al usuario en sesión.
     */
    @Query("SELECT * FROM reparaciones WHERE id = :id AND userId = :userId")
    Reparacion getReparacionById(int id, int userId);
}