package com.autocare.inventory_service.config;

import com.autocare.inventory_service.model.Repuesto;
import com.autocare.inventory_service.repository.RepuestoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RepuestoRepository repuestoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Idempotencia: Solo poblamos si la base de datos está vacía
        if (repuestoRepository.count() == 0) {
            log.info("Iniciando la carga de repuestos simulados en bodega...");
            
            Faker faker = new Faker(new Locale("es"));

            // Definimos un catálogo inicial realista para un taller
            String[] categorias = {
                "Filtro de Aceite", "Filtro de Aire", "Pastillas de Freno", 
                "Bujías", "Correa de Distribución", "Batería 12V", 
                "Amortiguador Delantero", "Disco de Freno", "Neumático aro 15", 
                "Aceite de Motor Sintético 5W30"
            };

            for (String categoria : categorias) {
                Repuesto repuesto = new Repuesto();
                
                // Generar un SKU tipo: FIL-2394
                String prefijoSku = categoria.substring(0, 3).toUpperCase();
                // bothify() reemplaza los '#' por números aleatorios
                repuesto.setCodigoSku(faker.bothify(prefijoSku + "-####")); 
                
                // Hacemos el nombre más realista añadiendo una marca de auto o fabricante
                repuesto.setNombre(categoria + " " + faker.vehicle().make());
                
                // Descripción ficticia
                repuesto.setDescripcion("Repuesto de alta calidad y durabilidad. " + faker.lorem().sentence());
                
                // Precio entre 15.00 y 150.00
                double precioRandom = faker.number().randomDouble(2, 15, 150);
                repuesto.setPrecioUnitario(BigDecimal.valueOf(precioRandom));
                
                // Stock abundante para pruebas y stock mínimo bajo
                repuesto.setStockActual(faker.number().numberBetween(40, 100));
                repuesto.setStockMinimo(faker.number().numberBetween(5, 15));
                
                repuestoRepository.save(repuesto);
            }
            
            log.info("¡Catálogo de inventario inicializado! {} tipos de repuestos listos en bodega.", categorias.length);
        } else {
            log.info("La bodega ya contiene inventario. Se omite DataLoader.");
        }
    }
}