# 1. Imagen base: Usamos una versión ligera de Java (JRE 17 en Alpine Linux)
FROM eclipse-temurin:17-jre-alpine

# 2. Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos el ejecutable compilado (.jar) desde la carpeta target al contenedor
# El asterisco (*) hace que este Dockerfile sea universal para todos tus servicios
COPY target/*.jar app.jar

# 4. Comando de arranque: Le dice a Docker cómo iniciar la aplicación Java
ENTRYPOINT ["java", "-jar", "app.jar"]