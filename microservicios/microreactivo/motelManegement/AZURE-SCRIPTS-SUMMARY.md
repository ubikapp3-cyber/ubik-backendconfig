# Resumen de Scripts para Azure PostgreSQL

Este documento describe todos los archivos y scripts creados para facilitar la inicialización de la base de datos PostgreSQL en Azure.

## 📁 Archivos Creados

### 1. `init-postgresql-azure.sh` (Principal)
**Ubicación:** `microservicios/microreactivo/motelManegement/init-postgresql-azure.sh`  
**Propósito:** Script principal de inicialización de la base de datos en Azure

**Funcionalidades:**
- ✅ Valida prerequisitos (psql instalado, variables de entorno configuradas)
- ✅ Prueba la conexión a Azure PostgreSQL con SSL
- ✅ Crea la base de datos si no existe
- ✅ Ejecuta el script SQL de inicialización
- ✅ Verifica que tablas y datos fueron creados correctamente
- ✅ Muestra información de conexión para la aplicación Spring Boot
- ✅ Manejo robusto de errores con mensajes claros
- ✅ Interfaz colorida y amigable

**Uso:**
```bash
# Configurar variables de entorno primero
source .env.azure

# Ejecutar el script
./init-postgresql-azure.sh
```

### 2. `azure-quickstart.sh` (Asistente Interactivo)
**Ubicación:** `microservicios/microreactivo/motelManegement/azure-quickstart.sh`  
**Propósito:** Asistente interactivo para configuración rápida

**Funcionalidades:**
- ✅ Guía interactiva paso a paso
- ✅ Solicita todas las credenciales necesarias
- ✅ Crea automáticamente el archivo `.env.azure`
- ✅ Prueba la conexión después de configurar
- ✅ Opción para ejecutar la inicialización completa inmediatamente
- ✅ Manejo seguro de contraseñas (no se muestran en pantalla)

**Uso:**
```bash
./azure-quickstart.sh
```

### 3. `.env.azure.template` (Plantilla de Configuración)
**Ubicación:** `microservicios/microreactivo/motelManegement/.env.azure.template`  
**Propósito:** Plantilla para configurar variables de entorno

**Contenido:**
- Servidor Azure PostgreSQL
- Usuario administrador
- Contraseña
- Nombre de base de datos
- Puerto
- Modo SSL
- Ruta del script SQL

**Uso:**
```bash
# Copiar la plantilla
cp .env.azure.template .env.azure

# Editar con tus credenciales
nano .env.azure  # o vim, code, etc.

# Cargar las variables
source .env.azure
```

### 4. `azure-init-motel.sql` (Script SQL para Azure)
**Ubicación:** `microservicios/microreactivo/motelManegement/src/main/resources/azure-init-motel.sql`  
**Propósito:** Script SQL optimizado para Azure Database for PostgreSQL

**Características:**
- ✅ Esquema completo de tablas (motel, room, service, room_service, room_image)
- ✅ Índices optimizados para rendimiento
- ✅ Constraints y relaciones de integridad referencial
- ✅ Datos de ejemplo (5 moteles, 15 habitaciones, 15 servicios)
- ✅ Vista resumen (v_room_summary)
- ✅ Comentarios en tablas y columnas
- ✅ Verificación automática de datos insertados
- ✅ Compatible con Azure PostgreSQL

**Tablas creadas:**
1. `motel` - Información de moteles
2. `room` - Habitaciones de cada motel
3. `service` - Servicios y amenidades disponibles
4. `room_service` - Relación muchos-a-muchos entre habitaciones y servicios
5. `room_image` - Imágenes de habitaciones (estructura preparada)

### 5. `application-azure.yml` (Perfil Spring Boot)
**Ubicación:** `microservicios/microreactivo/motelManegement/src/main/resources/application-azure.yml`  
**Propósito:** Perfil de configuración de Spring Boot para Azure

**Características:**
- ✅ Configuración R2DBC para Azure PostgreSQL
- ✅ Pool de conexiones optimizado
- ✅ SSL habilitado por defecto
- ✅ Variables de entorno para credenciales sensibles
- ✅ Configuración de actuator para monitoreo
- ✅ Logging configurado para producción
- ✅ Schema initialization deshabilitado (ya inicializado por script)

**Uso:**
```bash
# Cargar variables de entorno
export R2DBC_URL='r2dbc:postgresql://servidor.postgres.database.azure.com:5432/motel_management_db?sslmode=require'
export R2DBC_USERNAME='postgres'
export R2DBC_PASSWORD='tu_password'

# Ejecutar con perfil azure
./mvnw spring-boot:run -Dspring-boot.run.profiles=azure
```

### 6. `README-AZURE.md` (Documentación Completa)
**Ubicación:** `microservicios/microreactivo/motelManegement/README-AZURE.md`  
**Propósito:** Guía completa para configuración y uso en Azure

**Contenido:**
- 📘 Requisitos previos
- 📘 Cómo crear servidor en Azure (Portal y CLI)
- 📘 Instrucciones de instalación paso a paso
- 📘 Guía de uso de todos los scripts
- 📘 Configuración de Spring Boot para Azure
- 📘 Solución de problemas comunes
- 📘 Mejores prácticas de seguridad
- 📘 Referencias y recursos adicionales

## 🔄 Flujo de Trabajo Completo

### Opción 1: Configuración Rápida (Recomendado)

```bash
# 1. Ejecutar el asistente interactivo
./azure-quickstart.sh

# El asistente te guiará para:
# - Ingresar credenciales de Azure
# - Crear archivo .env.azure
# - Probar la conexión
# - Ejecutar la inicialización completa

# 2. ¡Listo! La base de datos está configurada
```

### Opción 2: Configuración Manual

```bash
# 1. Copiar y editar la plantilla de configuración
cp .env.azure.template .env.azure
nano .env.azure  # Editar con tus credenciales

# 2. Cargar las variables de entorno
source .env.azure

# 3. Ejecutar el script de inicialización
./init-postgresql-azure.sh

# 4. Configurar y ejecutar la aplicación Spring Boot
export R2DBC_URL='r2dbc:postgresql://tu-servidor.postgres.database.azure.com:5432/motel_management_db?sslmode=require'
export R2DBC_USERNAME='postgres'
export R2DBC_PASSWORD='tu_password'
./mvnw spring-boot:run -Dspring-boot.run.profiles=azure
```

## 🔐 Seguridad

### Archivos que NO deben subirse a Git:
- ❌ `.env.azure` - Contiene credenciales reales
- ✅ `.env.azure.template` - Plantilla sin credenciales (SÍ se sube)

El archivo `.gitignore` ya está configurado para ignorar `.env.azure`.

### Mejores Prácticas:
1. Usa contraseñas seguras (mínimo 12 caracteres, combinación de letras, números y símbolos)
2. Configura reglas de firewall restrictivas en Azure
3. Usa Azure Key Vault para almacenar secretos en producción
4. Habilita auditoría y monitoreo en Azure
5. Rota las credenciales periódicamente
6. Usa SSL/TLS siempre (ya configurado en los scripts)

## 📊 Estructura de Datos

### Datos de Ejemplo Incluidos:

**Moteles (5):**
- Motel Paraíso (Medellín)
- Motel Las Estrellas (Medellín)
- Motel El Oasis (Bogotá)
- Motel Vista Hermosa (Cali)
- Motel Romance (Cartagena)

**Habitaciones (15):**
- 4 en Motel Paraíso
- 4 en Motel Las Estrellas
- 3 en Motel El Oasis
- 2 en Motel Vista Hermosa
- 2 en Motel Romance

**Servicios (15):**
- Jacuzzi, Spa, WiFi, TV Cable, Minibar
- Aire Acondicionado, Estacionamiento, Room Service
- Cama King, Vista al Mar, Balcón, Cocina
- Desayuno, Gimnasio, Piscina

## 🧪 Verificación

Para verificar que todo funciona correctamente:

```bash
# 1. Verificar conexión
PGPASSWORD='tu_password' psql \
  -h tu-servidor.postgres.database.azure.com \
  -U postgres \
  -d motel_management_db \
  --set=sslmode=require \
  -c "SELECT COUNT(*) FROM motel;"

# 2. Ver datos de ejemplo
PGPASSWORD='tu_password' psql \
  -h tu-servidor.postgres.database.azure.com \
  -U postgres \
  -d motel_management_db \
  --set=sslmode=require \
  -c "SELECT * FROM motel;"

# 3. Probar la aplicación Spring Boot
curl http://localhost:8084/actuator/health
```

## 📚 Recursos Adicionales

- [Azure Database for PostgreSQL Docs](https://docs.microsoft.com/azure/postgresql/)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- README-AZURE.md (documentación detallada)

## 🆘 Soporte

Si encuentras problemas:

1. Revisa el archivo `README-AZURE.md` para solución de problemas comunes
2. Verifica que las reglas de firewall en Azure permiten tu IP
3. Asegúrate de que las credenciales son correctas
4. Revisa los logs del script para mensajes de error detallados
5. Consulta la documentación oficial de Azure

## ✅ Checklist de Verificación

Antes de usar en producción, asegúrate de:

- [ ] Servidor Azure PostgreSQL creado y en ejecución
- [ ] Reglas de firewall configuradas correctamente
- [ ] Credenciales seguras configuradas
- [ ] Archivo `.env.azure` creado y protegido
- [ ] Script de inicialización ejecutado exitosamente
- [ ] Datos de ejemplo verificados
- [ ] Aplicación Spring Boot conectada correctamente
- [ ] SSL/TLS habilitado y funcionando
- [ ] Backups automáticos configurados en Azure
- [ ] Monitoreo y alertas configurados

---

**Nota:** Estos scripts están diseñados específicamente para Azure Database for PostgreSQL. Para otros proveedores cloud, pueden necesitarse ajustes menores.
