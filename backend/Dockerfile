# =========================================================
#  Dockerfile del backend.  Va en:  backend/Dockerfile
#
#  Render no tiene un entorno nativo de Java, así que se
#  despliega con Docker. Esto no complica nada: es una receta
#  de dos fases que Render ejecuta sola.
#
#  Fase 1 (build): una imagen con Maven compila el JAR.
#  Fase 2 (runtime): una imagen sólo con Java copia ese JAR.
#
#  Se hace en dos fases para que la imagen final no arrastre
#  Maven ni el código fuente: pesa ~200 MB en vez de ~800 MB.
# =========================================================

# ---------- Fase 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Primero sólo el pom.xml: si no cambian las dependencias,
# Docker reutiliza esta capa y no vuelve a descargarlas.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora el código fuente
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Fase 2: ejecutar ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos sólo el JAR de la fase anterior
COPY --from=build /app/target/*.jar app.jar

# Documentativo. El puerto real lo decide Render con $PORT.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
