package com.example.gestionreparacionesapp.ui.clientes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;

import java.util.List;

public class ClientesAdapter extends RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder> {

    private List<Cliente> listaClientes;
    private final OnClienteInteractionListener listener;

    public interface OnClienteInteractionListener {
        void onClienteClick(Cliente cliente);
        void onClienteLongClick(Cliente cliente);
    }

    public ClientesAdapter(List<Cliente> listaClientes, OnClienteInteractionListener listener) {
        this.listaClientes = listaClientes;
        this.listener = listener;
    }

    public void setClientes(List<Cliente> clientes) {
        this.listaClientes = clientes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = listaClientes.get(position);
        holder.tvNombreCliente.setText(cliente.getNombre());
        holder.tvDni.setText("DNI: " + cliente.getDni()); // Mostrar DNI
        holder.tvDireccion.setText(cliente.getDireccion());
        holder.tvLocalidadYCP.setText(cliente.getLocalidad() + " (" + cliente.getCodigoPostal() + ")");

        holder.itemView.setOnClickListener(v -> listener.onClienteClick(cliente));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onClienteLongClick(cliente);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaClientes != null ? listaClientes.size() : 0;
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCliente, tvDni, tvDireccion, tvLocalidadYCP;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCliente = itemView.findViewById(R.id.tvNombreClienteItem);
            tvDni = itemView.findViewById(R.id.tvDniItem); // Vincular DNI
            tvDireccion = itemView.findViewById(R.id.tvDireccionItem);
            tvLocalidadYCP = itemView.findViewById(R.id.tvLocalidadYCPItem);
        }
    }
}