# ==========================================
# ETAPA 1: COMPILACIÓN (Builder)
# ==========================================
# ¡Actualizado a Java 21!
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: EJECUCIÓN (Runtime)
# ==========================================
# ¡Actualizado a Java 21!
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]