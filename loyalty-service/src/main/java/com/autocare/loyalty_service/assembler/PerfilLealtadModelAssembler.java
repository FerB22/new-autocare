package com.autocare.loyalty_service.assembler;

import com.autocare.loyalty_service.controller.LoyaltyController;
import com.autocare.loyalty_service.model.PerfilLealtad;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PerfilLealtadModelAssembler implements RepresentationModelAssembler<PerfilLealtad, EntityModel<PerfilLealtad>> {

    @Override
    public EntityModel<PerfilLealtad> toModel(PerfilLealtad perfil) {
        return EntityModel.of(perfil,
                linkTo(methodOn(LoyaltyController.class).consultarPerfil(perfil.getClienteId())).withSelfRel(),
                linkTo(methodOn(LoyaltyController.class).agregarPuntos(perfil.getClienteId(), null)).withRel("sumar-puntos"),
                linkTo(methodOn(LoyaltyController.class).restarPuntos(perfil.getClienteId(), null)).withRel("canjear-puntos")
        );
    }
}
