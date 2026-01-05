# Guía de Inicialización de PostgreSQL en Azure

Esta guía te ayudará a configurar y ejecutar la base de datos PostgreSQL en Azure Database for PostgreSQL para el sistema de gestión de moteles.

## 📋 Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Configuración de Azure Database for PostgreSQL](#configuración-de-azure-database-for-postgresql)
- [Instalación del Script](#instalación-del-script)
- [Uso del Script](#uso-del-script)
- [Configuración de la Aplicación Spring Boot](#configuración-de-la-aplicación-spring-boot)
- [Solución de Problemas](#solución-de-problemas)
- [Seguridad](#seguridad)

## 🔧 Requisitos Previos

### 1. Cliente PostgreSQL

Instala el cliente PostgreSQL en tu sistema:

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install postgresql-client
```

**CentOS/RHEL:**
```bash
sudo yum install postgresql
```

**macOS:**
```bash
brew install postgresql
```

**Windows:**
- Descarga desde [postgresql.org](https://www.postgresql.org/download/windows/)
- O usa el instalador de [EDB](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)

### 2. Servidor Azure Database for PostgreSQL

Necesitas tener creado un servidor de Azure Database for PostgreSQL. Si no lo tienes, crea uno siguiendo estos pasos:

#### Crear el servidor en Azure Portal:

1. Ve a [Azure Portal](https://portal.azure.com)
2. Busca "Azure Database for PostgreSQL"
3. Haz clic en "Crear" → "Servidor flexible"
4. Configura:
   - **Nombre del servidor**: `tu-servidor-motel` (será: tu-servidor-motel.postgres.database.azure.com)
   - **Región**: Selecciona la más cercana a tus usuarios
   - **Versión de PostgreSQL**: 15 o superior (recomendado: 16)
   - **Proceso y almacenamiento**: 
     - Para desarrollo: Flexible (B1ms - 1 vCores, 2 GiB RAM)
     - Para producción: General Purpose o Memory Optimized
   - **Usuario administrador**: `postgres` (o el que prefieras)
   - **Contraseña**: Usa una contraseña segura
   - **Conectividad**:
     - Método de conectividad: Acceso público
     - Reglas de firewall: Agrega tu IP actual

5. Haz clic en "Revisar y crear" → "Crear"

#### Crear el servidor con Azure CLI:

```bash
# Variables
RESOURCE_GROUP="motel-management-rg"
SERVER_NAME="tu-servidor-motel"
LOCATION="eastus"
ADMIN_USER="postgres"
ADMIN_PASSWORD="TuPasswordSeguro123!"

# Crear grupo de recursos
az group create --name $RESOURCE_GROUP --location $LOCATION

# Crear servidor PostgreSQL
az postgres flexible-server create \
  --resource-group $RESOURCE_GROUP \
  --name $SERVER_NAME \
  --location $LOCATION \
  --admin-user $ADMIN_USER \
  --admin-password $ADMIN_PASSWORD \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --storage-size 32 \
  --version 16 \
  --public-access 0.0.0.0

# Configurar regla de firewall para tu IP
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --name $SERVER_NAME \
  --rule-name AllowMyIP \
  --start-ip-address $(curl -s https://api.ipify.org) \
  --end-ip-address $(curl -s https://api.ipify.org)
```

## 🚀 Instalación del Script

### 1. Clonar o navegar al repositorio

```bash
cd /ruta/a/ubik-backendconfig/microservicios/microreactivo/motelManegement
```

### 2. Configurar variables de entorno

Copia el template de configuración:

```bash
cp .env.azure.template .env.azure
```

Edita `.env.azure` con tus credenciales reales:

```bash
nano .env.azure
# o
vim .env.azure
# o usa tu editor favorito
code .env.azure
```

Configura los siguientes valores:

```bash
# Servidor de PostgreSQL en Azure
AZURE_PG_SERVER=tu-servidor-motel.postgres.database.azure.com

# Usuario administrador
AZURE_PG_ADMIN_USER=postgres

# Contraseña del usuario administrador
AZURE_PG_ADMIN_PASSWORD=TuPasswordSeguro123!

# Nombre de la base de datos
AZURE_PG_DATABASE=motel_management_db

# Puerto (por defecto 5432)
AZURE_PG_PORT=5432

# Modo SSL (requerido para Azure)
AZURE_PG_SSL_MODE=require
```

### 3. Cargar las variables de entorno

```bash
source .env.azure
```

## 🎯 Uso del Script

### Inicialización Completa

Ejecuta el script principal de inicialización:

```bash
./init-postgresql-azure.sh
```

El script realizará automáticamente:

1. ✅ Validación de prerequisitos
2. ✅ Prueba de conexión al servidor Azure
3. ✅ Creación de la base de datos
4. ✅ Inicialización del esquema (tablas, índices, constraints)
5. ✅ Carga de datos de ejemplo
6. ✅ Verificación de la instalación
7. ✅ Muestra información de conexión

### Salida Esperada

```
==============================================
Inicialización de PostgreSQL en Azure
==============================================

ℹ️  Cargando configuración desde .env.azure

==============================================
Validando prerequisitos
==============================================

✅ psql está instalado
✅ Variable AZURE_PG_SERVER configurada: tu-servidor.postgres.database.azure.com
✅ Variables de autenticación configuradas
✅ Script SQL encontrado: ./src/main/resources/Postgres-init-motel.sql

==============================================
Probando conexión a Azure PostgreSQL
==============================================

✅ Conexión exitosa a Azure PostgreSQL
ℹ️  Versión: PostgreSQL 16.1 on x86_64-pc-linux-gnu

==============================================
Creando base de datos
==============================================

ℹ️  Creando base de datos 'motel_management_db'...
✅ Base de datos 'motel_management_db' creada exitosamente

==============================================
Inicializando esquema y datos
==============================================

[... salida SQL ...]
✅ Esquema y datos iniciales creados exitosamente

==============================================
Verificando instalación
==============================================

✅ Esquema creado: 5 tablas encontradas
[... listado de tablas ...]
✅ Datos de ejemplo cargados: 5 moteles

==============================================
¡Inicialización completada exitosamente!
==============================================
✅ La base de datos está lista para usarse
```

## ⚙️ Configuración de la Aplicación Spring Boot

### Opción 1: Variables de Entorno (Recomendado para Producción)

Configura estas variables de entorno antes de ejecutar la aplicación:

```bash
export R2DBC_URL='r2dbc:postgresql://tu-servidor.postgres.database.azure.com:5432/motel_management_db?sslmode=require'
export R2DBC_USERNAME='postgres'
export R2DBC_PASSWORD='TuPasswordSeguro123!'

# Ejecutar la aplicación
./mvnw spring-boot:run
```

### Opción 2: Crear un Perfil Azure en Spring Boot

Crea `src/main/resources/application-azure.yml`:

```yaml
server:
  port: 8084

spring:
  application:
    name: motel-management-service

  r2dbc:
    url: r2dbc:postgresql://tu-servidor.postgres.database.azure.com:5432/motel_management_db?sslmode=require
    username: postgres
    password: ${AZURE_PG_PASSWORD:TuPasswordSeguro123!}
    pool:
      initial-size: 5
      max-size: 10
      max-idle-time: 30m

  sql:
    init:
      mode: never  # La BD ya está inicializada

logging:
  level:
    com.ubik.usermanagement: INFO
    org.springframework.r2dbc: WARN
    io.r2dbc.postgresql: WARN
```

Ejecutar con el perfil Azure:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=azure
```

### Opción 3: Usando Azure App Service

Si despliegas en Azure App Service, configura las variables de entorno en el portal:

1. Ve a tu App Service en Azure Portal
2. En "Configuración" → "Configuración de la aplicación"
3. Agrega:
   - `R2DBC_URL`: `r2dbc:postgresql://tu-servidor.postgres.database.azure.com:5432/motel_management_db?sslmode=require`
   - `R2DBC_USERNAME`: `postgres`
   - `R2DBC_PASSWORD`: `TuPasswordSeguro123!`

## 🔍 Solución de Problemas

### Error: "psql: could not connect to server: Connection refused"

**Causa**: Las reglas de firewall de Azure no permiten tu IP.

**Solución**:
```bash
# Obtener tu IP pública
curl https://api.ipify.org

# Agregar regla de firewall en Azure Portal
# O con CLI:
az postgres flexible-server firewall-rule create \
  --resource-group tu-resource-group \
  --name tu-servidor \
  --rule-name AllowMyIP \
  --start-ip-address TU_IP \
  --end-ip-address TU_IP
```

### Error: "psql: FATAL: SSL connection is required"

**Causa**: Azure requiere conexiones SSL pero no está configurado correctamente.

**Solución**: Asegúrate de que `AZURE_PG_SSL_MODE=require` en tu `.env.azure`

### Error: "FATAL: password authentication failed"

**Causa**: Usuario o contraseña incorrectos.

**Solución**: 
1. Verifica las credenciales en `.env.azure`
2. Para Azure, el formato del usuario puede ser: `usuario@servidor` o solo `usuario`
3. Resetea la contraseña en Azure Portal si es necesario

### Error: "database does not exist"

**Causa**: La base de datos no fue creada correctamente.

**Solución**: Ejecuta manualmente:
```bash
PGPASSWORD='password' psql \
  -h tu-servidor.postgres.database.azure.com \
  -U postgres \
  -d postgres \
  --set=sslmode=require \
  -c "CREATE DATABASE motel_management_db;"
```

### Verificar Conexión Manualmente

```bash
# Conectarse a la base de datos
PGPASSWORD='TuPassword' psql \
  -h tu-servidor.postgres.database.azure.com \
  -U postgres \
  -d motel_management_db \
  --set=sslmode=require

# Una vez conectado, verificar tablas:
\dt

# Ver datos:
SELECT * FROM motel;
SELECT * FROM room;
SELECT * FROM service;
```

## 🔐 Seguridad

### Mejores Prácticas

1. **No subas credenciales al repositorio**
   - El archivo `.env.azure` está en `.gitignore`
   - Nunca hagas commit de archivos con credenciales

2. **Usa contraseñas seguras**
   - Mínimo 12 caracteres
   - Incluye mayúsculas, minúsculas, números y símbolos

3. **Configura reglas de firewall restrictivas**
   - Solo permite IPs necesarias
   - Usa VNet integration para App Services

4. **Usa Azure Key Vault**
   - Almacena credenciales en Key Vault
   - Referencia los secretos en tu aplicación

5. **Habilita auditoría en Azure**
   - Monitorea accesos a la base de datos
   - Configura alertas para actividades sospechosas

6. **Backups automáticos**
   - Azure hace backups automáticos
   - Configura retención según tus necesidades

### Rotación de Credenciales

Para cambiar la contraseña:

```bash
# En Azure Portal o con CLI:
az postgres flexible-server update \
  --resource-group tu-resource-group \
  --name tu-servidor \
  --admin-password NuevaPasswordSegura123!
```

Luego actualiza `.env.azure` y reinicia tu aplicación.

## 📚 Referencias

- [Azure Database for PostgreSQL Documentation](https://docs.microsoft.com/azure/postgresql/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [R2DBC PostgreSQL Driver](https://github.com/pgjdbc/r2dbc-postgresql)

## 🆘 Soporte

Si encuentras problemas:

1. Revisa los logs del script
2. Verifica las reglas de firewall en Azure
3. Consulta la documentación de Azure Database for PostgreSQL
4. Contacta al equipo de desarrollo

---

**Nota**: Este script está diseñado específicamente para Azure Database for PostgreSQL. Para otros proveedores cloud o instalaciones on-premise, podrían necesitarse ajustes.
