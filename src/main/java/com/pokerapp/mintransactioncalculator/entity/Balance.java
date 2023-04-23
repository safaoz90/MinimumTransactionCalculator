package com.pokerapp.mintransactioncalculator.entity;

public class Balance {
    public String name;
    public int balance = 0;

    public Balance(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }

    public Balance() {
    }

    public Balance(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
