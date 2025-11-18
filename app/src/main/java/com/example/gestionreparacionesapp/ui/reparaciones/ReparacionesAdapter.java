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

import java.util.List;
import java.util.Locale;

public class ReparacionesAdapter extends RecyclerView.Adapter<ReparacionesAdapter.ReparacionViewHolder> {

    private List<Reparacion> listaReparaciones;
    private List<Cliente> listaClientes;
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

    public void setReparaciones(List<Reparacion> reparaciones) {
        this.listaReparaciones = reparaciones;
        notifyDataSetChanged();
    }

    public void setClientes(List<Cliente> clientes) {
        this.listaClientes = clientes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReparacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que tu layout se llama 'item_reparacion.xml'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reparacion, parent, false);
        return new ReparacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReparacionViewHolder holder, int position) {
        Reparacion reparacion = listaReparaciones.get(position);

        // --- CÓDIGO SIMPLE Y FUNCIONAL ---
        // Usamos los getters de la entidad Reparacion que SÍ existen.
        holder.tvProducto.setText(reparacion.getProductoNombre());
        holder.tvDescripcion.setText(reparacion.getDescripcionProblema());
        holder.tvPresupuesto.setText(String.format(Locale.getDefault(), "Presupuesto: $%.2f", reparacion.getPresupuesto()));
        holder.tvEstado.setText(String.format("Estado: %s", reparacion.getEstado()));

        String nombreCliente = "Cliente no encontrado";
        if (listaClientes != null) {
            for (Cliente cliente : listaClientes) {
                if (cliente.getId() == reparacion.getClienteId()) {
                    nombreCliente = cliente.getNombre();
                    break;
                }
            }
        }
        holder.tvCliente.setText(nombreCliente);

        // Listeners para interacción
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

    // El ViewHolder ahora es mucho más simple y coincide con 'item_reparacion.xml'
    static class ReparacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente, tvProducto, tvDescripcion, tvPresupuesto, tvEstado;

        public ReparacionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de que estos IDs existen en tu 'item_reparacion.xml'
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvProducto = itemView.findViewById(R.id.tvProducto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPresupuesto = itemView.findViewById(R.id.tvPresupuesto);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}