# 🃏 Sueca

<p align="center">
  <img src="src/sueca/resources/logo.png" alt="Sueca" width="128">
</p>

Jogo de cartas **Sueca** para desktop, feito em Java. Joga offline contra 3 adversários controlados pelo computador, com diferentes níveis de dificuldade e temas visuais.

Criado como alternativa à versão da Microsoft Store (descontinuada em 31/12/2024), pensado para funcionar tanto em **Windows** como em **Linux**.

---

## Funcionalidades

- Jogo completo de Sueca a 4 jogadores (tu + 3 adversários IA)
- 3 níveis de dificuldade: **Fácil**, **Média** e **Difícil**
- 2 temas visuais: **Clássico** (mesa verde tradicional) e **Moderno** (mesa escura)
- Cartas jogáveis destacadas na tua mão (as não jogáveis ficam semi-transparentes)
- Indicador de trunfo visível durante toda a ronda
- Placar com pontos da ronda e jogos ganhos por equipa
- Nomes dos adversários gerados aleatoriamente (nomes portugueses) em cada partida
- Menu de pausa com opções de tema, ecrã inteiro e saída
- Modo ecrã inteiro
- Funciona em Windows e Linux sem dependências externas além do Java

---

## Instalação e execução

### Windows

#### Opção 1 — Executável (mais simples)

1. Vai à pasta `dist/`
2. Faz duplo clique em **`Sueca.exe`**

> O ficheiro `.exe` é criado automaticamente durante a compilação se o [Launch4j](https://launch4j.sourceforge.net/) estiver instalado. Caso contrário, usa a Opção 2.

#### Opção 2 — Ficheiro JAR

1. Certifica-te de que tens o **Java 21** ou superior instalado.
   - Podes instalar a partir de: [https://adoptium.net](https://adoptium.net) (Adoptium Temurin)
   - Depois de instalar, confirma no terminal:
     ```
     java -version
     ```
2. Faz duplo clique em **`dist/Sueca.jar`**
   - Ou, no terminal:
     ```
     java -jar dist\Sueca.jar
     ```

#### Compilar a partir do código fonte (Windows)

1. Instala o **JDK 21** ou superior ([Adoptium Temurin](https://adoptium.net))
2. Abre o terminal na pasta do projeto
3. Executa:
   ```
   build.bat
   ```
4. O ficheiro gerado fica em `dist/Sueca.jar` (e `dist/Sueca.exe` se tiveres o Launch4j instalado)
5. No final, o script pergunta se queres jogar de imediato

---

### Linux

#### Passo 1 — Instalar o Java

Se ainda não tiveres o Java instalado, usa o comando da tua distribuição:

| Distribuição | Comando |
|---|---|
| Ubuntu / Debian | `sudo apt install default-jre` |
| Fedora | `sudo dnf install java-21-openjdk` |
| Arch Linux | `sudo pacman -S jre-openjdk` |
| openSUSE | `sudo zypper install java-21-openjdk` |

Confirma a instalação:
```bash
java -version
```

#### Passo 2 — Compilar o jogo

```bash
chmod +x build.sh
./build.sh
```

Isto cria o ficheiro `dist/Sueca.jar`.

#### Passo 3 — Jogar

```bash
java -jar dist/Sueca.jar
```

#### Instalação no sistema (opcional)

Se preferires ter o jogo acessível pelo menu de aplicações:

```bash
chmod +x install-linux.sh
./install-linux.sh
```

Este script:
- Instala o Java automaticamente se não estiver presente
- Compila o jogo se necessário
- Copia o jogo para `/usr/local/share/sueca/`
- Cria um atalho no menu de aplicações
- Cria o comando `sueca` no terminal

Depois podes simplesmente:
- Procurar **"Sueca"** no menu de aplicações, ou
- Escrever `sueca` no terminal

---

## Como se joga

### Disposição dos jogadores

- Tu estás sempre na posição de **baixo** (nome: "Tu")
- O teu **parceiro de equipa** está na posição de cima (em frente a ti)
- Os **adversários** estão à esquerda e à direita
- Equipas: **Nós** (tu + parceiro) vs **Eles** (esquerda + direita)

### Objetivo

Ganhar **4 jogos** antes da equipa adversária.

Cada ronda distribui 40 cartas (10 por jogador) e um total de **120 pontos** repartidos por 10 vazas. A equipa que fizer mais de 60 pontos ganha a ronda.

### Trunfo

A última carta distribuída define o **naipe de trunfo**. Este naipe é mostrado no canto superior direito do ecrã durante toda a ronda. Qualquer carta de trunfo ganha a qualquer carta de outro naipe.

### Jogar uma vaza

1. Clica numa carta da tua mão para a jogar.
2. Tens de **assistir ao naipe** (jogar uma carta do mesmo naipe da primeira carta jogada), se tiveres.
3. Se não tiveres cartas desse naipe, podes jogar qualquer carta, incluindo trunfo.
4. Ganha a vaza quem jogou a carta mais alta do naipe pedido, ou o trunfo mais alto se alguém trunfou.
5. Quem ganha a vaza abre a seguinte.

### Valor das cartas

| Carta | Pontos |
|---|---|
| Ás | 11 |
| 7 | 10 |
| Rei | 4 |
| Valete | 3 |
| Dama | 2 |
| 2, 3, 4, 5, 6 | 0 |

**Hierarquia dentro do naipe (do mais fraco para o mais forte):**

2 → 3 → 4 → 5 → 6 → Valete → Dama → Rei → 7 → Ás

### Resultado da ronda

| Resultado | Jogos ganhos |
|---|---|
| Mais de 60 pontos | +1 jogo |
| 91 ou mais pontos (**capote**) | +2 jogos |
| 120 pontos (**bandeira**) | +4 jogos |
| Empate a 60 | Ninguém marca |

A primeira equipa a atingir **4 jogos** ganha a partida.

---

## Controlos

| Ação | Como |
|---|---|
| Jogar uma carta | Clicar na carta |
| Pausar / Menu | Tecla `Escape` |
| Mudar tema | No menu de pausa |
| Ecrã inteiro / Modo janela | No menu de pausa ou no menu principal |

---

## Níveis de dificuldade

- **Fácil** — O computador joga cartas aleatoriamente.
- **Média** — O computador usa estratégia básica: tenta ganhar vazas, joga baixo quando o parceiro está a ganhar, e evita desperdiçar trunfos.
- **Difícil** — O computador usa estratégia avançada: lembra-se das cartas já jogadas, calcula se as suas cartas são as mais altas que restam no naipe, trunfa em vazas valiosas, e maximiza pontos quando é o último a jogar.

---

## Temas visuais

- **Clássico** — Mesa verde tradicional, costas das cartas azuis, estilo semelhante ao jogo original.
- **Moderno** — Mesa escura, costas das cartas roxas, interface com tons mais sóbrios.

O tema pode ser escolhido no menu principal ou trocado a qualquer momento no menu de pausa.

---

## Estrutura do projeto

```
sueca/
├── src/sueca/
│   ├── SuecaGame.java          — Ponto de entrada e janela principal
│   ├── model/
│   │   ├── Card.java           — Carta (naipe + valor)
│   │   ├── Deck.java           — Baralho de 40 cartas
│   │   ├── Difficulty.java     — Níveis de dificuldade
│   │   ├── GameEngine.java     — Lógica do jogo e pontuação
│   │   ├── Player.java         — Jogador (humano ou IA)
│   │   ├── Rank.java           — Valores das cartas
│   │   ├── Suit.java           — Naipes (Copas, Ouros, Paus, Espadas)
│   │   └── Trick.java          — Vaza (4 cartas jogadas)
│   ├── ai/
│   │   └── AIPlayer.java       — Inteligência artificial dos adversários
│   ├── ui/
│   │   ├── CardRenderer.java   — Desenho das cartas
│   │   ├── GamePanel.java      — Ecrã de jogo
│   │   └── MenuPanel.java      — Menu principal
│   └── ui/theme/
│       ├── GameTheme.java      — Interface de tema
│       ├── ClassicTheme.java   — Tema clássico
│       └── ModernTheme.java    — Tema moderno
├── build.bat                   — Script de compilação (Windows)
├── build.sh                    — Script de compilação (Linux)
├── install-linux.sh            — Instalador para Linux
└── README.md
```

---

## Requisitos

**Para jogar:**
- Java 21 ou superior (JRE)

**Para compilar:**
- Java 21 ou superior (JDK)
- Launch4j (opcional, só para criar o `.exe` no Windows)

---

## Licença

Projeto pessoal, sem licença definida.
