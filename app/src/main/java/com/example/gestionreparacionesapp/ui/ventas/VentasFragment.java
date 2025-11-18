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
import android.widget.ImageButton;
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
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class VentasFragment extends Fragment implements VentasAdapter.OnVentaInteractionListener {

    private VentasViewModel ventasViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerViewVentas;
    private VentasAdapter adapter;
    private TextView tvSinVentas;
    private FloatingActionButton fabAgregarVenta;
    private EditText etBuscadorVentas;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    public VentasFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new VentasAdapter(new ArrayList<>(), new ArrayList<>(), this);
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ventas, container, false);
        recyclerViewVentas = view.findViewById(R.id.recyclerViewVentas);
        tvSinVentas = view.findViewById(R.id.tvSinVentas);
        fabAgregarVenta = view.findViewById(R.id.fabAgregarVenta);
        etBuscadorVentas = view.findViewById(R.id.etBuscadorVentas);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        ventasViewModel.cargarVentas();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();
    }

    private void setupRecyclerView() {
        recyclerViewVentas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVentas.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarVenta.setOnClickListener(v -> mostrarDialogoVenta());
        etBuscadorVentas.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ventasViewModel.buscarVentas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onVentaClick(Venta venta) {
        Toast.makeText(getContext(), "Venta #" + venta.getId() + " seleccionada.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVentaLongClick(Venta venta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Anular Venta")
                .setMessage("Esta acción no se puede deshacer y no restaurará el stock de los productos vendidos. ¿Deseas continuar?")
                .setPositiveButton("Anular", (dialog, which) -> ventasViewModel.eliminarVenta(venta))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        ventasViewModel.getListaVentas().observe(getViewLifecycleOwner(), ventas -> {
            boolean isEmpty = ventas == null || ventas.isEmpty();
            tvSinVentas.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerViewVentas.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (!isEmpty) adapter.setVentas(ventas);
        });

        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                adapter.setClientes(clientes);
                listaClientesSpinner = clientes;
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) listaProductosSpinner = productos;
        });

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoVenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nueva_venta, null);
        builder.setView(dialogView);
        builder.setTitle("Nueva Venta");

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteVenta);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteVentaDialog);
        LinearLayout containerProductos = dialogView.findViewById(R.id.containerProductosVenta);
        Button btnAnadirProducto = dialogView.findViewById(R.id.btnAnadirProductoVenta);
        Button btnNuevoProducto = dialogView.findViewById(R.id.btnNuevoProductoVentaDialog);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotalVenta);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarVentaDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarVentaDialog);

        final List<ProductoVenta> productosEnVenta = new ArrayList<>();
        final Cliente[] clienteSeleccionado = {null};

        // Configurar Spinner de Clientes
        List<String> nombresClientes = new ArrayList<>();
        nombresClientes.add("Selecciona un cliente");
        if (listaClientesSpinner != null) {
            nombresClientes.addAll(listaClientesSpinner.stream().map(c -> c.getNombre() + " (DNI: " + c.getDni() + ")").collect(Collectors.toList()));
        }
        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresClientes);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);

        spinnerCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clienteSeleccionado[0] = (position > 0) ? listaClientesSpinner.get(position - 1) : null;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAnadirProducto.setOnClickListener(v -> agregarViewProducto(containerProductos, productosEnVenta, tvTotal));
        agregarViewProducto(containerProductos, productosEnVenta, tvTotal);

        AlertDialog dialog = builder.create();

        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto());

        btnGuardar.setOnClickListener(v -> {
            actualizarListaProductos(containerProductos, productosEnVenta);
            ventasViewModel.guardarVenta(clienteSeleccionado[0], productosEnVenta);
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        if (listaProductosSpinner == null || listaProductosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos disponibles para vender.", Toast.LENGTH_SHORT).show();
            return;
        }

        View productoView = getLayoutInflater().inflate(R.layout.item_producto_venta, container, false);
        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        ImageButton btnEliminar = productoView.findViewById(R.id.btnEliminarProductoVenta);

        List<String> nombresProductos = new ArrayList<>();
        nombresProductos.add("Selecciona un producto");
        nombresProductos.addAll(listaProductosSpinner.stream()
                .map(p -> String.format(Locale.getDefault(), "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                .collect(Collectors.toList()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);

        TextWatcher textWatcher = (new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etCantidad.addTextChangedListener(textWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalVenta(container, productosLista, tvTotal);
        });

        container.addView(productoView);
    }

    private void actualizarListaProductos(LinearLayout container, List<ProductoVenta> productosLista) {
        productosLista.clear();
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Spinner spinnerProducto = itemView.findViewById(R.id.spinnerProducto);
            EditText etCantidad = itemView.findViewById(R.id.etCantidad);
            int pos = spinnerProducto.getSelectedItemPosition();
            if (pos > 0) {
                Producto p = listaProductosSpinner.get(pos - 1);
                int cant = 0;
                try {
                    cant = Integer.parseInt(etCantidad.getText().toString());
                } catch (NumberFormatException ignored) {}
                if (cant > 0) productosLista.add(new ProductoVenta(p, cant));
            }
        }
    }

    private void calcularTotalVenta(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        actualizarListaProductos(container, productosLista);
        double total = 0;
        for (ProductoVenta pv : productosLista) {
            total += pv.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "TOTAL: $%.2f", total));
    }

    private void mostrarDialogoNuevoCliente() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Cliente Rápido")
                .setView(dialogView)
                .create();

        EditText etDni = dialogView.findViewById(R.id.etDniDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadDialog);
        EditText etCodigoPostal = dialogView.findViewById(R.id.etCodigoPostalDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        btnGuardar.setOnClickListener(v -> {
            clientesViewModel.guardarCliente(
                    etDni.getText().toString(),
                    etNombre.getText().toString(),
                    etDireccion.getText().toString(),
                    etLocalidad.getText().toString(),
                    etCodigoPostal.getText().toString()
            );
            dialog.dismiss();
            Toast.makeText(getContext(), "Cliente guardado. Cierra y vuelve a abrir el diálogo para seleccionarlo.", Toast.LENGTH_LONG).show();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoNuevoProducto() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Producto Rápido")
                .setView(dialogView)
                .create();

        // --- INICIO DE LA CORRECCIÓN ---
        // Usamos los IDs correctos que existen en tu layout 'dialog_nuevo_producto.xml'
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog); // ID CORREGIDO
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);   // ID CORREGIDO

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        btnGuardar.setOnClickListener(v -> {
            // Pasamos un String vacío para el SKU, que no se pide en este diálogo rápido.
            productoViewModel.guardarProducto(
                    "", // SKU
                    etNombre.getText().toString(),
                    etPrecio.getText().toString(),
                    etCantidad.getText().toString()
            );
            // --- FIN DE LA CORRECCIÓN ---

            dialog.dismiss();
            Toast.makeText(getContext(), "Producto guardado. Cierra y vuelve a abrir el diálogo para seleccionarlo.", Toast.LENGTH_LONG).show();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}

