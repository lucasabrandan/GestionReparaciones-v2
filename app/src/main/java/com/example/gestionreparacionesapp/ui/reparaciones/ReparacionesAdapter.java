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

    // Interfaz para manejar los clics en los elementos de la lista
    public interface OnReparacionInteractionListener {
        void onReparacionClick(Reparacion reparacion);
        void onReparacionLongClick(Reparacion reparacion);
    }

    public ReparacionesAdapter(List<Reparacion> listaReparaciones, List<Cliente> listaClientes, OnReparacionInteractionListener listener) {
        this.listaReparaciones = listaReparaciones;
        this.listaClientes = listaClientes;
        this.listener = listener;
    }

    // Métodos para actualizar las listas desde el Fragment
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
        // Infla el layout del item que diseñamos (item_reparacion.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reparacion, parent, false);
        return new ReparacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReparacionViewHolder holder, int position) {
        // Obtiene la reparación actual de la lista
        Reparacion reparacion = listaReparaciones.get(position);

        // --- LÓGICA DE VISUALIZACIÓN ACTUALIZADA ---

        // 1. Combina marca y modelo para el título principal
        String equipo = reparacion.getEquipoMarca() + " " + reparacion.getEquipoModelo();
        holder.tvEquipo.setText(equipo.trim().isEmpty() ? "Equipo no especificado" : equipo);

        // 2. Muestra la descripción del problema
        holder.tvDescripcion.setText(reparacion.getDescripcionProblema());

        // 3. Muestra el presupuesto total calculado desde la entidad
        holder.tvPresupuesto.setText(String.format(Locale.getDefault(), "$%.2f", reparacion.getPresupuestoTotal()));

        // 4. Muestra el estado y cambia el color del fondo dinámicamente
        holder.tvEstado.setText(reparacion.getEstado());
        switch (reparacion.getEstado() != null ? reparacion.getEstado() : "Pendiente") {
            case "En Proceso":
                holder.tvEstado.setBackgroundResource(R.drawable.background_estado_en_proceso);
                break;
            case "Finalizado":
                holder.tvEstado.setBackgroundResource(R.drawable.background_estado_finalizado);
                break;
            case "Pendiente":
            default:
                holder.tvEstado.setBackgroundResource(R.drawable.background_estado_pendiente);
                break;
        }

        // 5. Busca y muestra el nombre del cliente
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

        // 6. Asigna los listeners para el clic corto y largo
        holder.itemView.setOnClickListener(v -> listener.onReparacionClick(reparacion));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onReparacionLongClick(reparacion);
            return true; // Indica que el evento ha sido consumido
        });
    }

    @Override
    public int getItemCount() {
        return listaReparaciones != null ? listaReparaciones.size() : 0;
    }

    // El ViewHolder que contiene las vistas de cada item
    static class ReparacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente, tvEquipo, tvDescripcion, tvPresupuesto, tvEstado;

        public ReparacionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vincula las variables con los IDs del layout item_reparacion.xml
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPresupuesto = itemView.findViewById(R.id.tvPresupuesto);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}