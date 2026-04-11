package sueca.model;

public enum Suit {
    COPAS("♥", "Copas", true),
    OUROS("♦", "Ouros", true),
    PAUS("♣", "Paus", false),
    ESPADAS("♠", "Espadas", false);

    private final String symbol;
    private final String name;
    private final boolean red;

    Suit(String symbol, String name, boolean red) {
        this.symbol = symbol;
        this.name = name;
        this.red = red;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public boolean isRed() { return red; }
}
