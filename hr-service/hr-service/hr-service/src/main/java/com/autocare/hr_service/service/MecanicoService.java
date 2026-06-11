package com.autocare.hr_service.service;

import com.autocare.hr_service.model.Mecanico;
import com.autocare.hr_service.repository.MecanicoRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MecanicoService {

    private static final List<String> ESPECIALIDADES_VALIDAS = List.of(
        "MOTOR", "FRENOS", "ELECTRICO", "SUSPENSION", "TRANSMISION", "CARROCERIA", "GENERAL"
    );

    private final MecanicoRepository mecanicoRepository;

    public MecanicoService(MecanicoRepository mecanicoRepository) {
        this.mecanicoRepository = mecanicoRepository;
    }

    public List<Mecanico> listarTodos() {
        return mecanicoRepository.findAll();
    }

    public Optional<Mecanico> buscarPorId(Long id) { // Cambiado a Long
        return mecanicoRepository.findById(id);
    }

    public List<Mecanico> buscarDisponibles() {
        // Asumiendo que tu repositorio tiene: List<Mecanico> findByEstaDisponible(boolean disponible);
        // Si no lo tiene, agrégalo a la interfaz MecanicoRepository.
        return mecanicoRepository.findAll().stream()
                .filter(Mecanico::isEstaDisponible)
                .toList();
    }

    public List<Mecanico> buscarPorEspecialidad(String especialidad) {
        String especialidadNormalizada = validarEspecialidad(especialidad);
        // Asumiendo que agregaste findByEspecialidad(String esp) al Repository
        return mecanicoRepository.findAll().stream()
                .filter(m -> m.getEspecialidad().equals(especialidadNormalizada))
                .toList();
    }

    public Mecanico guardar(Mecanico mecanico) {
        log.info("Guardando nuevo mecánico: {} {}", mecanico.getNombre(), mecanico.getApellido());

        mecanico.setEspecialidad(validarEspecialidad(mecanico.getEspecialidad()));

        // Validar duplicado por Documento de Identidad (mucho más preciso que por nombre)
        boolean documentoDuplicado = mecanicoRepository.findAll()
                .stream()
                .anyMatch(m -> m.getDocumentoIdentidad().equals(mecanico.getDocumentoIdentidad()));

        if (documentoDuplicado) {
            throw new RuntimeException("Ya existe un mecánico con el documento: " + mecanico.getDocumentoIdentidad());
        }

        mecanico.setEstaDisponible(true); // Por defecto entra disponible
        return mecanicoRepository.save(mecanico);
    }

    public Mecanico cambiarDisponibilidad(Long id, boolean disponible) {
        Mecanico mecanico = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Mecánico no encontrado con ID: " + id));

        if (mecanico.isEstaDisponible() == disponible) {
            throw new RuntimeException("El mecánico ya tiene el estado solicitado.");
        }

        mecanico.setEstaDisponible(disponible);
        return mecanicoRepository.save(mecanico);
    }

    public Mecanico actualizar(Long id, Mecanico datos) {
        Mecanico existente = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Mecánico no encontrado con ID: " + id));

        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setTelefono(datos.getTelefono());
        existente.setEspecialidad(validarEspecialidad(datos.getEspecialidad()));
        
        return mecanicoRepository.save(existente);
    }

    public void eliminar(Long id) {
        Mecanico mecanico = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Mecánico no encontrado con ID: " + id));

        // Tu excelente regla de negocio: no borrar mecánicos ocupados
        if (!mecanico.isEstaDisponible()) {
            throw new RuntimeException("No se puede eliminar porque está asignado a una orden. Libere al mecánico primero.");
        }

        mecanicoRepository.deleteById(id);
    }

    // Método auxiliar para centralizar la validación
    private String validarEspecialidad(String especialidad) {
        String normalizada = especialidad.toUpperCase().trim();
        if (!ESPECIALIDADES_VALIDAS.contains(normalizada)) {
            throw new RuntimeException("Especialidad inválida. Permitidas: " + ESPECIALIDADES_VALIDAS);
        }
        return normalizada;
    }
}