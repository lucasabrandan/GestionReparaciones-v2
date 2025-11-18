package com.example.gestionreparacionesapp.ui.clientes;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class ClientesFragment extends Fragment implements ClientesAdapter.OnClienteInteractionListener {

    private ClientesViewModel viewModel;
    private RecyclerView recyclerViewClientes;
    private ClientesAdapter adapter;
    private TextView tvSinClientes;
    private FloatingActionButton fabAgregarCliente;
    private EditText etBuscadorClientes;

    public ClientesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ClientesAdapter(new ArrayList<>(), this);
        viewModel = new ViewModelProvider(this).get(ClientesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);

        recyclerViewClientes = view.findViewById(R.id.recyclerViewClientes);
        tvSinClientes = view.findViewById(R.id.tvSinClientes);
        fabAgregarCliente = view.findViewById(R.id.fabAgregarCliente);
        etBuscadorClientes = view.findViewById(R.id.etBuscadorClientes);

        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        viewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS); // Carga inicial
    }

    private void setupRecyclerView() {
        recyclerViewClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewClientes.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarCliente.setOnClickListener(v -> mostrarDialogoCliente(null));

        etBuscadorClientes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscarClientes(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onClienteClick(Cliente cliente) {
        mostrarDialogoCliente(cliente); // Clic normal para Editar
    }

    @Override
    public void onClienteLongClick(Cliente cliente) {
        // Clic largo para Borrar
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Cliente")
                .setMessage("¿Estás seguro de que deseas eliminar a '" + cliente.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarCliente(cliente);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        viewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes == null || clientes.isEmpty()) {
                tvSinClientes.setVisibility(View.VISIBLE);
                recyclerViewClientes.setVisibility(View.GONE);
            } else {
                tvSinClientes.setVisibility(View.GONE);
                recyclerViewClientes.setVisibility(View.VISIBLE);
                adapter.setClientes(clientes);
            }
        });
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Muestra el diálogo.
     */
    private void mostrarDialogoCliente(@Nullable Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_cliente, null);
        builder.setView(dialogView);

        // --- ¡AQUÍ LA CORRECCIÓN! ---
        // Se usan los IDs estandarizados del archivo XML que ahora terminan en "ClienteDialog" o "CpClienteDialog".
        EditText etDni = dialogView.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionClienteDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadClienteDialog);
        EditText etCodigoPostal = dialogView.findViewById(R.id.etCpClienteDialog);

        Button btnGuardarCliente = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelarCliente = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        if (cliente != null) { // Modo Editar
            builder.setTitle("Editar Cliente");
            etDni.setText(cliente.getDni());
            etNombre.setText(cliente.getNombre());
            etDireccion.setText(cliente.getDireccion());
            etLocalidad.setText(cliente.getLocalidad());
            etCodigoPostal.setText(cliente.getCodigoPostal());
            etDni.setEnabled(false); // No se puede editar el DNI (clave de negocio)
        } else { // Modo Crear
            builder.setTitle("Nuevo Cliente");
            etDni.setEnabled(true);
        }

        AlertDialog dialog = builder.create();

        btnGuardarCliente.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String localidad = etLocalidad.getText().toString().trim();
            String codigoPostal = etCodigoPostal.getText().toString().trim();

            if (cliente == null) {
                // Modo CREAR
                viewModel.guardarCliente(dni, nombre, direccion, localidad, codigoPostal);
            } else {
                // Modo EDITAR
                viewModel.actualizarCliente(cliente.getId(), dni, nombre, direccion, localidad, codigoPostal);
            }
            dialog.dismiss();
        });

        btnCancelarCliente.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}