package sueca.ui.theme;

import java.awt.*;

public interface GameTheme {
    String getName();

    // Background
    Color getFeltColor();
    Color getFeltDarker();
    Color getTableBorderColor();

    // Cards
    Color getCardBackground();
    Color getCardBorder();
    Color getRedColor();
    Color getBlackColor();
    Color getCardBackPrimary();
    Color getCardBackSecondary();

    // UI
    Color getButtonColor();
    Color getButtonHoverColor();
    Color getButtonTextColor();
    Color getPanelBackground();
    Color getPanelBorder();
    Color getTextColor();
    Color getAccentColor();
    Color getLabelTeamUs();
    Color getLabelTeamThem();

    // Fonts
    Font getTitleFont();
    Font getButtonFont();
    Font getCardFont();
    Font getLabelFont();
    Font getScoreFont();
}
