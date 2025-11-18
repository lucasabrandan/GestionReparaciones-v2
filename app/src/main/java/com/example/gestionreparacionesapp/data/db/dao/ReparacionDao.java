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

    /**
     * Inserta una nueva reparación en la base de datos.
     * @return el ID de la fila recién insertada.
     */
    @Insert
    long insert(Reparacion reparacion);

    /**
     * Actualiza una reparación existente.
     */
    @Update
    void update(Reparacion reparacion);

    /**
     * Elimina una reparación de la base de datos.
     */
    @Delete
    void delete(Reparacion reparacion);

    /**
     * Obtiene todas las reparaciones de un usuario específico, ordenadas por ID descendente.
     * @param userId el ID del usuario.
     * @return una lista de reparaciones.
     */
    @Query("SELECT * FROM reparaciones WHERE user_id = :userId ORDER BY id DESC")
    List<Reparacion> getAll(int userId);

    /**
     * Busca reparaciones de un usuario cuya descripción, fecha o ID coincidan con la consulta.
     * @param query la cadena de búsqueda.
     * @param userId el ID del usuario.
     * @return una lista de reparaciones que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM reparaciones WHERE user_id = :userId AND " +
            "(descripcion LIKE '%' || :query || '%' OR " +
            "fecha LIKE '%' || :query || '%' OR " +
            "CAST(id AS TEXT) LIKE '%' || :query || '%') " +
            "ORDER BY id DESC")
    List<Reparacion> buscar(String query, int userId);

    /**
     * Obtiene una reparación por su ID.
     * @param id el ID de la reparación a buscar.
     * @return la reparación encontrada o null si no existe.
     */
    @Query("SELECT * FROM reparaciones WHERE id = :id")
    Reparacion getById(int id);
}

