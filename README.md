# Sueca

Jogo de Sueca para desktop, para jogar offline contra adversários controlados por computador.

---

## Instalação e execução

### Windows

1. Vai à pasta `dist/`
2. Faz duplo clique em **`Sueca.exe`**

Não é necessário instalar nada mais.

---

### Linux

**Passo 1 — Instala o Java** (se ainda não tiveres):

| Distribuição | Comando |
|---|---|
| Ubuntu / Debian | `sudo apt install default-jre` |
| Fedora | `sudo dnf install java-latest-openjdk` |
| Arch Linux | `sudo pacman -S jre-openjdk` |

**Passo 2 — Compila o jogo:**
```bash
chmod +x build.sh
./build.sh
```

**Passo 3 — Corre o jogo:**
```bash
./run.sh
```

Ou, se preferires instalar no sistema com atalho no menu de aplicações:
```bash
chmod +x install-linux.sh
./install-linux.sh
```
Depois podes procurar "Sueca" no menu de aplicações ou correr `sueca` no terminal.

---

## Como se joga

### O básico

- És sempre o jogador **"Tu"**, sentado na posição de baixo.
- O teu parceiro de equipa é o jogador do lado oposto (em cima).
- Os jogadores da esquerda e direita são adversários.
- Os nomes dos adversários são gerados aleatoriamente em cada partida.

### Objetivo

Ganhar **4 jogos** antes da equipa adversária.

Cada ronda distribui 120 pontos pelas 10 vazas. A equipa que fizer mais de 60 pontos ganha a ronda e marca **1 jogo**.

### Trunfo

A última carta distribuída ao dono da baralha define o naipe de trunfo. Esse naipe é mostrado no ecrã durante toda a ronda — qualquer carta desse naipe bate qualquer carta de outro naipe.

### Vazas

- Joga uma carta clicando nela.
- Tens de **assistir ao naipe** da carta que abriu a vaza, se tiveres cartas desse naipe.
- Se não tiveres, podes jogar qualquer carta (incluindo trunfo).
- Ganha a vaza quem jogou a carta mais alta do naipe pedido, ou o trunfo mais alto se alguém trunfou.
- Quem ganha a vaza abre a seguinte.

### Valor das cartas

| Carta | Pontos |
|---|---|
| Ás | 11 |
| 7 | 10 |
| Rei | 3 |
| Dama | 2 |
| Valete | 1 |
| 2, 3, 4, 5, 6 | 0 |

**Hierarquia dentro do naipe (do mais fraco para o mais forte):**
2 < 3 < 4 < 5 < 6 < Valete < Dama < Rei < 7 < Ás

### Pontuação especial

| Resultado | Jogos ganhos |
|---|---|
| Mais de 60 pontos | +1 jogo |
| 91 ou mais pontos (capote) | +2 jogos |
| 120 pontos (bandeira) | +4 jogos |

Primeiro a atingir 4 jogos ganha a partida.

---

## Opções do jogo

- **Dificuldade** — escolhida antes de começar: Fácil, Média ou Difícil. Afeta a estratégia da IA.
- **Tema visual** — Clássico ou Moderno, alternável com a tecla `T` durante o jogo.
- **Ecrã inteiro** — tecla `F` ou botão no menu de pausa.
- **Pausa / Menu** — tecla `Escape`.

---

## Compilar a partir do código fonte

### Windows
```
build.bat
```

### Linux
```bash
chmod +x build.sh
./build.sh
```

O ficheiro gerado fica em `dist/Sueca.jar` (e `dist/Sueca.exe` no Windows se tiveres o Launch4j instalado).

**Requisito para compilar:** Java JDK 11 ou superior.
