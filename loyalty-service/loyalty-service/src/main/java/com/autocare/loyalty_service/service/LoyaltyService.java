package com.autocare.loyalty_service.service;

import com.autocare.loyalty_service.dto.CrearPerfilDTO;
import com.autocare.loyalty_service.dto.TransaccionPuntosDTO;
import com.autocare.loyalty_service.model.PerfilLealtad;
import com.autocare.loyalty_service.repository.PerfilLealtadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final PerfilLealtadRepository repository;

    public PerfilLealtad obtenerPerfil(Long clienteId) {
        return repository.findByClienteId(clienteId)
                .orElseThrow(() -> new RuntimeException("Perfil de lealtad no encontrado para el cliente: " + clienteId));
    }

    public PerfilLealtad inicializarPerfil(CrearPerfilDTO dto) {
        Optional<PerfilLealtad> existente = repository.findByClienteId(dto.clienteId());
        if (existente.isPresent()) {
            throw new RuntimeException("El cliente ya tiene un perfil de lealtad.");
        }

        PerfilLealtad nuevoPerfil = new PerfilLealtad(
                null,
                dto.clienteId(),
                0,
                PerfilLealtad.NivelLealtad.BRONCE,
                LocalDateTime.now()
        );
        return repository.save(nuevoPerfil);
    }

    public PerfilLealtad sumarPuntos(Long clienteId, TransaccionPuntosDTO dto) {
        PerfilLealtad perfil = obtenerPerfil(clienteId);
        
        perfil.setPuntosAcumulados(perfil.getPuntosAcumulados() + dto.cantidadPuntos());
        actualizarNivel(perfil);
        perfil.setUltimaActualizacion(LocalDateTime.now());
        
        return repository.save(perfil);
    }

    public PerfilLealtad canjearPuntos(Long clienteId, TransaccionPuntosDTO dto) {
        PerfilLealtad perfil = obtenerPerfil(clienteId);
        
        if (perfil.getPuntosAcumulados() < dto.cantidadPuntos()) {
            throw new RuntimeException("Puntos insuficientes para el canje.");
        }
        
        perfil.setPuntosAcumulados(perfil.getPuntosAcumulados() - dto.cantidadPuntos());
        actualizarNivel(perfil);
        perfil.setUltimaActualizacion(LocalDateTime.now());
        
        return repository.save(perfil);
    }

    // Regla de negocio para la evolución
    private void actualizarNivel(PerfilLealtad perfil) {
        int puntos = perfil.getPuntosAcumulados();
        if (puntos >= 5000) {
            perfil.setNivel(PerfilLealtad.NivelLealtad.VIP);
        } else if (puntos >= 2000) {
            perfil.setNivel(PerfilLealtad.NivelLealtad.ORO);
        } else if (puntos >= 500) {
            perfil.setNivel(PerfilLealtad.NivelLealtad.PLATA);
        } else {
            perfil.setNivel(PerfilLealtad.NivelLealtad.BRONCE);
        }
    }
}