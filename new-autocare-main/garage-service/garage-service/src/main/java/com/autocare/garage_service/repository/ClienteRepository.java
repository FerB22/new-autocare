package com.autocare.garage_service.repository;

import com.autocare.garage_service.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDocumentoIdentidad(String documentoIdentidad);
    Optional<Cliente> findByEmail(String email);
}