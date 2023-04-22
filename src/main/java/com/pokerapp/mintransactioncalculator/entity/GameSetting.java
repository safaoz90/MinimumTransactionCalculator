package com.pokerapp.mintransactioncalculator.entity;

public class GameSetting {
    public int chipAmount = 1000;
    public int dollarValue = 10;
    public String leaderboardId = "";

    public GameSetting(int chipAmount, int dollarValue, String leaderboardId){
        this.chipAmount = chipAmount;
        this.dollarValue = dollarValue;
        this.leaderboardId = leaderboardId;
    }

    public GameSetting() {
    }

    public int getChipAmount() {
        return this.chipAmount;
    }

    public int getDollarValue() {
        return this.dollarValue;
    }

    public String getLeaderboardId() {
        return this.leaderboardId;
    }
}
