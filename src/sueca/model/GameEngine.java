package sueca.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
    private Player[] players;
    private Suit trump;
    private Card trumpCard;
    private int dealer; // index of dealer
    private int currentPlayer;
    private Trick currentTrick;
    private int[] teamPoints; // points for team 0 and 1
    private int[] gamesWon;  // games won by each team
    private int trickCount;
    private boolean roundOver;
    private boolean matchOver;

    public static final int TOTAL_POINTS = 120;
    public static final int GAMES_TO_WIN = 4;

    private static final String[] AI_NAMES = {
        "Albino", "Alcides", "Alexandre", "Alfredo", "Álvaro",
        "Américo", "Aníbal", "António", "Armando", "Arnaldo",
        "Artur", "Augusto", "Baltazar", "Bartolomeu", "Belmiro",
        "Benedito", "Benjamim", "Bernardo", "Bráulio", "Caetano",
        "Celestino", "Cláudio", "Custódio", "Damião", "Daniel",
        "Dário", "Delfim", "Diamantino", "Domingos", "Duarte",
        "Edmundo", "Eduardo", "Egas", "Elmano", "Ernesto",
        "Eugénio", "Evaristo", "Ezequiel", "Faustino", "Feliciano",
        "Felisberto", "Fernão", "Filipe", "Firmino", "Florêncio",
        "Francisco", "Frederico", "Frutuoso", "Gaspar", "Geraldo",
        "Gil", "Gonçalo", "Gregório", "Guilherme", "Gustavo",
        "Helder", "Henrique", "Hermínio", "Hilário", "Horácio",
        "Hugo", "Inocêncio", "Isidoro", "Jacinto", "Jerónimo",
        "Joaquim", "Jorge", "José", "Júlio", "Justino",
        "Ladislau", "Laurindo", "Leandro", "Leonardo", "Leopoldino",
        "Lourenço", "Lucas", "Luís", "Manel", "Manuel",
        "Marcelo", "Marcelino", "Márcio", "Mariano", "Mário",
        "Martinho", "Mateus", "Maurício", "Miguel", "Modesto",
        "Norberto", "Nuno", "Olavo", "Orlando", "Osvaldo",
        "Paulo", "Pedro", "Plácido", "Raimundo", "Renato",
        "Ricardo", "Roberto", "Rodrigo", "Rogério", "Romeu",
        "Rúben", "Rui", "Sérgio", "Silvério", "Simão",
        "Teodoro", "Timóteo", "Tomás", "Valentim", "Vasco"
    };

    private static String[] pickAiNames() {
        List<String> list = new ArrayList<>(List.of(AI_NAMES));
        Collections.shuffle(list);
        return new String[]{list.get(0), list.get(1), list.get(2)};
    }

    public GameEngine() {
        players = new Player[4];
        String[] names = pickAiNames();
        players[0] = new Player("Tu", 0, true);
        players[1] = new Player(names[0], 1, false);
        players[2] = new Player(names[1], 2, false);
        players[3] = new Player(names[2], 3, false);
        gamesWon = new int[]{0, 0};
        dealer = 0;
    }

    public void newRound() {
        Deck deck = new Deck();
        List<List<Card>> hands = deck.deal();

        for (int i = 0; i < 4; i++) {
            players[i].setHand(hands.get(i));
        }

        // Trump is determined by the last card dealt to the dealer
        trumpCard = hands.get(dealer).get(hands.get(dealer).size() - 1);
        trump = trumpCard.getSuit();

        teamPoints = new int[]{0, 0};
        trickCount = 0;
        roundOver = false;

        // Player to the right of dealer starts
        currentPlayer = (dealer + 1) % 4;
        currentTrick = new Trick();
    }

    public void playCard(Player player, Card card) {
        if (roundOver) return;
        player.removeCard(card);
        currentTrick.playCard(player, card);

        if (currentTrick.isComplete()) {
            Player winner = currentTrick.getWinner(trump);
            int team = winner.getTeam();
            teamPoints[team] += currentTrick.getPoints();
            trickCount++;
            currentPlayer = winner.getIndex();

            if (trickCount >= 10) {
                endRound();
            }
        } else {
            currentPlayer = (currentPlayer + 1) % 4;
        }
    }

    public void startNewTrick() {
        currentTrick = new Trick();
    }

    private void endRound() {
        roundOver = true;
        // Determine round winner
        int winningTeam;
        if (teamPoints[0] > 60) {
            winningTeam = 0;
        } else if (teamPoints[1] > 60) {
            winningTeam = 1;
        } else {
            // Draw at 60-60: no one wins the game
            winningTeam = -1;
        }

        if (winningTeam >= 0) {
            // Check for "bandeira" (all 120 points) = 4 games
            if (teamPoints[winningTeam] == 120) {
                gamesWon[winningTeam] += 4;
            }
            // Check for "capote" (91+ points) = 2 games
            else if (teamPoints[winningTeam] >= 91) {
                gamesWon[winningTeam] += 2;
            } else {
                gamesWon[winningTeam] += 1;
            }
        }

        if (gamesWon[0] >= GAMES_TO_WIN || gamesWon[1] >= GAMES_TO_WIN) {
            matchOver = true;
        }

        // Rotate dealer
        dealer = (dealer + 1) % 4;
    }

    public void newMatch() {
        gamesWon = new int[]{0, 0};
        matchOver = false;
        dealer = 0;
    }

    // Getters
    public Player[] getPlayers() { return players; }
    public Player getPlayer(int idx) { return players[idx]; }
    public Player getCurrentPlayer() { return players[currentPlayer]; }
    public int getCurrentPlayerIndex() { return currentPlayer; }
    public Suit getTrump() { return trump; }
    public Card getTrumpCard() { return trumpCard; }
    public Trick getCurrentTrick() { return currentTrick; }
    public int[] getTeamPoints() { return teamPoints; }
    public int[] getGamesWon() { return gamesWon; }
    public int getTrickCount() { return trickCount; }
    public boolean isRoundOver() { return roundOver; }
    public boolean isMatchOver() { return matchOver; }
    public int getDealer() { return dealer; }

    public Suit getLeadSuit() {
        return currentTrick != null && currentTrick.size() > 0 ? currentTrick.getLeadSuit() : null;
    }

    public String getRoundResult() {
        if (!roundOver) return "";
        if (teamPoints[0] == 120) return "Bandeira! Nós ganhámos todos os pontos! (+4 jogos)";
        if (teamPoints[1] == 120) return "Bandeira! Eles ganharam todos os pontos! (+4 jogos)";
        if (teamPoints[0] >= 91) return "Capote! Nós ganhámos com " + teamPoints[0] + " pontos! (+2 jogos)";
        if (teamPoints[1] >= 91) return "Capote! Eles ganharam com " + teamPoints[1] + " pontos! (+2 jogos)";
        if (teamPoints[0] > 60) return "Nós ganhámos com " + teamPoints[0] + " pontos!";
        if (teamPoints[1] > 60) return "Eles ganharam com " + teamPoints[1] + " pontos!";
        return "Empate a 60 pontos! Ninguém ganha este jogo.";
    }

    public String getMatchResult() {
        if (!matchOver) return "";
        if (gamesWon[0] >= GAMES_TO_WIN) return "🎉 Vitória! Nós ganhámos a partida!";
        return "Derrota... Eles ganharam a partida.";
    }
}
