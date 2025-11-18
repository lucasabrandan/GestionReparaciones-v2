package com.example.gestionreparacionesapp.ui.ventas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Venta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.VentaViewHolder> {

    private List<Venta> listaVentas;
    private List<Cliente> listaClientes;
    private final OnVentaInteractionListener listener;

    // --- INTERFAZ AÑADIDA PARA MANEJAR CLICS ---
    public interface OnVentaInteractionListener {
        void onVentaClick(Venta venta); // Para editar
        void onVentaLongClick(Venta venta); // Para borrar
    }
    // ------------------------------------------

    // --- CONSTRUCTOR MODIFICADO ---
    public VentasAdapter(List<Venta> listaVentas, List<Cliente> listaClientes, OnVentaInteractionListener listener) {
        this.listaVentas = listaVentas;
        this.listaClientes = listaClientes;
        this.listener = listener;
    }
    // ----------------------------

    @NonNull
    @Override
    public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta, parent, false);
        return new VentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaViewHolder holder, int position) {
        Venta venta = listaVentas.get(position);

        holder.tvVentaId.setText(String.format("VENTA #%03d", venta.getId()));

        String fecha = venta.getFecha();
        if (fecha != null && fecha.contains(" ")) {
            fecha = fecha.split(" ")[0]; // Solo la fecha, sin hora
        }
        holder.tvFecha.setText(fecha);

        String nombreCliente = "Cliente desconocido";
        if (listaClientes != null) {
            for (Cliente c : listaClientes) {
                if (c.getId() == venta.getClienteId()) {
                    nombreCliente = c.getNombre();
                    break;
                }
            }
        }
        holder.tvCliente.setText(nombreCliente);

        String productosTexto = parsearProductos(venta.getProductosJson());
        holder.tvProductos.setText(productosTexto);

        holder.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", venta.getTotal()));

        // --- LISTENERS AÑADIDOS PARA CLIC Y CLIC LARGO ---
        holder.itemView.setOnClickListener(v -> listener.onVentaClick(venta));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onVentaLongClick(venta);
            return true; // Importante: consume el evento
        });
        // -------------------------------------------------
    }

    @Override
    public int getItemCount() {
        return listaVentas != null ? listaVentas.size() : 0;
    }

    private String parsearProductos(String productosJson) {
        if (productosJson == null) return "No hay productos";
        StringBuilder resultado = new StringBuilder();
        try {
            JSONArray array = new JSONArray(productosJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject producto = array.getJSONObject(i);
                String nombre = producto.optString("nombre", "N/A");
                int cantidad = producto.optInt("cantidad", 0);
                resultado.append("• ").append(nombre).append(" x").append(cantidad);
                if (i < array.length() - 1) {
                    resultado.append("\n");
                }
            }
        } catch (Exception e) {
            resultado.append("Error al cargar productos");
        }
        return resultado.toString();
    }

    public void setVentasYClientes(List<Venta> nuevasVentas, List<Cliente> todosLosClientes) {
        if (this.listaVentas != null) {
            this.listaVentas.clear();
            this.listaVentas.addAll(nuevasVentas);
        }
        if (this.listaClientes != null) {
            this.listaClientes.clear();
            this.listaClientes.addAll(todosLosClientes);
        }
        notifyDataSetChanged();
    }

    static class VentaViewHolder extends RecyclerView.ViewHolder {
        TextView tvVentaId, tvFecha, tvCliente, tvProductos, tvTotal;

        public VentaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVentaId = itemView.findViewById(R.id.tvVentaId);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvProductos = itemView.findViewById(R.id.tvProductos);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}
