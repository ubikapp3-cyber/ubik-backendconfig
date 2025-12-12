#!/bin/bash

################################################################################
# Script de inicialización de PostgreSQL para Azure Database for PostgreSQL
# Autor: Ubik Backend Config Team
# Descripción: Inicializa y configura la base de datos PostgreSQL en Azure
# Uso: ./init-postgresql-azure.sh
################################################################################

set -e  # Salir inmediatamente si un comando falla

# Colores para mensajes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración por defecto - puede ser sobrescrita por variables de entorno
AZURE_PG_SERVER="${AZURE_PG_SERVER:-}"
AZURE_PG_ADMIN_USER="${AZURE_PG_ADMIN_USER:-postgres}"
AZURE_PG_ADMIN_PASSWORD="${AZURE_PG_ADMIN_PASSWORD:-}"
AZURE_PG_DATABASE="${AZURE_PG_DATABASE:-motel_management_db}"
AZURE_PG_PORT="${AZURE_PG_PORT:-5432}"
AZURE_PG_SSL_MODE="${AZURE_PG_SSL_MODE:-require}"

# Ruta del script SQL de inicialización
SQL_INIT_SCRIPT="${SQL_INIT_SCRIPT:-./src/main/resources/Postgres-init-motel.sql}"

################################################################################
# Funciones auxiliares
################################################################################

print_header() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}$1${NC}"
    echo "=============================================="
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

################################################################################
# Validaciones
################################################################################

validate_prerequisites() {
    print_header "Validando prerequisitos"
    
    # Verificar que psql está instalado
    if ! command -v psql &> /dev/null; then
        print_error "psql no está instalado"
        echo ""
        echo "Instala el cliente PostgreSQL:"
        echo "  Ubuntu/Debian: sudo apt-get install postgresql-client"
        echo "  CentOS/RHEL: sudo yum install postgresql"
        echo "  macOS: brew install postgresql"
        echo "  Windows: Descarga desde https://www.postgresql.org/download/windows/"
        exit 1
    fi
    print_success "psql está instalado"
    
    # Verificar variables de entorno requeridas
    if [ -z "$AZURE_PG_SERVER" ]; then
        print_error "Variable AZURE_PG_SERVER no está configurada"
        echo ""
        echo "Configura las variables de entorno requeridas:"
        echo "  export AZURE_PG_SERVER='tu-servidor.postgres.database.azure.com'"
        echo "  export AZURE_PG_ADMIN_USER='tu_usuario_admin'"
        echo "  export AZURE_PG_ADMIN_PASSWORD='tu_password'"
        echo ""
        echo "O copia y edita el archivo .env.azure.template:"
        echo "  cp .env.azure.template .env.azure"
        echo "  # Edita .env.azure con tus credenciales"
        echo "  source .env.azure"
        exit 1
    fi
    print_success "Variable AZURE_PG_SERVER configurada: $AZURE_PG_SERVER"
    
    if [ -z "$AZURE_PG_ADMIN_PASSWORD" ]; then
        print_error "Variable AZURE_PG_ADMIN_PASSWORD no está configurada"
        exit 1
    fi
    print_success "Variables de autenticación configuradas"
    
    # Verificar que existe el script SQL
    if [ ! -f "$SQL_INIT_SCRIPT" ]; then
        print_error "No se encuentra el script SQL: $SQL_INIT_SCRIPT"
        exit 1
    fi
    print_success "Script SQL encontrado: $SQL_INIT_SCRIPT"
    
    echo ""
}

################################################################################
# Conexión y pruebas
################################################################################

test_connection() {
    print_header "Probando conexión a Azure PostgreSQL"
    
    # Construir el string de conexión
    PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d postgres \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -c "SELECT version();" \
        > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        print_success "Conexión exitosa a Azure PostgreSQL"
        
        # Mostrar versión
        VERSION=$(PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
            -h "$AZURE_PG_SERVER" \
            -p "$AZURE_PG_PORT" \
            -U "$AZURE_PG_ADMIN_USER" \
            -d postgres \
            --set=sslmode="$AZURE_PG_SSL_MODE" \
            -t -c "SELECT version();" 2>/dev/null | xargs)
        print_info "Versión: $VERSION"
    else
        print_error "No se pudo conectar a Azure PostgreSQL"
        echo ""
        echo "Verifica:"
        echo "  1. Las credenciales son correctas"
        echo "  2. El servidor permite conexiones desde tu IP"
        echo "  3. Las reglas de firewall en Azure están configuradas"
        echo "  4. El servidor está en ejecución"
        exit 1
    fi
    
    echo ""
}

################################################################################
# Creación de base de datos
################################################################################

create_database() {
    print_header "Creando base de datos"
    
    # Verificar si la base de datos ya existe
    DB_EXISTS=$(PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d postgres \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -t -c "SELECT 1 FROM pg_database WHERE datname='$AZURE_PG_DATABASE';" 2>/dev/null | xargs)
    
    if [ "$DB_EXISTS" = "1" ]; then
        print_warning "La base de datos '$AZURE_PG_DATABASE' ya existe"
        
        read -p "¿Deseas recrearla? Esto eliminará todos los datos existentes (s/N): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Ss]$ ]]; then
            print_info "Eliminando base de datos existente..."
            PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
                -h "$AZURE_PG_SERVER" \
                -p "$AZURE_PG_PORT" \
                -U "$AZURE_PG_ADMIN_USER" \
                -d postgres \
                --set=sslmode="$AZURE_PG_SSL_MODE" \
                -c "DROP DATABASE IF EXISTS $AZURE_PG_DATABASE;" > /dev/null 2>&1
            print_success "Base de datos eliminada"
        else
            print_info "Usando base de datos existente"
            return 0
        fi
    fi
    
    # Crear la base de datos
    print_info "Creando base de datos '$AZURE_PG_DATABASE'..."
    PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d postgres \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -c "CREATE DATABASE $AZURE_PG_DATABASE WITH ENCODING='UTF8' LC_COLLATE='en_US.UTF-8' LC_CTYPE='en_US.UTF-8';" > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        print_success "Base de datos '$AZURE_PG_DATABASE' creada exitosamente"
    else
        print_error "Error al crear la base de datos"
        exit 1
    fi
    
    echo ""
}

################################################################################
# Inicialización del esquema
################################################################################

initialize_schema() {
    print_header "Inicializando esquema y datos"
    
    print_info "Ejecutando script SQL: $SQL_INIT_SCRIPT"
    
    # Ejecutar el script SQL
    PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d "$AZURE_PG_DATABASE" \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -f "$SQL_INIT_SCRIPT"
    
    if [ $? -eq 0 ]; then
        print_success "Esquema y datos iniciales creados exitosamente"
    else
        print_error "Error al ejecutar el script SQL"
        exit 1
    fi
    
    echo ""
}

################################################################################
# Verificación post-instalación
################################################################################

verify_installation() {
    print_header "Verificando instalación"
    
    # Verificar tablas creadas
    TABLES=$(PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d "$AZURE_PG_DATABASE" \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" 2>/dev/null | xargs)
    
    if [ "$TABLES" -gt 0 ]; then
        print_success "Esquema creado: $TABLES tablas encontradas"
        
        # Listar tablas
        print_info "Tablas creadas:"
        PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
            -h "$AZURE_PG_SERVER" \
            -p "$AZURE_PG_PORT" \
            -U "$AZURE_PG_ADMIN_USER" \
            -d "$AZURE_PG_DATABASE" \
            --set=sslmode="$AZURE_PG_SSL_MODE" \
            -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"
    else
        print_error "No se encontraron tablas en el esquema"
        exit 1
    fi
    
    echo ""
    
    # Verificar datos de ejemplo
    MOTEL_COUNT=$(PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d "$AZURE_PG_DATABASE" \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -t -c "SELECT COUNT(*) FROM motel;" 2>/dev/null | xargs)
    
    if [ "$MOTEL_COUNT" -gt 0 ]; then
        print_success "Datos de ejemplo cargados: $MOTEL_COUNT moteles"
    else
        print_warning "No se encontraron datos de ejemplo"
    fi
    
    echo ""
}

################################################################################
# Información de configuración
################################################################################

show_connection_info() {
    print_header "Información de conexión"
    
    echo "📊 Configuración de la base de datos:"
    echo "   Servidor:      $AZURE_PG_SERVER"
    echo "   Puerto:        $AZURE_PG_PORT"
    echo "   Base de datos: $AZURE_PG_DATABASE"
    echo "   Usuario:       $AZURE_PG_ADMIN_USER"
    echo "   SSL Mode:      $AZURE_PG_SSL_MODE"
    echo ""
    
    echo "🔌 String de conexión R2DBC para Spring Boot:"
    echo "   r2dbc:postgresql://$AZURE_PG_SERVER:$AZURE_PG_PORT/$AZURE_PG_DATABASE?sslmode=$AZURE_PG_SSL_MODE"
    echo ""
    
    echo "🔌 String de conexión JDBC (si es necesario):"
    echo "   jdbc:postgresql://$AZURE_PG_SERVER:$AZURE_PG_PORT/$AZURE_PG_DATABASE?sslmode=$AZURE_PG_SSL_MODE"
    echo ""
    
    echo "⚙️  Variables de entorno para Spring Boot:"
    echo "   export R2DBC_URL='r2dbc:postgresql://$AZURE_PG_SERVER:$AZURE_PG_PORT/$AZURE_PG_DATABASE?sslmode=$AZURE_PG_SSL_MODE'"
    echo "   export R2DBC_USERNAME='$AZURE_PG_ADMIN_USER'"
    echo "   export R2DBC_PASSWORD='<tu_password>'"
    echo ""
    
    echo "🔍 Comandos útiles:"
    echo "   # Conectarse a la base de datos:"
    echo "   PGPASSWORD='<password>' psql -h $AZURE_PG_SERVER -U $AZURE_PG_ADMIN_USER -d $AZURE_PG_DATABASE --set=sslmode=$AZURE_PG_SSL_MODE"
    echo ""
    echo "   # Ver todas las tablas:"
    echo "   \\dt"
    echo ""
    echo "   # Ver moteles:"
    echo "   SELECT * FROM motel;"
    echo ""
    
    print_info "Recuerda configurar las reglas de firewall en Azure para permitir conexiones desde tu aplicación"
    echo ""
}

################################################################################
# Programa principal
################################################################################

main() {
    print_header "Inicialización de PostgreSQL en Azure"
    
    echo "Este script configurará la base de datos PostgreSQL en Azure Database for PostgreSQL"
    echo ""
    
    # Cargar variables de entorno desde archivo si existe
    if [ -f ".env.azure" ]; then
        print_info "Cargando configuración desde .env.azure"
        source .env.azure
    fi
    
    # Ejecutar pasos de inicialización
    validate_prerequisites
    test_connection
    create_database
    initialize_schema
    verify_installation
    show_connection_info
    
    print_header "¡Inicialización completada exitosamente!"
    print_success "La base de datos está lista para usarse"
    echo ""
}

# Ejecutar el programa principal
main
