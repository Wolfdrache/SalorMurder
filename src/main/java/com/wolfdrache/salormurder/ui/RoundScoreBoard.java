package com.wolfdrache.salormurder.ui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.helper.TabHelper;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;

public class RoundScoreBoard {
    private static class ScoreboardData {
        public Team map;
        public Team time;
        public Team playerStatus;
        public Team playerAlive;
        public Team detectiveAlive;
    }

    private final Map<Player, ScoreboardData> boards = new HashMap<>();

    public void updateScoreBoard(RoundSM round) {
        for (Player player : round.players.keySet()) {
            updateScoreboardForPlayer(player, round);
        }
    }

    public void removeScoreBoard(RoundSM round) {
        for (Player player : round.players.keySet()) {
            removeScoreBoard(player);
        }
    }

    public void removeScoreBoard(Player player) {
        boards.remove(player);
        player.setScoreboard(player.getServer().getScoreboardManager().getNewScoreboard());
    }

    private String getPlayerStatus(PlayerSM playerSM) {
        switch (playerSM.mode) {
            case PLAYING:
                switch (playerSM.role) {
                    case DETECTIVE: return "§aDetektiv";
                    case MURDERER: return "§cMörder";
                    case INNOCENT: return "§bUnschuldiger";
                }
            case SPECTATING:
                return "§3Zuschauer";
            default:
                return "§7Unbekannt";
        }
    }

    private void updateScoreboardForPlayer(Player player, RoundSM round) {
        ScoreboardData data = boards.get(player);
        if (data == null) {
            createScoreboard(player, round);
            data = boards.get(player);
        }

        data.map.suffix(MessageHelper.createComponent("§f" + round.map.name));
        data.time.suffix(MessageHelper.createComponent("§f" + TabHelper.formatTime(round.time)));
        PlayerSM playerSM = round.players.get(player);
        if (playerSM != null) {
            data.playerStatus.suffix(MessageHelper.createComponent("§f" + getPlayerStatus(playerSM)));
        }
        data.playerAlive.suffix(MessageHelper.createComponent("§f" + getAlivePlayersCount(round)));
        data.detectiveAlive.suffix(MessageHelper.createComponent(getDetectiveAlive(round)));
    }

    private String getDetectiveAlive(RoundSM round) {
        if (round.detective != null) {
            PlayerSM detectiveSM = round.players.get(round.detective);
            if (detectiveSM != null) {
                if (detectiveSM.mode == PlayerMode.PLAYING) {
                    return "§a✔";
                } 
            }
        }
        return "§c✖";
    }

    private String getAlivePlayersCount(RoundSM round) {
        int playersInGame = 0;
        int alivePlayersCount = 0;
        for (Player p : round.players.keySet()) {
            PlayerSM playerSM = round.players.get(p);
            if (playerSM != null && playerSM.role != null) {
                playersInGame++;
                if (playerSM.mode == PlayerMode.PLAYING) {
                    alivePlayersCount++;
                }
            }
        }
        return alivePlayersCount + "/" + playersInGame;
    }

    private void createScoreboard(Player player, RoundSM round) {
        if (boards.containsKey(player)) {
            return; // Scoreboard already exists for this player
        }
        ScoreboardData data = new ScoreboardData();

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = board.registerNewObjective("sm", Criteria.DUMMY, MessageHelper.createComponent("§3§lSalor§c§lMurder"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = 15;
        objective.getScore(" ").setScore(score--);

        data.map = registerLine(board, objective, "map", "§1", "§7Runde: ", score--);
        data.time = registerLine(board, objective, "time", "§2", "§7Zeit: ", score--);
        data.playerStatus = registerLine(board, objective, "playerStatus", "§3", "§7Deine Rolle: ", score--);
        data.playerAlive = registerLine(board, objective, "playerAlive", "§4", "§7Spieler: ", score--);
        data.detectiveAlive = registerLine(board, objective, "detectiveAlive", "§5", "§7Detektive: ", score--);

        player.setScoreboard(board);
        boards.put(player, data);
    }

    private Team registerLine(Scoreboard board, Objective objective,
                String teamName, String entry, String prefix, int score) {

        Team team = board.registerNewTeam(teamName);
        team.prefix(MessageHelper.createComponent(prefix));
        team.suffix(MessageHelper.createComponent(""));
        team.addEntry(entry);

        objective.getScore(entry).setScore(score);
        return team;
    }
}
