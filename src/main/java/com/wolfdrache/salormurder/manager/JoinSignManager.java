package com.wolfdrache.salormurder.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class JoinSignManager {
    private final RoundManager roundManager;
    private final FileManager fileManager;

    private final Map<Location, RoundSM> joinSigns = new HashMap<>();
    private final Map<RoundSM, Location> roundToSign = new HashMap<>();

    public JoinSignManager(RoundManager roundManager, FileManager fileManager) {
        this.roundManager = roundManager;
        this.fileManager = fileManager;
        loadJoinSigns();
        setupSigns();
    }

    private void loadJoinSigns() {
        List<Location> signLocations = fileManager.getJoinSigns();
        for (Location location : signLocations) {
            joinSigns.put(location, null);
        }
    }

    public void setupSigns() {
        List<RoundSM> waitingRounds = roundManager.getRoundsByMode(RoundMode.WAITING);
        Collections.shuffle(waitingRounds);
        for (RoundSM round : waitingRounds) {
            if (!hasFreeSign()) break;
            if (!hasRoundSign(round)) {
                giveRoundToSign(round);
            }
        }
        updateAllSigns();
    }

    public void replaceRoundSign(RoundSM round) {
        if (!hasRoundSign(round)) return;
        Location signLocation = getSignForRound(round);
        removeRoundFromSign(round);
        List<RoundSM> waitingRounds = roundManager.getRoundsByMode(RoundMode.WAITING);
        waitingRounds.removeAll(roundToSign.keySet());
        RoundSM newRound = waitingRounds.isEmpty() ? null : waitingRounds.get(0);
        if (newRound != null) {
            giveRoundToSign(newRound);
            updateSign(newRound);
        } else {
            setEmptySign(signLocation);
        }
    }

    public void interactWithSign(Location signLocation, Player player) {
        if (!joinSigns.containsKey(signLocation)) return; // Not a join sign
        RoundSM round = joinSigns.get(signLocation);
        if (round == null) return; // No round associated with this sign
        roundManager.joinPlayer(player, round);
    }

    public boolean hasRoundSign(RoundSM round) {
        return roundToSign.containsKey(round);
    }

    private Location getSignForRound(RoundSM round) {
        return roundToSign.get(round);
    }

    public boolean hasFreeSign() {
        return joinSigns.containsValue(null);
    }

    public void giveRoundToSign(RoundSM round) {
        if (roundToSign.containsKey(round)) return; 
        for (Map.Entry<Location, RoundSM> entry : joinSigns.entrySet()) {
            if (entry.getValue() == null) {
                entry.setValue(round);
                roundToSign.put(round, entry.getKey());
                updateSign(round);
                return;
            }
        }
    }

    public void removeRoundFromSign(RoundSM round) {
        Location signLocation = roundToSign.remove(round);
        if (signLocation != null) {
            joinSigns.put(signLocation, null);
            setEmptySign(signLocation);
        }
    }

    public void updateAllSigns() {
        for (Location location : joinSigns.keySet()) {
            RoundSM round = joinSigns.get(location);
            if (round != null) {
                updateSign(round);
            }
            else {
                setEmptySign(location);
            }
        }
    }

    private void setEmptySign(Location location) {
        if(!(location.getBlock().getState() instanceof Sign sign)) return;
        sign.line(0, MessageHelper.createComponent("§3Salor§cMurder"));
        sign.line(1, MessageHelper.createComponent("§7Keine Runde"));
        sign.line(2, MessageHelper.createComponent("§7verfügbar"));
        sign.line(3, MessageHelper.createComponent("§e-----------"));
        sign.update();
    }

    public void updateSign(RoundSM round) {
        Location location = getSignForRound(round);
        if (location == null) return;
        if(!(location.getBlock().getState() instanceof Sign sign)) return;
        sign.line(0, MessageHelper.createComponent("§3Salor§cMurder"));
        sign.line(1, MessageHelper.createComponent("§7Click to join"));
        sign.line(2, MessageHelper.createComponent("§e" + round.map.name));
        sign.line(3, MessageHelper.createComponent("§a" + round.players.size() + "/" + round.map.getMaxPlayers() + " Spieler"));
        sign.update();
    }
}
