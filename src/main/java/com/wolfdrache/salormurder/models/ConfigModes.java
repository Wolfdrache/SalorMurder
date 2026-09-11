package com.wolfdrache.salormurder.models;

public class ConfigModes {
    public static enum Time {
        LOBBY,
        BEFORE_START,
        END,
        TRIDENT_RELOAD;
    }
    public static enum Coins {
        MURDER_KILL_DETECTIVE,
        MURDER_KILL_INNO,
        DETECTIVE_KILL_MURDER,
        RANDOM_KILL,
        MURDER_WIN,
        INNO_WIN;
    }
}
