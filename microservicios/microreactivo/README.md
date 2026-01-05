# microreactivo-2025

Plantilla 2025 con microservicios **Spring WebFlux** (funcional con `records`) + **API Gateway** (Spring Cloud Gateway Server WebFlux) y **R2DBC MySQL**.

## Requisitos
- JDK 21
- Maven 3.9+
- MySQL 8+

## Preparación de MySQL
```sql
-- ejecuta en tu MySQL local:
SOURCE mysql-init.sql;
```

## Cómo ejecutar (sin Docker)
En terminales separadas:
```bash
mvn -q -pl userManagement -am spring-boot:run
mvn -q -pl motelManegement -am spring-boot:run
mvn -q -pl gateway   -am spring-boot:run
```

Gateway: `http://localhost:8080/api/motels` etc.

## Pruebas
```bash
mvn -q -pl userManagement test
mvn -q -pl motelManegement test
mvn -q -pl gateway   test
```
