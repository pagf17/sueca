#!/bin/bash
# ─── Sueca - Build Script ───
# Compila o projeto e cria o JAR executável

echo "🃏 A compilar Sueca..."

# Limpar build anterior
rm -rf build dist
mkdir -p build dist

# Compilar
find src -name "*.java" > sources.txt
javac -d build @sources.txt

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação!"
    exit 1
fi

# Copiar recursos
if [ -d src/sueca/resources ]; then
    cp -r src/sueca/resources build/sueca/resources
fi

# Criar manifest
echo "Main-Class: sueca.SuecaGame" > build/MANIFEST.MF

# Criar JAR
cd build
jar cfm ../dist/Sueca.jar MANIFEST.MF sueca/
cd ..

echo "✅ Build completo! Ficheiro: dist/Sueca.jar"
echo ""
echo "Para jogar executa:"
echo "  java -jar dist/Sueca.jar"
echo ""
echo "Ou simplesmente faz duplo clique no ficheiro Sueca.jar"

rm -f sources.txt
