package com.autocare.billing_service.service;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.exception.RecursoNoEncontradoException;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.repository.FacturaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;

    // ─────────────────────────────────────────
    //  LECTURA
    // ─────────────────────────────────────────

    public List<Factura> listarTodas() {
        log.info("Listando todas las facturas");
        return facturaRepository.findAll();
    }

    public Optional<Factura> buscarPorId(Long id) {
        if (id == null) throw new IllegalArgumentException("El ID no puede ser nulo");
        
        log.info("Buscando factura con ID: {}", id);
        return facturaRepository.findById(id);
    }

    public List<Factura> buscarPorEstado(Factura.EstadoPago estado) {
        log.info("Buscando facturas con estado: {}", estado);
        return facturaRepository.findByEstado(estado);
    }

    // ─────────────────────────────────────────
    //  GENERACIÓN CON REGLAS DE NEGOCIO
    // ─────────────────────────────────────────

    public Factura generar(FacturaRequestDTO dto) {
        log.info("Generando factura para orden de trabajo: {}", dto.ordenTrabajoId());

        boolean yaFacturada = facturaRepository
                .findByOrdenTrabajoId(dto.ordenTrabajoId())
                .isPresent();

        if (yaFacturada) {
            log.warn("La orden {} ya tiene una factura generada", dto.ordenTrabajoId());
            throw new RuntimeException(
                "Ya existe una factura para la orden: " + dto.ordenTrabajoId() +
                ". No se puede facturar dos veces el mismo trabajo."
            );
        }

        if (dto.subtotal().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Intento de cobro con monto $0 para la orden: {}", dto.ordenTrabajoId());
            throw new RuntimeException("No se puede generar una factura con monto $0 o negativo.");
        }

        Factura factura = new Factura();
        factura.setOrdenTrabajoId(dto.ordenTrabajoId());
        factura.setSubtotal(dto.subtotal());
        factura.setImpuestos(dto.impuestos());
        factura.setTotal(dto.subtotal().add(dto.impuestos()));
        factura.setEstado(Factura.EstadoPago.PENDIENTE);
        factura.setFechaEmision(LocalDateTime.now());

        log.info("Factura calculada — total: {}", factura.getTotal());
        return facturaRepository.save(factura);
    }

    // ─────────────────────────────────────────
    //  PAGO Y ANULACIÓN
    // ─────────────────────────────────────────

    public Factura pagarFactura(Long id) {
        log.info("Procesando pago de factura con ID: {}", id);

        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura no encontrada con ID: " + id));

        if (factura.getEstado() == Factura.EstadoPago.PAGADA) {
            log.warn("Intento de pagar una factura ya pagada: {}", id);
            throw new RuntimeException("La factura ya fue PAGADA. No se puede procesar el mismo pago dos veces.");
        }

        if (factura.getEstado() == Factura.EstadoPago.ANULADA) {
            log.warn("Intento de pagar una factura anulada: {}", id);
            throw new RuntimeException("La factura está ANULADA y no puede ser pagada.");
        }

        factura.setEstado(Factura.EstadoPago.PAGADA);
        log.info("Factura {} marcada como PAGADA exitosamente", id);
        return facturaRepository.save(factura);
    }

    public Factura anularFactura(Long id) {
        log.info("Anulando factura con ID: {}", id);

        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura no encontrada con ID: " + id));

        if (factura.getEstado() == Factura.EstadoPago.PAGADA) {
            log.warn("Intento de anular una factura ya pagada: {}", id);
            throw new RuntimeException("No se puede anular una factura que ya fue PAGADA.");
        }

        if (factura.getEstado() == Factura.EstadoPago.ANULADA) {
            throw new RuntimeException("La factura ya se encuentra ANULADA.");
        }

        factura.setEstado(Factura.EstadoPago.ANULADA);
        log.info("Factura {} anulada exitosamente", id);
        return facturaRepository.save(factura);
    }

    // ─────────────────────────────────────────
    //  ELIMINACIÓN
    // ─────────────────────────────────────────

    public void eliminar(Long id) {
        log.info("Eliminando factura con ID: {}", id);

        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura no encontrada con ID: " + id));

        if (factura.getEstado() == Factura.EstadoPago.PAGADA) {
            throw new RuntimeException("No se puede eliminar una factura PAGADA. Es un documento contable.");
        }

        facturaRepository.deleteById(id);
        log.info("Factura {} eliminada del sistema", id);
    }
}