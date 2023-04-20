package com.pokerapp.mintransactioncalculator.entity;

public class User {
    public String name = "";
    public int buyIn = 1;
    public int stack = 0;

    public User(String name, int buyIn, int stack) {
        this.name = name;
        this.buyIn = buyIn;
        this.stack = stack;
    }

    public String getName() {
        return name;
    }

    public int getBuyIn() {
        return buyIn;
    }

    public int getStack() {
        return stack;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBuyIn(int buyIn) {
        this.buyIn = buyIn;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }
}
