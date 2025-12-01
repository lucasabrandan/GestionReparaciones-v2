package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import java.util.List;

@Dao
public interface ProductoReparacionDao {
    @Insert
    void insertAll(List<ProductoReparacion> items);

    @Query("SELECT * FROM productos_reparacion WHERE reparacionId = :reparacionId")
    List<ProductoReparacion> getItemsPorReparacion(int reparacionId);

    // --- NUEVO: Para limpiar repuestos al editar ---
    @Query("DELETE FROM productos_reparacion WHERE reparacionId = :reparacionId")
    void deleteByReparacionId(int reparacionId);
}