package com.pokerapp.mintransactioncalculator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pokerapp.mintransactioncalculator.entity.Balance;
import com.pokerapp.mintransactioncalculator.entity.GameSetting;
import com.pokerapp.mintransactioncalculator.entity.Transaction;
import com.pokerapp.mintransactioncalculator.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class GameService {
    private GameSetting gameSetting = new GameSetting();
    private Map<String, User> users = new HashMap<>();
    private User leader;

    GameService() {
        this.leader = new User("", 1, 0);
    }

    public void updateGame(GameSetting gameSetting) {
        this.gameSetting = gameSetting;
    }

    public void addUser(User user) {
        users.put(user.name, user);
    }

    public void deleteUser(String username) {
        users.remove(username);
    }

    public List<User> getAllUsers() {
        return users.values().stream().toList();
    }

    public List<Transaction> minTransactions() throws Exception {
        int oneDollarChip = gameSetting.chipAmount / gameSetting.dollarValue;
        var balances = users.values().stream()
                .map(user -> new Balance(user.name, user.stack / oneDollarChip - user.buyIn * gameSetting.dollarValue))
                .collect(Collectors.toList());

        int sum = balances.stream().map(b -> b.balance).reduce(0, Integer::sum);
        balances.add(new Balance("Leader " + leader.name, -sum));
        long positiveCount = balances.stream().filter(b -> b.balance > 0).count();
        if (positiveCount > 10) {
            throw new Exception("More than 10 positive balance is not allowed");
        }

        return GameHelper.minTransactions(balances);
    }

    public void updateLeader(String username) {
        this.leader.name = username;
    }

    public String getLeader() {
        return this.leader.name;
    }

    public void parseFile(MultipartFile file) {
        try {
            this.gameSetting.chipAmount = 3000;
            this.users = GameHelper.parseCsv(file, gameSetting);
            this.leader = users.values().stream().max((a, b) -> (a.stack - a.buyIn * gameSetting.chipAmount)
                    - (b.stack - b.buyIn * gameSetting.chipAmount)).get();
            users.remove(leader.name);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
