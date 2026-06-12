package com.autocare.hr_service.config;

import com.autocare.hr_service.model.Mecanico;
import com.autocare.hr_service.repository.MecanicoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final MecanicoRepository mecanicoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Idempotencia: Verificamos que la tabla esté vacía
        if (mecanicoRepository.count() == 0) {
            log.info("Iniciando la contratación simulada de mecánicos...");
            
            Faker faker = new Faker(new Locale("es"));

            // Arreglo con especialidades clásicas de un taller integral
            String[] especialidades = {
                "Mecánica General", 
                "Electromecánico", 
                "Especialista en Frenos", 
                "Transmisión y Embragues", 
                "Suspensión y Dirección",
                "Diagnóstico Computarizado"
            };

            // Vamos a contratar a 6 mecánicos (uno para cada especialidad)
            for (int i = 0; i < especialidades.length; i++) {
                Mecanico mecanico = new Mecanico();
                
                mecanico.setDocumentoIdentidad(faker.idNumber().valid());
                mecanico.setNombre(faker.name().firstName());
                mecanico.setApellido(faker.name().lastName());
                mecanico.setTelefono(faker.phoneNumber().cellPhone());
                
                // Asignamos la especialidad correspondiente de la lista
                mecanico.setEspecialidad(especialidades[i]);
                
                // Para efectos de tu demostración técnica, es mejor que todos 
                // arranquen estando disponibles para recibir trabajo.
                mecanico.setEstaDisponible(true);
                
                mecanicoRepository.save(mecanico);
            }
            
            log.info("¡Plantilla de mecánicos lista! 6 especialistas han sido registrados exitosamente.");
        } else {
            log.info("La base de datos ya cuenta con personal registrado. Se omite DataLoader.");
        }
    }
}