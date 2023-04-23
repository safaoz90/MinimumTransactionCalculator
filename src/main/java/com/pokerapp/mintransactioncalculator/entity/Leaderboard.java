package com.pokerapp.mintransactioncalculator.entity;

import java.util.HashMap;
import java.util.Map;

public class Leaderboard {
    public Map<String, GameResult> leaderboard;

    public Leaderboard() {
        this.leaderboard = new HashMap<String, GameResult>();
    }
}
