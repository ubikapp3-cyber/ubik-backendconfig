FROM eclipse-temurin:17-jre-alpine

# Metadatos
LABEL maintainer="ubik-team"
LABEL description="Notification Management Microservice"

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado
COPY target/notification-management-*.jar app.jar

# Exponer el puerto
EXPOSE 8085

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
