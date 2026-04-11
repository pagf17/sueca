#!/bin/bash
# ─── Sueca - Executar ───

if [ ! -f "dist/Sueca.jar" ]; then
    echo "JAR não encontrado. A compilar primeiro..."
    bash build.sh
fi

java -jar dist/Sueca.jar
