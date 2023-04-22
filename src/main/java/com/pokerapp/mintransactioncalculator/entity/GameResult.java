package com.pokerapp.mintransactioncalculator.entity;

import java.util.List;

public class GameResult {
    public List<Transaction> transactions;
    public List<Balance> balances;

    public GameResult(List<Transaction> transactions, List<Balance> balances) {
        this.transactions = transactions;
        this.balances = balances;
    }
}
