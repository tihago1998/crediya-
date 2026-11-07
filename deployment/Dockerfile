# ============================================================
# Etapa 1 (build): compila el proyecto con Gradle dentro de Docker.
# Así no es necesario tener Java 25 instalado en la máquina (WSL/Ubuntu).
# ============================================================
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

# Se copia el código fuente del proyecto (lo excluido está en .dockerignore)
COPY . .

# Asegura finales de línea Unix en gradlew (por si se clonó en Windows) y permisos de ejecución.
# Luego genera el jar ejecutable del módulo app-service sin correr las pruebas.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew :app-service:bootJar -x test --no-daemon \
    && find applications/app-service/build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} /workspace/app.jar \;

# ============================================================
# Etapa 2 (runtime): imagen liviana solo con el JRE y el jar.
# ============================================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Se crea un usuario sin privilegios para no ejecutar el contenedor como root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /workspace/app.jar /app/crediya.jar

# Opciones de la JVM: respeta los límites de memoria del contenedor
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser
EXPOSE 8080
ENTRYPOINT [ "/bin/sh", "-c", "java $JAVA_OPTS -jar /app/crediya.jar" ]
