package com.autocare.garage_service.config;

import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.repository.ClienteRepository;
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

    private final ClienteRepository clienteRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo inyectamos datos si la base de datos está vacía
        if (clienteRepository.count() == 0) {
            log.info("Iniciando la carga de datos falsos (DataFaker)...");
            
            // Configuramos Faker en español
            Faker faker = new Faker(new Locale("es"));

            // Vamos a generar 15 clientes
            for (int i = 0; i < 15; i++) {
                Cliente cliente = new Cliente();
                // Usamos métodos de Faker para rellenar la entidad Cliente
                cliente.setDocumentoIdentidad(faker.idNumber().valid());
                cliente.setNombre(faker.name().firstName());
                cliente.setApellido(faker.name().lastName());
                cliente.setEmail(faker.internet().emailAddress());
                cliente.setTelefono(faker.phoneNumber().cellPhone());

                // Generar 1 o 2 vehículos por cada cliente
                int numVehiculos = faker.number().numberBetween(1, 3);
                for (int j = 0; j < numVehiculos; j++) {
                    Vehiculo vehiculo = new Vehiculo();
                    // Simulamos una patente con 4 letras y 2 números
                    vehiculo.setPatente(faker.regexify("[A-Z]{4}\\d{2}")); 
                    vehiculo.setMarca(faker.vehicle().make());
                    vehiculo.setModelo(faker.vehicle().model());
                    vehiculo.setAnio(faker.number().numberBetween(2010, 2024));
                    vehiculo.setColor(faker.color().name());
                    vehiculo.setVin(faker.vehicle().vin());
                    
                    // Relacionamos el vehículo con el cliente
                    vehiculo.setCliente(cliente);

                    // Agregamos el vehículo a la lista del cliente
                    cliente.getVehiculos().add(vehiculo);
                }
                
                // Guardamos el cliente. Como en tu entidad Cliente pusiste cascade = CascadeType.ALL, 
                // esto guardará automáticamente también los vehículos asociados.
                clienteRepository.save(cliente);
            }
            log.info("¡Datos cargados exitosamente! 15 clientes y sus vehículos han sido creados.");
        } else {
            log.info("La base de datos ya contiene información. Se omite DataLoader.");
        }
    }
}