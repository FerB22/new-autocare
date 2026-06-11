package com.autocare.booking_service.service;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository repository;

    public List<Cita> obtenerTodas() {
        return repository.findAll();
    }

    public Optional<Cita> buscarPorId(Long id) {
        return repository.findById(id);
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

    public Cita cancelarCita(Long id) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new RuntimeException("La cita ya fue cancelada anteriormente.");
        }

        if (cita.getEstado() == Cita.EstadoCita.EJECUTADA) {
            throw new RuntimeException("No se puede cancelar una cita que ya fue ejecutada.");
        }

        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return repository.save(cita);
    }

    public void eliminarCita(Long id) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        if (cita.getEstado() == Cita.EstadoCita.EJECUTADA) {
            throw new RuntimeException("No se puede eliminar una cita que ya fue ejecutada.");
        }

        repository.deleteById(id);
    }
}