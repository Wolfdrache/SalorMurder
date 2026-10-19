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
        INNOCENT (true),
        MURDERER (false),
        DETECTIVE (true);

        public final boolean inno;
        Role(boolean inno) {
            this.inno = inno;
        }
    }

    public PlayerMode mode;
    public Role role = null;

    public PlayerSM(PlayerMode mode) {
        this.mode = mode;
    }
}
