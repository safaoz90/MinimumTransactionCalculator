package com.pokerapp.mintransactioncalculator.entity;

import java.util.List;

public class GameResult {
    public List<Transaction> transactions;
    public List<Balance> balances;

    public GameResult() {
    }

    public GameResult(List<Balance> balances) {
        this.balances = balances;
    }

    public GameResult(List<Transaction> transactions, List<Balance> balances) {
        this.transactions = transactions;
        this.balances = balances;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public List<Balance> getBalances() {
        return this.balances;
    }
}
