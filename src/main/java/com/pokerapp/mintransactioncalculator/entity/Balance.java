package com.pokerapp.mintransactioncalculator.entity;

public class Balance {
    public String name;
    public int balance = 0;

    public Balance(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }
}
