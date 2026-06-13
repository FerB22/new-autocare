package com.autocare.booking_service.service;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.exception.CitaNoEncontradaException;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository repository;

    public List<Cita> obtenerTodas() {
        return repository.findAll();
    }

    public Cita agendarCita(CitaRequestDTO dto) {
        Cita cita = new Cita();
        cita.setClienteId(dto.clienteId());
        cita.setVehiculoId(dto.vehiculoId());
        cita.setFechaHora(dto.fechaHora());
        cita.setMotivo(dto.motivo());
        cita.setEstado(Cita.EstadoCita.AGENDADA);
        
        return repository.save(cita);
    }

    public Cita obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException("No se encontró la cita con ID: " + id));
    }

    public Cita actualizarCita(Long id, CitaRequestDTO dto) {
        Cita citaExistente = obtenerPorId(id);
        
        citaExistente.setClienteId(dto.clienteId());
        citaExistente.setVehiculoId(dto.vehiculoId());
        citaExistente.setFechaHora(dto.fechaHora());
        citaExistente.setMotivo(dto.motivo());
        
        return repository.save(citaExistente);
    }

    public void eliminarCita(Long id) {
        Cita citaExistente = obtenerPorId(id);
        repository.delete(citaExistente);
    }
}