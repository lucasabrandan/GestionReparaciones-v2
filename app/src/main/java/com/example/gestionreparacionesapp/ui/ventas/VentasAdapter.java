package com.example.gestionreparacionesapp.ui.ventas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Venta; // Entidad Correcta
import java.util.List;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.VentaViewHolder> {

    private List<Venta> listaVentas;
    private final OnVentaInteractionListener listener;

    public interface OnVentaInteractionListener {
        void onVentaClick(Venta venta);
    }

    public VentasAdapter(List<Venta> listaVentas, OnVentaInteractionListener listener) {
        this.listaVentas = listaVentas;
        this.listener = listener;
    }

    public void setVentas(List<Venta> ventas) {
        this.listaVentas = ventas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta, parent, false); // Crea item_venta.xml si no existe
        return new VentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaViewHolder holder, int position) {
        Venta venta = listaVentas.get(position);
        holder.tvFecha.setText(venta.getFecha());
        holder.tvTotal.setText("Total: $" + venta.getTotal());
        // holder.tvCliente.setText(...) -> Necesitarías hacer una query extra o guardar el nombre del cliente en la venta para mostrarlo directo

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
            tvFecha = itemView.findViewById(R.id.tvFechaVenta); // Asegúrate IDs en XML
            tvTotal = itemView.findViewById(R.id.tvTotalVenta);
        }
    }
}