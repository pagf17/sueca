# 🃏 Sueca

<p align="center">
  <img src="src/sueca/resources/logo.png" alt="Sueca" width="128">
</p>

Um jogo de Sueca para PC, feito em Java. Jogas sozinho contra 3 adversários controlados pelo computador, com três níveis de dificuldade e dois temas visuais diferentes.

## O que tem

- Sueca a 4 jogadores (tu e mais 3 adversários controlados pelo computador)
- Três dificuldades: Fácil, Média e Difícil
- Dois temas: Clássico (mesa verde) e Moderno (mesa escura)
- Placar com pontos e jogos ganhos
- Nomes dos adversários sorteados a cada partida
- Ecrã inteiro e menu de pausa

## Como instalar

### Windows

A forma mais simples é ir à pasta `dist/` e abrir o `Sueca.exe`.

Se preferires (ou se o `.exe` não existir), precisas do Java 21+ instalado ([adoptium.net](https://adoptium.net)) e depois corres:

```
java -jar dist\Sueca.jar
```

Para compilar tu mesmo a partir do código, corre `build.bat` na pasta do projeto. O jar (e o exe, se tiveres o [Launch4j](https://launch4j.sourceforge.net/)) fica em `dist/`.

### Linux

Primeiro garante que tens o Java instalado:

```bash
sudo apt install default-jre       # Ubuntu/Debian
sudo dnf install java-21-openjdk   # Fedora
sudo pacman -S jre-openjdk         # Arch
sudo zypper install java-21-openjdk # openSUSE
```

Depois compila e corre:

```bash
chmod +x build.sh
./build.sh
java -jar dist/Sueca.jar
```

Se quiseres o jogo integrado no menu de aplicações (com um comando `sueca` no terminal), há um instalador:

```bash
chmod +x install-linux.sh
./install-linux.sh
```

## Como se joga

Tu ficas em baixo, o teu parceiro em cima, e os adversários nos lados. É "Nós" (tu + parceiro) contra "Eles". Ganha quem chegar primeiro a 4 jogos.

Cada ronda são 40 cartas distribuídas (10 por jogador) e 120 pontos ao todo, divididos por 10 vazas. Quem passar de 60 pontos ganha a ronda — e se fizeres 91+ (capote) ganhas 2 jogos de uma vez, ou 4 jogos se fizeres a limpeza total (bandeira, 120 pontos).

A última carta distribuída define o trunfo, mostrado sempre no canto do ecrã. Regras de jogo normais: tens de assistir ao naipe pedido se tiveres carta desse naipe; senão, jogas o que quiseres, incluindo trunfo. Ganha a vaza a carta mais alta do naipe pedido, ou o trunfo mais alto se alguém cortou.

**Valor das cartas:** Ás (11), 7 (10), Rei (4), Valete (3), Dama (2), as restantes (0).

**Força das cartas (do mais fraco ao mais forte):** 2, 3, 4, 5, 6, Valete, Dama, Rei, 7, Ás.

## Controlos

Clicas numa carta para a jogar. `Escape` abre o menu de pausa, onde também dá para mudar de tema e ativar o ecrã inteiro.

## Dificuldades

- **Fácil** — o computador joga à sorte.
- **Média** — tenta ganhar vazas, evita gastar trunfos à toa e joga baixo quando o parceiro já está a ganhar.
- **Difícil** — joga de forma mais calculada: sabe que cartas já saíram, avalia se a carta que tem na mão já é a mais forte do naipe, corta em vazas que valham a pena e tenta maximizar pontos quando joga por último.

## Requisitos

Só precisas do Java 21 ou superior para jogar. Para compilar também precisas do JDK 21+ (e opcionalmente do Launch4j, se quiseres gerar o `.exe` no Windows).

## Licença

MIT — o código está livre para usares como quiseres. Ver o ficheiro [LICENSE](LICENSE).
