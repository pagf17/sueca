package sueca.model;

public enum Rank {
    DOIS("2", 0, 0),
    TRES("3", 0, 1),
    QUATRO("4", 0, 2),
    CINCO("5", 0, 3),
    SEIS("6", 0, 4),
    VALETE("J", 3, 5),
    DAMA("Q", 2, 6),
    REI("K", 4, 7),
    SETE("7", 10, 8),
    AS("A", 11, 9);

    private final String symbol;
    private final int points;
    private final int strength; // higher = stronger

    Rank(String symbol, int points, int strength) {
        this.symbol = symbol;
        this.points = points;
        this.strength = strength;
    }

    public String getSymbol() { return symbol; }
    public int getPoints() { return points; }
    public int getStrength() { return strength; }
}
