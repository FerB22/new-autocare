package com.autocare.booking_service.assembler;

import com.autocare.booking_service.controller.CitaController;
import com.autocare.booking_service.model.Cita;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CitaModelAssembler implements RepresentationModelAssembler<Cita, EntityModel<Cita>> {

    @Override
    public EntityModel<Cita> toModel(Cita cita) {
        return EntityModel.of(cita,
                // Cambiado a obtenerCitaPorId para coincidir con tu controlador
                linkTo(methodOn(CitaController.class).obtenerCitaPorId(cita.getId())).withSelfRel(),
                // Cambiado a obtenerTodas para coincidir con tu controlador
                linkTo(methodOn(CitaController.class).obtenerTodas()).withRel("todas-las-citas")
        );
    }

    @Override
    public CollectionModel<EntityModel<Cita>> toCollectionModel(Iterable<? extends Cita> entities) {
        CollectionModel<EntityModel<Cita>> collectionModel = RepresentationModelAssembler.super.toCollectionModel(entities);
        // Cambiado a obtenerTodas para coincidir con tu controlador
        collectionModel.add(linkTo(methodOn(CitaController.class).obtenerTodas()).withSelfRel());
        return collectionModel;
    }
}