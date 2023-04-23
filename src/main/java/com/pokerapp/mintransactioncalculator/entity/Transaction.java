package com.pokerapp.mintransactioncalculator.entity;

public class Transaction {
    private String from;
    private String to;
    private int amount;

    public Transaction(String from, String to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Transaction() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
