package com.wolfdrache.salormurder.models;

import com.wolfdrache.salormurder.models.PlayerSM.Role;

public class PlayerStats {
    public int killedDetectives;
    public int killedInnocents;
    public int murderersKilled;
    public int roundsWonMurderer;
    public int roundsWonInnocent;
    public int roundsLostMurderer;
    public int roundsLostInnocent;
    public int randomKills;
    public int coins;

    public PlayerStats(int killedDetectives, int killedInnocents, int murderersKilled, int roundsWonMurderer, int roundsWonInnocent, int roundsLostMurderer, int roundsLostInnocent, int randomKills, int coins) {
        this.killedDetectives = killedDetectives;
        this.killedInnocents = killedInnocents;
        this.murderersKilled = murderersKilled;
        this.roundsWonMurderer = roundsWonMurderer;
        this.roundsWonInnocent = roundsWonInnocent;
        this.roundsLostMurderer = roundsLostMurderer;
        this.roundsLostInnocent = roundsLostInnocent;
        this.randomKills = randomKills;
        this.coins = coins;
    }

    public int getPoints() {
        return killedDetectives * 10 + killedInnocents * 5 + murderersKilled * 15 + roundsWonMurderer * 25 + roundsWonInnocent * 15 - roundsLostMurderer * 5 - roundsLostInnocent * 10 - randomKills * 5;
    }

    public int getRoundsPlayed() {
        return roundsWonMurderer + roundsWonInnocent + roundsLostMurderer + roundsLostInnocent;
    }

    public String getWinRate() {
        double winRate;
        if (getRoundsPlayed() == 0) {
            winRate = 0; // Avoid division by zero, return 0% if no rounds played
        } else {
            winRate = ((double) (roundsWonMurderer + roundsWonInnocent) / getRoundsPlayed()) * 100;
        }
        return String.format("%.2f%%", winRate);
    }

    public void addWin(Role role) {
        if (role == Role.MURDERER) {
            roundsWonMurderer++;
        } else if (role == Role.INNOCENT) {
            roundsWonInnocent++;
        }
    }

    public void addLoss(Role role) {
        if (role == Role.MURDERER) {
            roundsLostMurderer++;
        } else if (role == Role.INNOCENT) {
            roundsLostInnocent++;
        }
    }
}
