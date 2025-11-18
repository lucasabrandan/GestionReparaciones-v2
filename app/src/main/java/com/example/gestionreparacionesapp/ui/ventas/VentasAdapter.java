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

    private List<Venta> ventas;
    private List<Cliente> clientes;
    private final OnVentaInteractionListener listener;

    public VentasAdapter(List<Venta> ventas, List<Cliente> clientes, OnVentaInteractionListener listener) {
        this.ventas = ventas;
        this.clientes = clientes;
        this.listener = listener;
    }

    // --- MÉTODO QUE FALTA ---
    // Añade este método completo a tu clase
    public void setVentas(List<Venta> nuevasVentas) {
        this.ventas.clear();
        this.ventas.addAll(nuevasVentas);
        notifyDataSetChanged();
    }

    // También es buena práctica tener un método para actualizar los clientes
    public void setClientes(List<Cliente> nuevosClientes) {
        this.clientes.clear();
        this.clientes.addAll(nuevosClientes);
        notifyDataSetChanged(); // Es importante notificar para que se actualicen los nombres
    }
    // --- FIN DE LOS MÉTODOS A AÑADIR ---


    @NonNull
    @Override
    public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta, parent, false);
        return new VentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaViewHolder holder, int position) {
        Venta venta = ventas.get(position);
        holder.bind(venta, listener);
    }

    @Override
    public int getItemCount() {
        return ventas != null ? ventas.size() : 0;
    }

    // Clase interna ViewHolder
    class VentaViewHolder extends RecyclerView.ViewHolder {
        // IDs de tu item_venta.xml
        private TextView tvVentaId, tvClienteNombre, tvFecha, tvTotal;

        public VentaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVentaId = itemView.findViewById(R.id.tvVentaId); // Asegúrate de que este ID exista
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombreVenta); // Asegúrate de que este ID exista
            tvFecha = itemView.findViewById(R.id.tvFechaVenta); // Asegúrate de que este ID exista
            tvTotal = itemView.findViewById(R.id.tvTotalVenta); // Asegúrate de que este ID exista
        }

        public void bind(final Venta venta, final OnVentaInteractionListener listener) {
            tvVentaId.setText(String.format(Locale.getDefault(), "Venta #%d", venta.getId()));
            tvFecha.setText(venta.getFecha());
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", venta.getTotal()));

            // Buscar nombre del cliente
            String nombreCliente = "Cliente no encontrado";
            for (Cliente cliente : clientes) {
                if (cliente.getId() == venta.getClienteId()) {
                    nombreCliente = cliente.getNombre();
                    break;
                }
            }
            tvClienteNombre.setText(nombreCliente);

            itemView.setOnClickListener(v -> listener.onVentaClick(venta));
            itemView.setOnLongClickListener(v -> {
                listener.onVentaLongClick(venta);
                return true;
            });
        }
    }

    // Interfaz de interacción
    public interface OnVentaInteractionListener {
        void onVentaClick(Venta venta);
        void onVentaLongClick(Venta venta);
    }
}
