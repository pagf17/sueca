package sueca.ai;

import sueca.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class AIPlayer {

    /**
     * Choose a card based on difficulty level.
     */
    public static Card chooseCard(Player player, Trick trick, Suit trump,
                                   int[] teamPoints, Difficulty difficulty, List<Card> playedCards) {
        List<Card> playable = player.getPlayableCards(trick.size() > 0 ? trick.getLeadSuit() : null);
        if (playable.isEmpty()) return null;
        if (playable.size() == 1) return playable.get(0);

        switch (difficulty) {
            case FACIL:
                return chooseEasy(playable);
            case MEDIA:
                return chooseMedium(player, playable, trick, trump);
            case DIFICIL:
                return chooseHard(player, playable, trick, trump, teamPoints, playedCards);
            default:
                return chooseEasy(playable);
        }
    }

    // ─── EASY: Random play ───
    private static Card chooseEasy(List<Card> playable) {
        Collections.shuffle(playable);
        return playable.get(0);
    }

    // ─── MEDIUM: Basic strategy ───
    private static Card chooseMedium(Player player, List<Card> playable,
                                      Trick trick, Suit trump) {
        Suit lead = trick.getLeadSuit();

        // If leading: play a non-trump card, preferring low value
        if (trick.size() == 0) {
            List<Card> nonTrump = playable.stream()
                    .filter(c -> c.getSuit() != trump)
                    .collect(Collectors.toList());
            List<Card> pool = nonTrump.isEmpty() ? playable : nonTrump;
            pool.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            return pool.get(0);
        }

        // Find current best card in trick
        Card best = getBestCardInTrick(trick, trump);
        Player bestPlayer = getWinningPlayer(trick, trump);
        boolean partnerWinning = bestPlayer != null && bestPlayer.getTeam() == player.getTeam();

        // If partner is winning with high points, play low
        if (partnerWinning) {
            playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            // If we can add points safely
            if (trick.size() == 3) {
                // We're last, partner winning - dump points
                playable.sort(Comparator.comparingInt(Card::getPoints).reversed());
                return playable.get(0);
            }
            return playable.get(0); // play lowest
        }

        // Try to beat current best
        List<Card> winners = playable.stream()
                .filter(c -> c.beats(best, trump, lead))
                .collect(Collectors.toList());

        if (!winners.isEmpty()) {
            // Play the cheapest winner
            winners.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            return winners.get(0);
        }

        // Can't win - play lowest value card
        playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
        return playable.get(0);
    }

    // ─── HARD: Advanced strategy ───
    private static Card chooseHard(Player player, List<Card> playable,
                                    Trick trick, Suit trump,
                                    int[] teamPoints, List<Card> playedCards) {
        Suit lead = trick.getLeadSuit();

        // If leading
        if (trick.size() == 0) {
            return chooseLeadHard(player, playable, trump, playedCards);
        }

        Card best = getBestCardInTrick(trick, trump);
        Player bestPlayer = getWinningPlayer(trick, trump);
        boolean partnerWinning = bestPlayer != null && bestPlayer.getTeam() == player.getTeam();
        int trickPoints = trick.getCards().stream().mapToInt(Card::getPoints).sum();

        // Last to play
        if (trick.size() == 3) {
            if (partnerWinning) {
                // Partner winning - dump highest value card
                playable.sort(Comparator.comparingInt(Card::getPoints).reversed());
                return playable.get(0);
            } else {
                // Need to win - use cheapest winner
                List<Card> winners = playable.stream()
                        .filter(c -> c.beats(best, trump, lead))
                        .collect(Collectors.toList());
                if (!winners.isEmpty()) {
                    // If trick has many points, it's worth winning
                    if (trickPoints >= 10) {
                        winners.sort(Comparator.comparingInt(Card::getStrength));
                        return winners.get(0);
                    }
                    winners.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
                    return winners.get(0);
                }
                // Can't win - dump lowest
                playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
                return playable.get(0);
            }
        }

        // Second or third to play
        if (partnerWinning) {
            // Play low, but if we're 3rd and partner winning big, save our high cards
            playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            return playable.get(0);
        }

        // Try to win
        List<Card> winners = playable.stream()
                .filter(c -> c.beats(best, trump, lead))
                .collect(Collectors.toList());

        if (!winners.isEmpty()) {
            // If high-point trick, use strongest winner
            if (trickPoints >= 20) {
                winners.sort(Comparator.comparingInt(Card::getStrength).reversed());
                return winners.get(0);
            }
            // Otherwise use cheapest winner
            winners.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            return winners.get(0);
        }

        // Can't follow suit - consider trumping
        if (lead != null && !player.hasCardsOfSuit(lead) && !partnerWinning) {
            List<Card> trumpCards = playable.stream()
                    .filter(c -> c.getSuit() == trump)
                    .collect(Collectors.toList());
            if (!trumpCards.isEmpty() && trickPoints >= 10) {
                // Trump with lowest trump
                trumpCards.sort(Comparator.comparingInt(Card::getStrength));
                return trumpCards.get(0);
            }
        }

        // Dump lowest
        playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
        return playable.get(0);
    }

    private static Card chooseLeadHard(Player player, List<Card> playable,
                                        Suit trump, List<Card> playedCards) {
        // Prefer to lead with Aces/7s of non-trump suits if they're the highest remaining
        List<Card> nonTrump = playable.stream()
                .filter(c -> c.getSuit() != trump)
                .collect(Collectors.toList());

        if (!nonTrump.isEmpty()) {
            // Find cards that are the highest remaining in their suit
            for (Card c : nonTrump) {
                if (isHighestRemaining(c, playedCards, player.getHand())) {
                    return c; // Lead with guaranteed winner
                }
            }

            // Lead with high cards in suits where partner might be strong
            // Or just play low non-trump
            nonTrump.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
            return nonTrump.get(0);
        }

        // Only trump left - play lowest
        playable.sort(Comparator.comparingInt(Card::getPoints).thenComparingInt(Card::getStrength));
        return playable.get(0);
    }

    private static boolean isHighestRemaining(Card card, List<Card> playedCards, List<Card> hand) {
        Suit suit = card.getSuit();
        int strength = card.getStrength();

        // Check if any higher card of this suit is still in play (not in hand, not played)
        for (Rank r : Rank.values()) {
            if (r.getStrength() > strength) {
                Card higher = new Card(suit, r);
                if (!playedCards.contains(higher) && !hand.contains(higher)) {
                    return false; // There's a higher card out there
                }
            }
        }
        return true;
    }

    private static Card getBestCardInTrick(Trick trick, Suit trump) {
        if (trick.size() == 0) return null;
        Card best = trick.getCard(0);
        Suit lead = trick.getLeadSuit();
        for (int i = 1; i < trick.size(); i++) {
            if (trick.getCard(i).beats(best, trump, lead)) {
                best = trick.getCard(i);
            }
        }
        return best;
    }

    private static Player getWinningPlayer(Trick trick, Suit trump) {
        if (trick.size() == 0) return null;
        Card best = trick.getCard(0);
        Player winner = trick.getPlayer(0);
        Suit lead = trick.getLeadSuit();
        for (int i = 1; i < trick.size(); i++) {
            if (trick.getCard(i).beats(best, trump, lead)) {
                best = trick.getCard(i);
                winner = trick.getPlayer(i);
            }
        }
        return winner;
    }
}
