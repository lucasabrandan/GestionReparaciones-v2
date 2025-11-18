package com.example.gestionreparacionesapp.ui.productos;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import java.util.List;
import java.util.Locale;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    // Interfaz actualizada para manejar Clic (Editar) y Clic Largo (Borrar)
    private final OnProductoInteractionListener listener;

    public interface OnProductoInteractionListener {
        void onProductoClick(Producto producto); // Clic normal para Editar
        void onProductoLongClick(Producto producto); // Clic largo para Borrar
    }

    public ProductosAdapter(List<Producto> listaProductos, OnProductoInteractionListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    public void setProductos(List<Producto> productos) {
        this.listaProductos = productos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombreProducto.setText(producto.getNombre());
        holder.tvSku.setText("SKU: " + producto.getSku());
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
        holder.tvCantidad.setText(String.format(Locale.getDefault(), "Stock: %d", producto.getCantidad()));

        if (producto.getImageUri() != null) {
            holder.ivProducto.setImageURI(Uri.parse(producto.getImageUri()));
        } else {
            holder.ivProducto.setImageResource(R.drawable.ic_add_photo);
        }

        // Asignar Clic (Editar)
        holder.itemView.setOnClickListener(v -> listener.onProductoClick(producto));

        // Asignar Clic Largo (Borrar)
        holder.itemView.setOnLongClickListener(v -> {
            listener.onProductoLongClick(producto);
            return true; // Importante: retornar true para consumir el evento
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreProducto, tvSku, tvPrecio, tvCantidad;
        ImageView ivProducto;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProductoItem);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProductoItem);
            tvSku = itemView.findViewById(R.id.tvSkuItem);
            tvPrecio = itemView.findViewById(R.id.tvPrecioItem);
            tvCantidad = itemView.findViewById(R.id.tvCantidadItem);
        }
    }
}