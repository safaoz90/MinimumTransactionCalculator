package com.pokerapp.mintransactioncalculator.entity;

public class GameSetting {
    public int chipAmount = 1000;
    public int dollarValue = 10;

    public GameSetting(int chipAmount, int dollarValue){
        this.chipAmount = chipAmount;
        this.dollarValue = dollarValue;
    }

    public GameSetting() {
    }

    public int getChipAmount(){
        return this.chipAmount;
    }

    public int getDollarValue(){
        return this.dollarValue;
    }
}
