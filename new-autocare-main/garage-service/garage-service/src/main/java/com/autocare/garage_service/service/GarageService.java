package com.autocare.garage_service.service;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
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
    private final LoyaltyClient loyaltyClient; // Inyectamos el emisario

    public Cliente registrarCliente(ClienteRequestDTO dto) {
        if (clienteRepository.findByDocumentoIdentidad(dto.documentoIdentidad()).isPresent()) {
            throw new RuntimeException("El cliente con este documento ya existe");
        }

        Cliente nuevoCliente = new Cliente(
                null, dto.documentoIdentidad(), dto.nombre(), 
                dto.apellido(), dto.email(), dto.telefono(), null
        );
        // 1. Guardamos al humano en la BD local de autocare_garage
        Cliente clienteGuardado = clienteRepository.save(nuevoCliente);

        // 2. Disparamos la señal espacial síncrona hacia el loyalty-service
        loyaltyClient.inicializarPerfilLealtad(clienteGuardado.getId());

        return clienteGuardado;
    }

    @Transactional
    public Vehiculo registrarVehiculoEnGarage(Long clienteId, VehiculoRequestDTO dto) {
        Cliente dueño = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado en el sistema"));

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
                .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
    }
    
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }
}