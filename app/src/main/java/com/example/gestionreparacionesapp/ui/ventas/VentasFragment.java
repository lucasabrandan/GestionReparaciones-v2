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
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VentasFragment extends Fragment implements VentasAdapter.OnVentaInteractionListener {

    private VentasViewModel ventasViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;
    private VentasAdapter adapter;

    private RecyclerView recyclerViewVentas;
    private EditText etBuscador;

    private List<Cliente> listaClientes = new ArrayList<>();
    private List<Producto> listaProductos = new ArrayList<>();
    private ArrayAdapter<String> clientAdapter;
    private ArrayAdapter<String> productAdapter;
    private Spinner spClienteGlobal;
    private Spinner spProductoGlobal;

    public VentasFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(this).get(ProductoViewModel.class);
        adapter = new VentasAdapter(new ArrayList<>(), new ArrayList<>(), this); // Pasamos lista vacía de clientes por ahora
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ventas, container, false);
        recyclerViewVentas = view.findViewById(R.id.recyclerViewVentas);
        etBuscador = view.findViewById(R.id.etBuscadorVentas);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregarVenta);

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

        ventasViewModel.cargarVentas();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();

        ventasViewModel.getListaVentas().observe(getViewLifecycleOwner(), list -> adapter.setVentas(list));

        // Observar Clientes y actualizar Adapter y Spinner
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), list -> {
            if(list != null) {
                listaClientes = list;
                adapter.setClientes(list); // Actualizamos adapter para que muestre nombres
                if(clientAdapter != null) {
                    actualizarSpinnerClientes(clientAdapter, list);
                    // Auto-seleccionar si se creó uno nuevo
                    if(list.size() > 0 && spClienteGlobal != null && spClienteGlobal.getSelectedItemPosition() == 0) {
                        spClienteGlobal.setSelection(list.size());
                    }
                }
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), list -> {
            if(list != null) {
                listaProductos = list;
                if(productAdapter != null) {
                    actualizarSpinnerProductos(productAdapter, list);
                    if(list.size() > 0 && spProductoGlobal != null && spProductoGlobal.getSelectedItemPosition() == 0) {
                        spProductoGlobal.setSelection(list.size());
                    }
                }
            }
        });

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), res -> {
            if(res != null) Toast.makeText(getContext(), res.message, Toast.LENGTH_SHORT).show();
        });

        ventasViewModel.getPdfGeneradoEvent().observe(getViewLifecycleOwner(), this::compartirPdf);

        // Refrescos
        clientesViewModel.getClienteCreadoConExito().observe(getViewLifecycleOwner(), success -> {
            if(success) clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        });
        productoViewModel.getNuevoProductoCreadoEvent().observe(getViewLifecycleOwner(), p -> {
            if(p != null) productoViewModel.cargarProductos();
        });
    }

    private void mostrarDialogoNuevaVenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_nueva_venta, null);
        builder.setView(view);

        Spinner spCliente = view.findViewById(R.id.spinnerClienteVenta);
        this.spClienteGlobal = spCliente;
        Spinner spProducto = view.findViewById(R.id.spinnerProductoVenta);
        this.spProductoGlobal = spProducto;

        EditText etCantidad = view.findViewById(R.id.etCantidadVenta);
        Button btnAgregar = view.findViewById(R.id.btnAgregarProductoVenta);
        LinearLayout llContainer = view.findViewById(R.id.llProductosVentaContainer);
        TextView tvTotal = view.findViewById(R.id.tvTotalVentaDialog);
        Button btnFinalizar = view.findViewById(R.id.btnFinalizarVenta);

        Button btnNewCli = view.findViewById(R.id.btnNuevoClienteVenta);
        Button btnNewProd = view.findViewById(R.id.btnNuevoProductoVenta);
        Button btnCancel = view.findViewById(R.id.btnCancelarVenta);

        configurarSpinnerClientes(spCliente);
        configurarSpinnerProductos(spProducto);

        List<ProductoVenta> carrito = new ArrayList<>();
        final double[] totalVenta = {0.0};

        // Listeners Nuevo
        if(btnNewCli != null) btnNewCli.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        if(btnNewProd != null) btnNewProd.setOnClickListener(v -> mostrarDialogoNuevoProducto());

        AlertDialog dialog = builder.create();
        if(btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAgregar.setOnClickListener(v -> {
            int pos = spProducto.getSelectedItemPosition();
            if (pos <= 0) {
                Toast.makeText(getContext(), "Seleccione un producto", Toast.LENGTH_SHORT).show();
                return;
            }

            Producto prod = listaProductos.get(pos - 1);
            try {
                String cantStr = etCantidad.getText().toString();
                if (cantStr.isEmpty()) { etCantidad.setError("Requerido"); return; }
                int cant = Integer.parseInt(cantStr);
                if (cant <= 0) throw new NumberFormatException();

                if (cant > prod.getCantidad()) {
                    etCantidad.setError("Stock insuficiente");
                    return;
                }

                ProductoVenta item = new ProductoVenta(0, prod.getId(), prod.getNombre(), cant, prod.getPrecio());
                item.setProducto(prod);
                carrito.add(item);

                // Agregar vista visual
                View row = getLayoutInflater().inflate(R.layout.item_producto_reparacion_simple, null);
                TextView tv = row.findViewById(R.id.tvResumenItemSimple);
                tv.setText(cant + "x " + prod.getNombre() + " ($" + (cant * prod.getPrecio()) + ")");
                llContainer.addView(row);

                totalVenta[0] += (cant * prod.getPrecio());
                tvTotal.setText("Total: $" + totalVenta[0]);

                etCantidad.setText("1");
                spProducto.setSelection(0); // Reset spinner para agregar otro

            } catch (NumberFormatException e) {
                etCantidad.setError("Cantidad inválida");
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            int posCliente = spCliente.getSelectedItemPosition();
            if (posCliente <= 0) {
                TextView errView = (TextView) spCliente.getSelectedView();
                if(errView != null) errView.setError("Requerido");
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

    // --- Helpers Spinners ---
    private void configurarSpinnerClientes(Spinner sp) {
        clientAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        sp.setAdapter(clientAdapter);
        actualizarSpinnerClientes(clientAdapter, listaClientes);
    }

    private void actualizarSpinnerClientes(ArrayAdapter<String> adp, List<Cliente> list) {
        adp.clear(); adp.add("Seleccionar Cliente...");
        for (Cliente c : list) adp.add(c.getNombre());
        adp.notifyDataSetChanged();
    }

    private void configurarSpinnerProductos(Spinner sp) {
        productAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        sp.setAdapter(productAdapter);
        actualizarSpinnerProductos(productAdapter, listaProductos);
    }

    private void actualizarSpinnerProductos(ArrayAdapter<String> adp, List<Producto> list) {
        adp.clear(); adp.add("Seleccionar Producto...");
        for (Producto p : list) adp.add(p.getNombre() + " ($" + p.getPrecio() + ")");
        adp.notifyDataSetChanged();
    }

    // --- Diálogos Rápidos ---
    private void mostrarDialogoNuevoCliente() {
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(view).create();
        EditText etDni = view.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = view.findViewById(R.id.etNombreClienteDialog);
        // Opcionales
        EditText etDir = view.findViewById(R.id.etDireccionClienteDialog);
        EditText etLoc = view.findViewById(R.id.etLocalidadClienteDialog);

        view.findViewById(R.id.btnGuardarClienteDialog).setOnClickListener(v -> {
            if(etNombre.getText().toString().isEmpty()) {
                etNombre.setError("Requerido");
                return;
            }
            String dniVal = etDni.getText().toString().isEmpty() ? "-" : etDni.getText().toString();
            String dirVal = (etDir != null) ? etDir.getText().toString() : "";
            String locVal = (etLoc != null) ? etLoc.getText().toString() : "";

            clientesViewModel.guardarCliente(dniVal, etNombre.getText().toString(), dirVal, locVal, "");
            d.dismiss();
        });
        view.findViewById(R.id.btnCancelarClienteDialog).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void mostrarDialogoNuevoProducto() {
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(view).create();
        EditText etNom = view.findViewById(R.id.etNombreProductoDialog);
        EditText etPre = view.findViewById(R.id.etPrecioDialog);
        EditText etCant = view.findViewById(R.id.etCantidadDialog);
        EditText etSku = view.findViewById(R.id.etSkuDialog); // Si existe en layout

        view.findViewById(R.id.btnGuardarProductoDialog).setOnClickListener(v -> {
            if(etNom.getText().toString().isEmpty()) { etNom.setError("Requerido"); return; }
            if(etPre.getText().toString().isEmpty()) { etPre.setError("Requerido"); return; }

            // SKU opcional
            String skuVal = (etSku != null && !etSku.getText().toString().isEmpty()) ? etSku.getText().toString() : "SKU-" + System.currentTimeMillis();

            productoViewModel.guardarProducto(skuVal, etNom.getText().toString(), etPre.getText().toString(), etCant.getText().toString());
            d.dismiss();
        });
        view.findViewById(R.id.btnCancelarProductoDialog).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    @Override public void onVentaClick(Venta venta) { ventasViewModel.generarComprobanteVenta(requireContext(), venta); }

    private void compartirPdf(File pdfFile) {
        if(pdfFile == null) return;
        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", pdfFile);
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(android.content.Intent.createChooser(intent, "Compartir Comprobante"));
    }
}