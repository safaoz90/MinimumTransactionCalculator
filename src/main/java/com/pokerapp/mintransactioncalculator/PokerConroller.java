package com.pokerapp.mintransactioncalculator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pokerapp.mintransactioncalculator.entity.Balance;
import com.pokerapp.mintransactioncalculator.entity.GameResult;
import com.pokerapp.mintransactioncalculator.entity.GameSetting;
import com.pokerapp.mintransactioncalculator.entity.Leaderboard;
import com.pokerapp.mintransactioncalculator.entity.User;

@RestController
public class PokerConroller {
    @Autowired
    GameService service;

    @GetMapping("/game")
    public GameSetting getGame() {
        return service.getGame();
    }

    @PostMapping("/game")
    public void updateGame(@RequestBody GameSetting gameSetting) {
        service.updateGame(gameSetting);
    }

    @PostMapping("/user")
    public void addUser(@RequestBody User user) {
        service.addUser(user);
    }

    @GetMapping("/user")
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }

    @DeleteMapping("/user/{username}")
    public void deleteUser(@PathVariable String username) {
        service.deleteUser(username);
    }

    @GetMapping("/transactions")
    public ResponseEntity<GameResult> getMinTransactions() throws Exception {
        try {
            return new ResponseEntity(new GameResult(service.minTransactions(), service.getBalances()), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/leader/{username}")
    public void updateLeader(@PathVariable String username) {
        service.updateLeader(username);
    }

    @GetMapping("/leader")
    public String updateLeader() {
        return service.getLeader();
    }

    @PostMapping(value = "/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        service.parseFile(file);
        return file.getOriginalFilename();
    }

    @GetMapping("/balances")
    public ResponseEntity<List<Balance>> getBalances() throws Exception {
        try {
            return new ResponseEntity(service.getBalances(), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/leaderboard")
    public void publishLeaderboard() throws Exception {
        service.publishLeaderboard();
    }

    @GetMapping(value = "/leaderboard")
    public ResponseEntity<Leaderboard> getLeaderboard() throws JsonMappingException, JsonProcessingException {
        return new ResponseEntity(service.getLeaderboard(), HttpStatus.OK);
    }
}
