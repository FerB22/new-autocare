# 1. Imagen base: Usamos JRE 17 sobre Alpine Linux (ligera y segura)
FROM eclipse-temurin:17-jre-alpine

# 2. Directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos el archivo .jar generado por Maven al contenedor
COPY target/*.jar app.jar

# 4. Exponemos el puerto estándar de Eureka (útil como documentación)
EXPOSE 8761

# 5. Comando para arrancar el servidor de descubrimiento
ENTRYPOINT ["java", "-jar", "app.jar"]