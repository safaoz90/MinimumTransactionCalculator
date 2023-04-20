package com.pokerapp.mintransactioncalculator.entity;

public class Transaction {
    public String from;
    public String to;
    public int amount;
    public Transaction(String from, String to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }
}
