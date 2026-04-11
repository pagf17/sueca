package sueca.model;

public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }
    public int getPoints() { return rank.getPoints(); }
    public int getStrength() { return rank.getStrength(); }

    public boolean beats(Card other, Suit trump, Suit leadSuit) {
        // Trump always beats non-trump
        if (this.suit == trump && other.suit != trump) return true;
        if (this.suit != trump && other.suit == trump) return false;
        // If same suit, compare strength
        if (this.suit == other.suit) return this.getStrength() > other.getStrength();
        // If this follows lead suit and other doesn't (and neither is trump)
        if (this.suit == leadSuit && other.suit != leadSuit) return true;
        if (this.suit != leadSuit && other.suit == leadSuit) return false;
        // Neither follows suit nor is trump - first played wins
        return false;
    }

    @Override
    public String toString() {
        return rank.getSymbol() + suit.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card c = (Card) o;
        return suit == c.suit && rank == c.rank;
    }

    @Override
    public int hashCode() {
        return suit.ordinal() * 13 + rank.ordinal();
    }
}
