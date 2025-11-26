package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta;
import java.util.List;

@Dao
public interface ProductoVentaDao {
    @Insert
    void insertAll(List<ProductoVenta> items);

    @Query("SELECT * FROM productos_venta WHERE ventaId = :ventaId")
    List<ProductoVenta> getItemsPorVenta(int ventaId);
}