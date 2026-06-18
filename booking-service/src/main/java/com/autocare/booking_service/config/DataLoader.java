package com.autocare.booking_service.config;

import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.repository.CitaRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initData(CitaRepository citaRepository) {
        return args -> {
            // Se verifica si la tabla ya tiene registros para evitar duplicados en cada reinicio
            if (citaRepository.count() == 0) {
                Faker faker = new Faker();
                List<Cita> citasList = new ArrayList<>();

                for (int i = 0; i < 15; i++) {
                    Cita cita = new Cita();
                    
                    // Asignación de IDs aleatorios de tipo Long para mantener la cohesión con garage-service
                    cita.setVehiculoId(faker.number().numberBetween(1L, 50L));
                    cita.setClienteId(faker.number().numberBetween(1L, 50L));
                    
                    // Generación de una fecha y hora aleatoria utilizando LocalDateTime
                    // Se simulan citas entre 5 días en el pasado y 30 días en el futuro en horario laboral
                    int diasAleatorios = faker.number().numberBetween(-5, 30);
                    int horasAleatorias = faker.number().numberBetween(8, 18); 
                    cita.setFechaHora(LocalDateTime.now().plusDays(diasAleatorios).withHour(horasAleatorias).withMinute(0).withSecond(0));
                    
                    // Generación de un motivo descriptivo en texto
                    cita.setMotivo(faker.lorem().sentence(4));
                    
                    // Selección aleatoria de un estado desde el enum EstadoCita
                    Cita.EstadoCita[] estados = Cita.EstadoCita.values();
                    cita.setEstado(estados[faker.random().nextInt(estados.length)]);

                    citasList.add(cita);
                }

                citaRepository.saveAll(citasList);
                System.out.println("Datos falsos de Citas cargados exitosamente en la base de datos.");
            }
        };
    }
}