package sueca.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>(40);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deal 10 cards to each of 4 players.
     * The last card dealt determines trump suit.
     */
    public List<List<Card>> deal() {
        shuffle();
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new ArrayList<>(cards.subList(i * 10, (i + 1) * 10)));
        }
        return hands;
    }

    /**
     * Get the trump card (last card of the deck, which goes to the dealer).
     * In Sueca, the last card dealt determines trump.
     */
    public Card getTrumpCard() {
        return cards.get(cards.size() - 1);
    }

    public List<Card> getCards() {
        return cards;
    }
}
