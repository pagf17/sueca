package sueca.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private final String name;
    private final int index; // 0=bottom(human), 1=right, 2=top, 3=left
    private final boolean human;
    private List<Card> hand;

    public Player(String name, int index, boolean human) {
        this.name = name;
        this.index = index;
        this.human = human;
        this.hand = new ArrayList<>();
    }

    public String getName() { return name; }
    public int getIndex() { return index; }
    public boolean isHuman() { return human; }
    public List<Card> getHand() { return hand; }

    public void setHand(List<Card> hand) {
        this.hand = new ArrayList<>(hand);
        sortHand();
    }

    public void sortHand() {
        hand.sort(Comparator.comparingInt((Card c) -> c.getSuit().ordinal())
                .thenComparingInt(Card::getStrength));
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public boolean hasCardsOfSuit(Suit suit) {
        return hand.stream().anyMatch(c -> c.getSuit() == suit);
    }

    public List<Card> getCardsOfSuit(Suit suit) {
        return hand.stream().filter(c -> c.getSuit() == suit).collect(Collectors.toList());
    }

    /**
     * Get the list of playable cards given the lead suit.
     * Must follow suit if possible.
     */
    public List<Card> getPlayableCards(Suit leadSuit) {
        if (leadSuit == null) return new ArrayList<>(hand);
        List<Card> suited = getCardsOfSuit(leadSuit);
        return suited.isEmpty() ? new ArrayList<>(hand) : suited;
    }

    public int getTeam() {
        return index % 2; // 0 and 2 = team 0 (Nós), 1 and 3 = team 1 (Eles)
    }
}
