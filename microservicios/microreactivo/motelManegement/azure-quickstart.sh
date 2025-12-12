#!/bin/bash

################################################################################
# Script de inicio rápido para Azure PostgreSQL
# Facilita la configuración inicial de variables de entorno
################################################################################

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

clear

echo "=============================================="
echo -e "${BLUE}Configuración Rápida de Azure PostgreSQL${NC}"
echo "=============================================="
echo ""
echo "Este asistente te ayudará a configurar las variables"
echo "de entorno necesarias para conectarte a Azure PostgreSQL"
echo ""

# Verificar si ya existe .env.azure
if [ -f ".env.azure" ]; then
    echo -e "${YELLOW}⚠️  Ya existe un archivo .env.azure${NC}"
    read -p "¿Deseas sobrescribirlo? (s/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Saliendo sin cambios..."
        exit 0
    fi
fi

echo ""
echo "Por favor, proporciona la siguiente información:"
echo "----------------------------------------------"
echo ""

# Solicitar información
read -p "Servidor Azure PostgreSQL (ejemplo: miservidor.postgres.database.azure.com): " AZURE_PG_SERVER
read -p "Usuario administrador [postgres]: " AZURE_PG_ADMIN_USER
AZURE_PG_ADMIN_USER=${AZURE_PG_ADMIN_USER:-postgres}

# Solicitar password de forma segura
echo -n "Contraseña del administrador: "
read -s AZURE_PG_ADMIN_PASSWORD
echo ""

read -p "Nombre de la base de datos [motel_management_db]: " AZURE_PG_DATABASE
AZURE_PG_DATABASE=${AZURE_PG_DATABASE:-motel_management_db}

read -p "Puerto [5432]: " AZURE_PG_PORT
AZURE_PG_PORT=${AZURE_PG_PORT:-5432}

read -p "Modo SSL [require]: " AZURE_PG_SSL_MODE
AZURE_PG_SSL_MODE=${AZURE_PG_SSL_MODE:-require}

# Crear archivo .env.azure
cat > .env.azure << EOF
# ============================================================================
# Configuración de Azure Database for PostgreSQL
# Generado automáticamente por azure-quickstart.sh
# Fecha: $(date)
# ============================================================================

# Servidor de PostgreSQL en Azure
AZURE_PG_SERVER=$AZURE_PG_SERVER

# Usuario administrador
AZURE_PG_ADMIN_USER=$AZURE_PG_ADMIN_USER

# Contraseña del usuario administrador
AZURE_PG_ADMIN_PASSWORD=$AZURE_PG_ADMIN_PASSWORD

# Nombre de la base de datos
AZURE_PG_DATABASE=$AZURE_PG_DATABASE

# Puerto
AZURE_PG_PORT=$AZURE_PG_PORT

# Modo SSL
AZURE_PG_SSL_MODE=$AZURE_PG_SSL_MODE

# Ruta del script SQL de inicialización
SQL_INIT_SCRIPT=./src/main/resources/Postgres-init-motel.sql
EOF

echo ""
echo -e "${GREEN}✅ Archivo .env.azure creado exitosamente${NC}"
echo ""

# Cargar variables
source .env.azure

echo "=============================================="
echo "Resumen de configuración:"
echo "=============================================="
echo "  Servidor:      $AZURE_PG_SERVER"
echo "  Usuario:       $AZURE_PG_ADMIN_USER"
echo "  Base de datos: $AZURE_PG_DATABASE"
echo "  Puerto:        $AZURE_PG_PORT"
echo "  SSL Mode:      $AZURE_PG_SSL_MODE"
echo "=============================================="
echo ""

# Preguntar si desea probar la conexión
read -p "¿Deseas probar la conexión ahora? (S/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Nn]$ ]]; then
    echo ""
    echo -e "${BLUE}Probando conexión...${NC}"
    
    if PGPASSWORD="$AZURE_PG_ADMIN_PASSWORD" psql \
        -h "$AZURE_PG_SERVER" \
        -p "$AZURE_PG_PORT" \
        -U "$AZURE_PG_ADMIN_USER" \
        -d postgres \
        --set=sslmode="$AZURE_PG_SSL_MODE" \
        -c "SELECT version();" > /dev/null 2>&1; then
        
        echo -e "${GREEN}✅ Conexión exitosa!${NC}"
        echo ""
        
        # Preguntar si desea ejecutar la inicialización
        read -p "¿Deseas ejecutar la inicialización completa de la base de datos? (S/n): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Nn]$ ]]; then
            echo ""
            ./init-postgresql-azure.sh
        else
            echo ""
            echo "Para inicializar la base de datos más tarde, ejecuta:"
            echo "  ./init-postgresql-azure.sh"
        fi
    else
        echo -e "${RED}❌ No se pudo conectar al servidor${NC}"
        echo ""
        echo "Posibles causas:"
        echo "  1. Las credenciales son incorrectas"
        echo "  2. El servidor no permite conexiones desde tu IP"
        echo "  3. Las reglas de firewall no están configuradas"
        echo ""
        echo "Por favor, verifica la configuración en Azure Portal"
        echo "y asegúrate de que tu IP está permitida en las reglas de firewall."
    fi
else
    echo ""
    echo "Para inicializar la base de datos, ejecuta:"
    echo "  source .env.azure"
    echo "  ./init-postgresql-azure.sh"
fi

echo ""
echo -e "${BLUE}Comandos útiles:${NC}"
echo "  # Cargar variables de entorno:"
echo "  source .env.azure"
echo ""
echo "  # Inicializar base de datos:"
echo "  ./init-postgresql-azure.sh"
echo ""
echo "  # Conectarse manualmente:"
echo "  PGPASSWORD='***' psql -h $AZURE_PG_SERVER -U $AZURE_PG_ADMIN_USER -d postgres --set=sslmode=$AZURE_PG_SSL_MODE"
echo ""
