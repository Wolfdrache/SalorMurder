package com.wolfdrache.salormurder.models;

public class PlayerSM {
    public static enum PlayerMode {
        WAITING,
        PLAYING,
        SPECTATING,
        ENDING,
        EDIT
    }

    public static enum Role {
        INNOCENT,
        MURDERER,
        DETECTIVE
    }

    public PlayerMode mode;
    public Role role = null;

    public PlayerSM(PlayerMode mode) {
        this.mode = mode;
    }
}
