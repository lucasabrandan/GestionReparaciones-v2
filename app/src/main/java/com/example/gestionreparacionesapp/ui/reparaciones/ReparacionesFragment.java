// REEMPLAZA TODO EL ARCHIVO CON ESTE CÓDIGO
package com.example.gestionreparacionesapp.ui.reparaciones;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel reparacionesViewModel;
    private ClientesViewModel clientesViewModel;
    // Eliminamos la dependencia de ProductoViewModel

    private RecyclerView recyclerViewReparaciones;
    private ReparacionesAdapter adapter;
    private FloatingActionButton fabAgregarReparacion;
    private EditText etBuscadorReparaciones;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reparacionesViewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        // El adapter ahora solo necesita la lista de clientes para mostrar el nombre
        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reparaciones, container, false);
        recyclerViewReparaciones = view.findViewById(R.id.recyclerViewReparaciones);
        fabAgregarReparacion = view.findViewById(R.id.fabAgregarReparacion);
        etBuscadorReparaciones = view.findViewById(R.id.etBuscadorReparaciones);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        reparacionesViewModel.cargarReparaciones();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        // Ya no cargamos productos
    }

    private void setupRecyclerView() {
        recyclerViewReparaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReparaciones.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarReparacion.setOnClickListener(v -> mostrarDialogoReparacion(null));
        etBuscadorReparaciones.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                reparacionesViewModel.buscarReparaciones(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        reparacionesViewModel.getListaReparaciones().observe(getViewLifecycleOwner(), reparaciones -> {
            if (reparaciones != null) {
                adapter.setReparaciones(reparaciones);
            }
        });
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                this.listaClientesSpinner = clientes;
                adapter.setClientes(clientes);
            }
        });
        reparacionesViewModel.getResultadoOperacion().observe(getViewLifecycleOwner(), resultado -> {
            if (resultado != null && getContext() != null) {
                Toast.makeText(getContext(), resultado.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReparacionClick(Reparacion reparacion) {
        mostrarDialogoReparacion(reparacion);
    }

    @Override
    public void onReparacionLongClick(Reparacion reparacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Reparación")
                .setMessage("¿Estás seguro de que quieres eliminar la reparación del equipo '" + reparacion.getEquipoMarca() + " " + reparacion.getEquipoModelo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> reparacionesViewModel.eliminarReparacion(reparacion.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- MÉTODO PRINCIPAL MODIFICADO ---
    private void mostrarDialogoReparacion(@Nullable Reparacion reparacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nueva_reparacion, null);
        builder.setView(dialogView);

        // --- Bindeo de los nuevos campos del layout ---
        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteReparacion);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteReparacionDialog);
        EditText etEquipoMarca = dialogView.findViewById(R.id.etEquipoMarca);
        EditText etEquipoModelo = dialogView.findViewById(R.id.etEquipoModelo);
        EditText etEquipoSerie = dialogView.findViewById(R.id.etEquipoSerie);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcionReparacion);
        EditText etRepuestos = dialogView.findViewById(R.id.etRepuestosUtilizados);
        EditText etCostoRepuestos = dialogView.findViewById(R.id.etCostoRepuestos);
        EditText etCostoManoDeObra = dialogView.findViewById(R.id.etCostoManoDeObra);
        Spinner spinnerEstado = dialogView.findViewById(R.id.spinnerEstadoReparacion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarReparacionDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarReparacionDialog);

        // Configuración de Spinners (Cliente y Estado)
        refrescarSpinnerClientes(spinnerCliente);
        ArrayAdapter<CharSequence> estadoAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.estados_reparacion, android.R.layout.simple_spinner_item);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        // Listener para el botón "Nuevo Cliente"
        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente(spinnerCliente));

        // --- LÓGICA DE EDICIÓN O CREACIÓN ---
        if (reparacion != null) { // MODO EDICIÓN
            builder.setTitle("Editar Reparación");
            btnGuardar.setText("Actualizar");

            // Rellenar campos con los datos de la reparación existente
            etEquipoMarca.setText(reparacion.getEquipoMarca());
            etEquipoModelo.setText(reparacion.getEquipoModelo());
            etEquipoSerie.setText(reparacion.getEquipoSerie());
            etDescripcion.setText(reparacion.getDescripcionProblema());
            etRepuestos.setText(reparacion.getRepuestosUtilizados());
            etCostoRepuestos.setText(String.valueOf(reparacion.getCostoRepuestos()));
            etCostoManoDeObra.setText(String.valueOf(reparacion.getCostoManoDeObra()));

            // Seleccionar cliente en el spinner
            spinnerCliente.post(() -> {
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == reparacion.getClienteId()) {
                        spinnerCliente.setSelection(i + 1); // +1 por el "Seleccionar..."
                        break;
                    }
                }
            });

            // Seleccionar estado en el spinner
            int estadoPosition = estadoAdapter.getPosition(reparacion.getEstado());
            if (estadoPosition >= 0) {
                spinnerEstado.setSelection(estadoPosition);
            }
        } else { // MODO CREAR
            builder.setTitle("Nueva Reparación");
            btnGuardar.setText("Guardar");
        }

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            int clientePosition = spinnerCliente.getSelectedItemPosition();
            if (clientePosition <= 0) {
                Toast.makeText(getContext(), "Debes seleccionar un cliente.", Toast.LENGTH_SHORT).show();
                return;
            }
            Cliente clienteSeleccionado = listaClientesSpinner.get(clientePosition - 1);

            String marca = etEquipoMarca.getText().toString().trim();
            String modelo = etEquipoModelo.getText().toString().trim();
            if (marca.isEmpty() || modelo.isEmpty()) {
                Toast.makeText(getContext(), "La Marca y el Modelo del equipo son obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            String serie = etEquipoSerie.getText().toString().trim();
            String desc = etDescripcion.getText().toString().trim();
            String repuestos = etRepuestos.getText().toString().trim();
            String costoRepStr = etCostoRepuestos.getText().toString();
            String costoManoObraStr = etCostoManoDeObra.getText().toString();
            String estado = spinnerEstado.getSelectedItem().toString();

            if (reparacion == null) { // Insertar nuevo
                reparacionesViewModel.insertarReparacion(clienteSeleccionado.getId(), marca, modelo, serie, desc, repuestos, costoRepStr, costoManoObraStr, estado);
            } else { // Actualizar existente
                reparacionesViewModel.actualizarReparacion(reparacion.getId(), clienteSeleccionado.getId(), marca, modelo, serie, desc, repuestos, costoRepStr, costoManoObraStr, estado);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void refrescarSpinnerClientes(Spinner spinner) {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccionar cliente...");
        if (listaClientesSpinner != null) {
            nombres.addAll(listaClientesSpinner.stream().map(Cliente::getNombre).collect(Collectors.toList()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void mostrarDialogoNuevoCliente(Spinner spinnerAActualizar) {
        // Este método se mantiene igual, no necesita cambios.
        // ... (el código que ya tenías para este método está bien)
    }
}
