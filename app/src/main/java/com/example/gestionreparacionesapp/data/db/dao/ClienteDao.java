package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestionreparacionesapp.data.db.entity.Cliente;

import java.util.List;

@Dao
public interface ClienteDao {

    @Insert
    long insert(Cliente cliente);

    @Update
    void update(Cliente cliente);

    @Delete
    void delete(Cliente cliente);

    // CAMBIO: Filtrado por Usuario
    @Query("SELECT * FROM clientes WHERE userId = :userId ORDER BY nombre ASC")
    List<Cliente> getAll(int userId);

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    Cliente getById(int id);

    // CAMBIO: DNI único por usuario
    @Query("SELECT * FROM clientes WHERE dni = :dni AND userId = :userId LIMIT 1")
    Cliente getByDni(String dni, int userId);

    // CAMBIO: Búsqueda filtrada por usuario
    @Query("SELECT * FROM clientes WHERE userId = :userId AND (nombre LIKE '%' || :query || '%' OR dni LIKE '%' || :query || '%')")
    List<Cliente> buscarPorNombre(String query, int userId);

    // CAMBIO: Filtros filtrados por usuario
    @Query("SELECT * FROM clientes WHERE userId = :userId AND id IN (SELECT DISTINCT clienteId FROM ventas)")
    List<Cliente> getClientesConVentas(int userId);

    @Query("SELECT * FROM clientes WHERE userId = :userId AND id IN (SELECT DISTINCT clienteId FROM reparaciones)")
    List<Cliente> getClientesConReparaciones(int userId);
}