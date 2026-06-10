package com.autocare.inventory_service.service;

import com.autocare.inventory_service.dto.RepuestoRequestDTO;
import com.autocare.inventory_service.dto.StockReductionDTO;
import com.autocare.inventory_service.model.Repuesto;
import com.autocare.inventory_service.repository.RepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepuestoService {

    private final RepuestoRepository repository;

    public List<Repuesto> obtenerTodos() {
        return repository.findAll();
    }

    public Repuesto crearRepuesto(RepuestoRequestDTO dto) {
        Repuesto repuesto = new Repuesto(
                null,
                dto.codigoSku(),
                dto.nombre(),
                dto.descripcion(),
                dto.precioUnitario(),
                dto.stockActual(),
                dto.stockMinimo()
        );
        return repository.save(repuesto);
    }

    // El método estrella para la cohesión del ecosistema
    @Transactional
    public void reducirStock(StockReductionDTO dto) {
        Repuesto repuesto = repository.findById(dto.repuestoId())
                .orElseThrow(() -> new RuntimeException("Repuesto no encontrado en el radar"));

        if (repuesto.getStockActual() < dto.cantidadUtilizada()) {
            throw new RuntimeException("Stock insuficiente para el repuesto: " + repuesto.getNombre());
        }

        repuesto.setStockActual(repuesto.getStockActual() - dto.cantidadUtilizada());
        repository.save(repuesto);

        // Aquí, más adelante, podríamos lanzar un evento si el stockActual < stockMinimo
        // para que el procurement-service inicie una compra.
    }
}