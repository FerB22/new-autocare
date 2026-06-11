package com.autocare.workshop_service.service;

import com.autocare.workshop_service.client.InventoryClient;
import com.autocare.workshop_service.dto.AsignarRepuestoDTO;
import com.autocare.workshop_service.dto.OrdenTrabajoRequestDTO;
import com.autocare.workshop_service.model.OrdenTrabajo;
import com.autocare.workshop_service.repository.OrdenTrabajoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenTrabajoService {

    private final OrdenTrabajoRepository repository;
    private final InventoryClient inventoryClient;

    public List<OrdenTrabajo> obtenerTodas() {
        return repository.findAll();
    }

    public OrdenTrabajo crearOrden(OrdenTrabajoRequestDTO dto) {
        OrdenTrabajo orden = new OrdenTrabajo(
                null,
                dto.vehiculoId(),
                dto.mecanicoId(),
                dto.descripcionFalla(),
                OrdenTrabajo.EstadoOrden.RECEPCIONADO,
                LocalDateTime.now(),
                null
        );
        return repository.save(orden);
    }

    @Transactional
    public OrdenTrabajo utilizarRepuestoEnOrden(Long ordenId, AsignarRepuestoDTO dto) {
        OrdenTrabajo orden = repository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada en el sistema"));

        // Si la orden estaba solo recepcionada, al usar repuestos cambia automáticamente a progreso
        if (orden.getEstado() == OrdenTrabajo.EstadoOrden.RECEPCIONADO) {
            orden.setEstado(OrdenTrabajo.EstadoOrden.EN_PROGRESO);
        }

        // Ejecutamos el puente síncrono WebClient hacia inventory-service
        inventoryClient.descontarRepuesto(dto.repuestoId(), dto.cantidad());

        // Guardamos los cambios de estado locales de la orden
        return repository.save(orden);
    }
}