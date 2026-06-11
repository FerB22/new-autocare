package com.autocare.procurement_service.service;

import com.autocare.procurement_service.dto.OrdenCompraRequestDTO;
import com.autocare.procurement_service.dto.ProveedorRequestDTO;
import com.autocare.procurement_service.model.OrdenCompra;
import com.autocare.procurement_service.model.Proveedor;
import com.autocare.procurement_service.repository.OrdenCompraRepository;
import com.autocare.procurement_service.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final ProveedorRepository proveedorRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    public Proveedor registrarProveedor(ProveedorRequestDTO dto) {
        Proveedor proveedor = new Proveedor(
                null, dto.rut(), dto.razonSocial(), dto.emailContacto(), dto.telefono()
        );
        return proveedorRepository.save(proveedor);
    }

    public OrdenCompra emitirOrdenCompra(OrdenCompraRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(dto.proveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado en la base de datos"));

        OrdenCompra orden = new OrdenCompra(
                null, proveedor, dto.repuestoId(), dto.cantidadSolicitada(),
                OrdenCompra.EstadoOrden.SOLICITADA, LocalDateTime.now()
        );
        return ordenCompraRepository.save(orden);
    }

    public OrdenCompra marcarComoRecibida(Long ordenId) {
        OrdenCompra orden = ordenCompraRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        if (orden.getEstado() == OrdenCompra.EstadoOrden.RECIBIDA) {
            throw new RuntimeException("La orden ya fue marcada como recibida anteriormente.");
        }

        orden.setEstado(OrdenCompra.EstadoOrden.RECIBIDA);
        
        // NOTA ESTRATÉGICA: Aquí, en el futuro, podríamos inyectar un WebClient 
        // para llamar a inventory-service y sumarle el stock de estas piezas al inventario.

        return ordenCompraRepository.save(orden);
    }
}