package com.example.gestionreparacionesapp.ui.ventas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta; // <--- LA IMPORTACIÓN CORRECTA
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VentasFragment extends Fragment implements VentasAdapter.OnVentaInteractionListener {

    private VentasViewModel ventasViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;
    private VentasAdapter adapter;

    private RecyclerView recyclerViewVentas;
    private EditText etBuscador;

    // Listas para Spinners
    private List<Cliente> listaClientes = new ArrayList<>();
    private List<Producto> listaProductos = new ArrayList<>();

    public VentasFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(this).get(ProductoViewModel.class);
        adapter = new VentasAdapter(new ArrayList<>(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ventas, container, false); // Asegúrate de tener este XML
        recyclerViewVentas = view.findViewById(R.id.recyclerViewVentas); // Asegúrate de este ID en el XML
        etBuscador = view.findViewById(R.id.etBuscadorVentas); // Asegúrate de este ID
        FloatingActionButton fab = view.findViewById(R.id.fabAgregarVenta); // Asegúrate de este ID

        recyclerViewVentas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVentas.setAdapter(adapter);

        fab.setOnClickListener(v -> mostrarDialogoNuevaVenta());

        etBuscador.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ventasViewModel.buscarVentas(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargas iniciales
        ventasViewModel.cargarVentas();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();

        // Observers
        ventasViewModel.getListaVentas().observe(getViewLifecycleOwner(), list -> adapter.setVentas(list));
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), list -> listaClientes = list);
        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), list -> listaProductos = list);

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), res -> {
            if(res != null) Toast.makeText(getContext(), res.message, Toast.LENGTH_SHORT).show();
        });

        // Observer para el PDF
        ventasViewModel.getPdfGeneradoEvent().observe(getViewLifecycleOwner(), file -> {
            if (file != null) {
                // Aquí podrías llamar al método de compartir que usamos en ReparacionesFragment
                // Por brevedad, solo mostramos toast, pero copia el método compartirPdf aquí si quieres.
                Toast.makeText(getContext(), "PDF generado: " + file.getName(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoNuevaVenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_nueva_venta, null); // Necesitas crear este XML similar al de reparacion
        builder.setView(view);

        Spinner spCliente = view.findViewById(R.id.spinnerClienteVenta);
        Spinner spProducto = view.findViewById(R.id.spinnerProductoVenta);
        EditText etCantidad = view.findViewById(R.id.etCantidadVenta);
        Button btnAgregar = view.findViewById(R.id.btnAgregarProductoVenta);
        LinearLayout llContainer = view.findViewById(R.id.llProductosVentaContainer);
        TextView tvTotal = view.findViewById(R.id.tvTotalVentaDialog);
        Button btnFinalizar = view.findViewById(R.id.btnFinalizarVenta);

        // Configurar Spinners
        configurarSpinnerClientes(spCliente);
        configurarSpinnerProductos(spProducto);

        // Lista temporal de productos AÑADIDOS AL CARRITO
        List<ProductoVenta> carrito = new ArrayList<>();
        final double[] totalVenta = {0.0};

        btnAgregar.setOnClickListener(v -> {
            int pos = spProducto.getSelectedItemPosition();
            if (pos <= 0) return;

            Producto prod = listaProductos.get(pos - 1);
            try {
                int cant = Integer.parseInt(etCantidad.getText().toString());
                if (cant <= 0) throw new NumberFormatException();
                if (cant > prod.getCantidad()) {
                    Toast.makeText(getContext(), "Stock insuficiente (Max: " + prod.getCantidad() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Creamos la entidad correcta
                ProductoVenta item = new ProductoVenta(0, prod.getId(), prod.getNombre(), cant, prod.getPrecio());
                item.setProducto(prod); // Guardamos referencia completa para UI

                carrito.add(item);

                // Actualizar UI
                View row = getLayoutInflater().inflate(R.layout.item_producto_reparacion_simple, null); // Reusamos layout simple
                TextView tv = row.findViewById(R.id.tvResumenItemSimple); // Asegúrate que el ID coincida
                if(tv == null) tv = new TextView(getContext()); // Fallback si no usas xml

                tv.setText(cant + "x " + prod.getNombre() + " ($" + (cant * prod.getPrecio()) + ")");
                llContainer.addView(row);

                // Actualizar Total
                totalVenta[0] += (cant * prod.getPrecio());
                tvTotal.setText("Total: $" + totalVenta[0]);

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        btnFinalizar.setOnClickListener(v -> {
            int posCliente = spCliente.getSelectedItemPosition();
            if (posCliente <= 0) {
                Toast.makeText(getContext(), "Seleccione un cliente", Toast.LENGTH_SHORT).show();
                return;
            }
            if (carrito.isEmpty()) {
                Toast.makeText(getContext(), "Agregue productos", Toast.LENGTH_SHORT).show();
                return;
            }

            Cliente cliente = listaClientes.get(posCliente - 1);
            ventasViewModel.guardarVenta(cliente, carrito);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void configurarSpinnerClientes(Spinner sp) {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccionar Cliente...");
        for (Cliente c : listaClientes) nombres.add(c.getNombre());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, nombres);
        sp.setAdapter(adapter);
    }

    private void configurarSpinnerProductos(Spinner sp) {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccionar Producto...");
        for (Producto p : listaProductos) nombres.add(p.getNombre() + " ($" + p.getPrecio() + ")");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, nombres);
        sp.setAdapter(adapter);
    }

    @Override
    public void onVentaClick(Venta venta) {
        // Generar PDF de la venta al hacer click (o abrir detalle)
        ventasViewModel.generarComprobanteVenta(requireContext(), venta);
    }
}