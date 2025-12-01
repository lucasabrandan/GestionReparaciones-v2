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
import java.util.List;
import java.util.Locale;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.VentaViewHolder> {

    private List<Venta> listaVentas;
    private List<Cliente> listaClientes;
    private final OnVentaInteractionListener listener;

    public interface OnVentaInteractionListener {
        void onVentaClick(Venta venta);
    }

    public VentasAdapter(List<Venta> listaVentas, List<Cliente> listaClientes, OnVentaInteractionListener listener) {
        this.listaVentas = listaVentas;
        this.listaClientes = listaClientes;
        this.listener = listener;
    }

    public void setVentas(List<Venta> ventas) {
        this.listaVentas = ventas;
        notifyDataSetChanged();
    }

    public void setClientes(List<Cliente> clientes) {
        this.listaClientes = clientes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta, parent, false);
        return new VentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaViewHolder holder, int position) {
        Venta venta = listaVentas.get(position);

        // Buscar nombre cliente
        String nombreCliente = "Cliente Desconocido";
        if(listaClientes != null) {
            for(Cliente c : listaClientes) {
                if(c.getId() == venta.getClienteId()) {
                    nombreCliente = c.getNombre();
                    break;
                }
            }
        }

        holder.tvFecha.setText(nombreCliente);
        holder.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", venta.getTotal()));

        holder.itemView.setOnClickListener(v -> listener.onVentaClick(venta));
    }

    @Override
    public int getItemCount() {
        return listaVentas != null ? listaVentas.size() : 0;
    }

    static class VentaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvTotal;
        public VentaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Usamos tvFechaVenta para mostrar el nombre (por consistencia con el layout anterior)
            tvFecha = itemView.findViewById(R.id.tvFechaVenta);
            tvTotal = itemView.findViewById(R.id.tvTotalVenta);
        }
    }
}