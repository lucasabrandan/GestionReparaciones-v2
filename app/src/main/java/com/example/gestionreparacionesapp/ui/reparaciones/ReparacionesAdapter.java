package com.example.gestionreparacionesapp.ui.reparaciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class ReparacionesAdapter extends RecyclerView.Adapter<ReparacionesAdapter.ReparacionViewHolder> {

    private List<Reparacion> listaReparaciones;
    private List<Cliente> listaClientes; // Necesaria para mostrar el nombre del cliente
    private final OnReparacionInteractionListener listener;

    public interface OnReparacionInteractionListener {
        void onReparacionClick(Reparacion reparacion);
        void onReparacionLongClick(Reparacion reparacion);
    }

    public ReparacionesAdapter(List<Reparacion> listaReparaciones, List<Cliente> listaClientes, OnReparacionInteractionListener listener) {
        this.listaReparaciones = listaReparaciones;
        this.listaClientes = listaClientes;
        this.listener = listener;
    }

    /**
     * Actualiza la lista de reparaciones.
     */
    public void setReparaciones(List<Reparacion> reparaciones) {
        this.listaReparaciones = reparaciones;
        notifyDataSetChanged();
    }

    /**
     * Actualiza la lista de clientes (necesaria para los nombres).
     */
    public void setClientes(List<Cliente> clientes) {
        this.listaClientes = clientes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReparacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reparacion, parent, false);
        return new ReparacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReparacionViewHolder holder, int position) {
        Reparacion reparacion = listaReparaciones.get(position);

        holder.tvReparacionId.setText(String.format(Locale.getDefault(), "REPARACIÓN #%03d", reparacion.getId()));

        String fecha = reparacion.getFecha();
        if (fecha.contains(" ")) {
            fecha = fecha.split(" ")[0]; // Solo fecha
        }
        holder.tvFecha.setText(fecha);

        // Buscar el nombre del cliente usando el clienteId
        String nombreCliente = "Cliente (ID: " + reparacion.getClienteId() + ")";
        if (listaClientes != null) {
            for (Cliente c : listaClientes) {
                if (c.getId() == reparacion.getClienteId()) {
                    nombreCliente = c.getNombre();
                    break;
                }
            }
        }
        holder.tvCliente.setText(nombreCliente);

        // Mostrar descripción o productos
        if (reparacion.getDescripcion() != null && !reparacion.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(reparacion.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        // Parsear JSON de productos
        String productosTexto = parsearProductos(reparacion.getProductosJson());
        if (!productosTexto.isEmpty()) {
            holder.tvProductos.setText(productosTexto);
            holder.tvProductos.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductos.setVisibility(View.GONE);
        }

        holder.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", reparacion.getTotal()));

        holder.itemView.setOnClickListener(v -> listener.onReparacionClick(reparacion));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onReparacionLongClick(reparacion);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaReparaciones != null ? listaReparaciones.size() : 0;
    }

    private String parsearProductos(String productosJson) {
        StringBuilder resultado = new StringBuilder();
        try {
            JSONArray array = new JSONArray(productosJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject producto = array.getJSONObject(i);
                String nombre = producto.getString("nombre");
                int cantidad = producto.getInt("cantidad");
                resultado.append("• ").append(nombre).append(" (x").append(cantidad).append(")");
                if (i < array.length() - 1) {
                    resultado.append("\n");
                }
            }
        } catch (Exception e) {
            // No hacer nada si el JSON está vacío o es inválido
        }
        return resultado.toString();
    }

    static class ReparacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvReparacionId, tvFecha, tvCliente, tvDescripcion, tvProductos, tvTotal;

        public ReparacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReparacionId = itemView.findViewById(R.id.tvReparacionId);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvProductos = itemView.findViewById(R.id.tvProductos);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}