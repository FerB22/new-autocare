package com.autocare.garage_service.service;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.exception.ClienteNoEncontradoException;
import com.autocare.garage_service.exception.VehiculoNoEncontradoException;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.repository.ClienteRepository;
import com.autocare.garage_service.repository.VehiculoRepository;
import com.autocare.garage_service.client.LoyaltyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GarageService {

    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final LoyaltyClient loyaltyClient;

    public Cliente registrarCliente(ClienteRequestDTO dto) {
        if (clienteRepository.findByDocumentoIdentidad(dto.documentoIdentidad()).isPresent()) {
            throw new RuntimeException("El cliente con este documento ya existe");
        }

        // Usamos setters para no pisar el new ArrayList<>() de la entidad
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDocumentoIdentidad(dto.documentoIdentidad());
        nuevoCliente.setNombre(dto.nombre());
        nuevoCliente.setApellido(dto.apellido());
        nuevoCliente.setEmail(dto.email());
        nuevoCliente.setTelefono(dto.telefono());

        Cliente clienteGuardado = clienteRepository.save(nuevoCliente);
        loyaltyClient.inicializarPerfilLealtad(clienteGuardado.getId());

        return clienteGuardado;
    }

    @Transactional
    public Vehiculo registrarVehiculoEnGarage(Long clienteId, VehiculoRequestDTO dto) {
        Cliente dueño = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado en el sistema"));

        if (vehiculoRepository.findByPatente(dto.patente()).isPresent()) {
            throw new RuntimeException("Este vehículo ya está registrado en la red");
        }

        Vehiculo nuevoVehiculo = new Vehiculo(
                null, dto.patente(), dto.marca(), dto.modelo(),
                dto.anio(), dto.color(), dto.vin(), dueño
        );

        return vehiculoRepository.save(nuevoVehiculo);
    }

    public Cliente obtenerPerfilCompleto(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("Perfil de cliente no encontrado"));
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // ── NUEVOS MÉTODOS ──────────────────────────────────────────────────────────

    @Transactional
    public Cliente actualizarCliente(Long id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado en el sistema"));

        clienteRepository.findByDocumentoIdentidad(dto.documentoIdentidad())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new RuntimeException("El documento ya pertenece a otro cliente registrado");
                    }
                });

        cliente.setDocumentoIdentidad(dto.documentoIdentidad());
        cliente.setNombre(dto.nombre());
        cliente.setApellido(dto.apellido());
        cliente.setEmail(dto.email());
        cliente.setTelefono(dto.telefono());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado en el sistema"));

        clienteRepository.delete(cliente);
    }

    @Transactional
    public Vehiculo actualizarVehiculo(Long clienteId, Long vehiculoId, VehiculoRequestDTO dto) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado en el sistema"));

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new VehiculoNoEncontradoException("Vehículo no encontrado en el garage"));

        vehiculoRepository.findByPatente(dto.patente())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(vehiculoId)) {
                        throw new RuntimeException("La patente ya está registrada en otro vehículo");
                    }
                });

        vehiculo.setPatente(dto.patente());
        vehiculo.setMarca(dto.marca());
        vehiculo.setModelo(dto.modelo());
        vehiculo.setAnio(dto.anio());
        vehiculo.setColor(dto.color());
        vehiculo.setVin(dto.vin());

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void eliminarVehiculo(Long clienteId, Long vehiculoId) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado en el sistema"));

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new VehiculoNoEncontradoException("Vehículo no encontrado en el garage"));

        vehiculoRepository.delete(vehiculo);
    }

    public Vehiculo obtenerVehiculoPorId(Long vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new VehiculoNoEncontradoException("Vehículo no encontrado en el garage"));
    }
}