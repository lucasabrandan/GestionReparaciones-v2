package com.example.gestionreparacionesapp.ui.ventas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// --- PASO 1: IMPLEMENTAR LA INTERFAZ ---
public class VentasFragment extends Fragment implements VentasAdapter.OnVentaInteractionListener {

    private VentasViewModel ventasViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerView;
    private VentasAdapter adapter;
    private TextView tvSinVentas;
    private EditText etBuscadorVentas;
    private FloatingActionButton fabAgregarVenta;

    private List<Cliente> listaDeTodosLosClientes = new ArrayList<>();
    private List<Producto> listaDeTodosLosProductos = new ArrayList<>();

    public VentasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ventas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupViewModels();
        setupRecyclerView();
        observeViewModels();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (clientesViewModel != null) {
            clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        }
        if (productoViewModel != null) {
            productoViewModel.cargarProductos();
        }
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewVentas);
        tvSinVentas = view.findViewById(R.id.tvSinVentas);
        etBuscadorVentas = view.findViewById(R.id.etBuscadorVentas);
        fabAgregarVenta = view.findViewById(R.id.fabAgregarVenta);
    }

    private void setupViewModels() {
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // --- PASO 2: PASAR 'this' COMO LISTENER ---
        adapter = new VentasAdapter(new ArrayList<>(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModels() {
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                listaDeTodosLosClientes = clientes;
                ventasViewModel.cargarVentas();
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) {
                listaDeTodosLosProductos = productos;
            }
        });

        ventasViewModel.getListaVentas().observe(getViewLifecycleOwner(), ventas -> {
            adapter.setVentasYClientes(ventas != null ? ventas : new ArrayList<>(), listaDeTodosLosClientes);
            if (ventas == null || ventas.isEmpty()) {
                tvSinVentas.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvSinVentas.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), resultado -> {
            if (resultado != null && !resultado.message.isEmpty()) {
                Toast.makeText(getContext(), resultado.message, Toast.LENGTH_SHORT).show();
                // Si la operación (guardar/borrar) fue exitosa, recargamos el stock visible en toda la app
                if (resultado.isSuccess) {
                    productoViewModel.cargarProductos();
                }
            }
        });
    }

    private void setupListeners() {
        fabAgregarVenta.setOnClickListener(v -> mostrarDialogoVenta(null));

        etBuscadorVentas.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ventasViewModel.buscarVentas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // --- PASO 3: IMPLEMENTAR LOS MÉTODOS DE LA INTERFAZ ---
    @Override
    public void onVentaClick(Venta venta) {
        // La edición aún no está implementada en el diálogo, pero la llamada está lista.
        Toast.makeText(getContext(), "Funcionalidad para EDITAR venta no implementada.", Toast.LENGTH_SHORT).show();
        // Cuando se implemente, se llamará a: mostrarDialogoVenta(venta);
    }

    @Override
    public void onVentaLongClick(Venta venta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Venta")
                .setMessage("¿Estás seguro de que deseas eliminar la venta #" + venta.getId() + "?\n\nATENCIÓN: Esta acción NO restaura el stock de los productos vendidos.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ventasViewModel.eliminarVenta(venta);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    // -----------------------------------------------------------

    private void mostrarDialogoVenta(@Nullable Venta ventaExistente) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_formulario_venta, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        AutoCompleteTextView actvCliente = dialogView.findViewById(R.id.actvCliente);
        LinearLayout contenedorProductos = dialogView.findViewById(R.id.contenedorProductosVenta);
        MaterialButton btnAnadirProducto = dialogView.findViewById(R.id.btnAnadirProducto);
        TextView tvTotalVenta = dialogView.findViewById(R.id.tvTotalVenta);
        MaterialButton btnCancelarVenta = dialogView.findViewById(R.id.btnCancelarVenta);
        MaterialButton btnGuardarVenta = dialogView.findViewById(R.id.btnGuardarVenta);

        final List<ProductoVenta> productosEnVenta = new ArrayList<>();
        final Cliente[] clienteSeleccionado = {null};

        ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, listaDeTodosLosClientes);
        actvCliente.setAdapter(clienteAdapter);
        actvCliente.setOnItemClickListener((parent, view, position, id) -> clienteSeleccionado[0] = clienteAdapter.getItem(position));

        btnAnadirProducto.setOnClickListener(v -> agregarViewProductoVenta(contenedorProductos, productosEnVenta, tvTotalVenta));

        if (ventaExistente == null) {
            agregarViewProductoVenta(contenedorProductos, productosEnVenta, tvTotalVenta);
        } else {
            // TODO: Lógica para modo edición (cargar cliente y productos del JSON de la venta)
        }

        btnCancelarVenta.setOnClickListener(v -> dialog.dismiss());
        btnGuardarVenta.setOnClickListener(v -> {
            if (clienteSeleccionado[0] == null) {
                Toast.makeText(getContext(), "Por favor, seleccione un cliente", Toast.LENGTH_SHORT).show();
                return;
            }

            actualizarListaProductosVenta(contenedorProductos, productosEnVenta);

            if (productosEnVenta.isEmpty()) {
                Toast.makeText(getContext(), "Debe añadir al menos un producto válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí se pasa la lista de productos al ViewModel, que a su vez se la pasará al Repositorio
            ventasViewModel.guardarVenta(clienteSeleccionado[0], productosEnVenta);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void agregarViewProductoVenta(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        if (listaDeTodosLosProductos.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos cargados", Toast.LENGTH_SHORT).show();
            return;
        }

        View productoView = LayoutInflater.from(getContext()).inflate(R.layout.item_producto_venta, container, false);
        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        ImageButton btnEliminar = productoView.findViewById(R.id.btnEliminarProductoVenta);

        List<String> nombresProductos = listaDeTodosLosProductos.stream()
                .map(p -> String.format(Locale.US, "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                .collect(Collectors.toList());
        nombresProductos.add(0, "Selecciona un producto");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);

        TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            public void afterTextChanged(Editable s) {}
        };
        etCantidad.addTextChangedListener(textWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalVenta(container, productosLista, tvTotal);
        });

        container.addView(productoView);
        calcularTotalVenta(container, productosLista, tvTotal);
    }

    private void actualizarListaProductosVenta(LinearLayout container, List<ProductoVenta> productosLista) {
        productosLista.clear();
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Spinner spinner = itemView.findViewById(R.id.spinnerProducto);
            EditText etCantidad = itemView.findViewById(R.id.etCantidad);
            if (spinner.getSelectedItemPosition() > 0) {
                Producto p = listaDeTodosLosProductos.get(spinner.getSelectedItemPosition() - 1);
                int cantidad = 0;
                try {
                    cantidad = Integer.parseInt(etCantidad.getText().toString());
                } catch (NumberFormatException e) { /* cantidad es 0 */ }
                if (cantidad > 0) {
                    productosLista.add(new ProductoVenta(p, cantidad));
                }
            }
        }
    }

    private void calcularTotalVenta(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        actualizarListaProductosVenta(container, productosLista);
        double total = 0;
        for (ProductoVenta pv : productosLista) {
            total += pv.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.US, "$%.2f", total));
    }
}
