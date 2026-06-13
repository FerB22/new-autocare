package com.autocare.booking_service.controller;

import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public ResponseEntity<EntityModel<Cita>> crearCita(@RequestBody CitaRequestDTO citaDTO) {
        Cita nuevaCita = citaService.agendarCita(citaDTO);
        EntityModel<Cita> recurso = EntityModel.of(nuevaCita);
        
        recurso.add(linkTo(methodOn(CitaController.class).obtenerCitaPorId(nuevaCita.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(CitaController.class).obtenerTodas()).withRel("todas-las-citas"));
        
        return new ResponseEntity<>(recurso, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cita>> obtenerCitaPorId(@PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        EntityModel<Cita> recurso = EntityModel.of(cita);

        recurso.add(linkTo(methodOn(CitaController.class).obtenerCitaPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(CitaController.class).obtenerTodas()).withRel("todas-las-citas"));

        return ResponseEntity.ok(recurso);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cita>>> obtenerTodas() {
        List<Cita> citas = citaService.obtenerTodas();
        
        // Usamos una lista tradicional para evadir el error del compilador con lambdas y varargs
        List<EntityModel<Cita>> citasRecursos = new ArrayList<>();
        for (Cita cita : citas) {
            EntityModel<Cita> recurso = EntityModel.of(cita);
            recurso.add(linkTo(methodOn(CitaController.class).obtenerCitaPorId(cita.getId())).withSelfRel());
            citasRecursos.add(recurso);
        }

        CollectionModel<EntityModel<Cita>> modeloColeccion = CollectionModel.of(citasRecursos);
        modeloColeccion.add(linkTo(methodOn(CitaController.class).obtenerTodas()).withSelfRel());

        return ResponseEntity.ok(modeloColeccion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Cita>> actualizarCita(@PathVariable Long id, @RequestBody CitaRequestDTO citaDTO) {
        Cita citaActualizada = citaService.actualizarCita(id, citaDTO);
        EntityModel<Cita> recurso = EntityModel.of(citaActualizada);

        recurso.add(linkTo(methodOn(CitaController.class).obtenerCitaPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(CitaController.class).obtenerTodas()).withRel("todas-las-citas"));

        return ResponseEntity.ok(recurso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }
}