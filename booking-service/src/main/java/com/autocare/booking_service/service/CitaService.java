package com.autocare.booking_service.service;

import com.autocare.booking_service.dto.CitaRequestDTO;
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
}