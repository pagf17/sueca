#!/bin/bash
# ─── Sueca - Instalador Linux ───
# Instala o jogo no sistema e cria atalho no menu

set -e

INSTALL_DIR="/usr/local/share/sueca"
DESKTOP_DIR="/usr/share/applications"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Instalador da Sueca"
echo "==================="

# ─── Verificar / Instalar Java ───
install_java() {
    echo "A tentar instalar o Java automaticamente..."
    if command -v apt &> /dev/null; then
        sudo apt update && sudo apt install -y default-jre
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y java-21-openjdk
    elif command -v pacman &> /dev/null; then
        sudo pacman -S --noconfirm jre-openjdk
    elif command -v zypper &> /dev/null; then
        sudo zypper install -y java-21-openjdk
    else
        echo ""
        echo "ERRO: Nao foi possivel instalar o Java automaticamente."
        echo "Instala manualmente com um destes comandos:"
        echo "  Ubuntu/Debian:  sudo apt install default-jre"
        echo "  Fedora:         sudo dnf install java-21-openjdk"
        echo "  Arch Linux:     sudo pacman -S jre-openjdk"
        echo "  openSUSE:       sudo zypper install java-21-openjdk"
        exit 1
    fi
}

if ! command -v java &> /dev/null; then
    echo "Java nao encontrado."
    install_java
fi

# Verificar versao minima (Java 21)
JAVA_VER=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "Java $JAVA_VER encontrado, mas e necessario Java 21 ou superior."
    install_java
fi

echo "Java encontrado: $(java -version 2>&1 | head -1)"

# Compilar se necessario
if [ ! -f "$SCRIPT_DIR/dist/Sueca.jar" ]; then
    echo "A compilar..."
    bash "$SCRIPT_DIR/build.sh"
fi

# Instalar
echo "A instalar em $INSTALL_DIR..."
sudo mkdir -p "$INSTALL_DIR"
sudo cp "$SCRIPT_DIR/dist/Sueca.jar" "$INSTALL_DIR/"

# Copiar icone se existir
if [ -f "$SCRIPT_DIR/src/sueca/resources/logo.png" ]; then
    sudo cp "$SCRIPT_DIR/src/sueca/resources/logo.png" "$INSTALL_DIR/Sueca.png"
fi

# Criar script de execucao
sudo tee "$INSTALL_DIR/sueca" > /dev/null <<'EOF'
#!/bin/bash
java -jar /usr/local/share/sueca/Sueca.jar
EOF
sudo chmod +x "$INSTALL_DIR/sueca"

# Criar link no PATH
sudo ln -sf "$INSTALL_DIR/sueca" /usr/local/bin/sueca

# Instalar atalho no menu
sudo tee "$DESKTOP_DIR/Sueca.desktop" > /dev/null <<EOF
[Desktop Entry]
Name=Sueca
Comment=Jogo de Sueca
Exec=/usr/local/share/sueca/sueca
Path=/usr/local/share/sueca
Type=Application
Categories=Game;CardGame;
Terminal=false
Icon=/usr/local/share/sueca/Sueca.png
EOF

echo ""
echo "Instalacao concluida!"
echo ""
echo "Para jogar:"
echo "  - Procura 'Sueca' no menu de aplicacoes"
echo "  - Ou corre no terminal: sueca"
echo ""
