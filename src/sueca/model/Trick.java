package sueca.model;

import java.util.ArrayList;
import java.util.List;

public class Trick {
    private final List<Card> cards;
    private final List<Player> players;
    private Suit leadSuit;

    public Trick() {
        cards = new ArrayList<>(4);
        players = new ArrayList<>(4);
    }

    public void playCard(Player player, Card card) {
        if (cards.isEmpty()) {
            leadSuit = card.getSuit();
        }
        cards.add(card);
        players.add(player);
    }

    public boolean isComplete() {
        return cards.size() == 4;
    }

    public Suit getLeadSuit() {
        return leadSuit;
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int size() {
        return cards.size();
    }

    /**
     * Determine the winner of the trick.
     */
    public Player getWinner(Suit trump) {
        Card best = cards.get(0);
        int bestIdx = 0;
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i).beats(best, trump, leadSuit)) {
                best = cards.get(i);
                bestIdx = i;
            }
        }
        return players.get(bestIdx);
    }

    /**
     * Total points in this trick.
     */
    public int getPoints() {
        return cards.stream().mapToInt(Card::getPoints).sum();
    }

    public Card getCard(int index) {
        return index < cards.size() ? cards.get(index) : null;
    }

    public Player getPlayer(int index) {
        return index < players.size() ? players.get(index) : null;
    }
}
