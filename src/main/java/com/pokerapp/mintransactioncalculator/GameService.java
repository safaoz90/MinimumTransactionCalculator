package com.pokerapp.mintransactioncalculator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokerapp.mintransactioncalculator.entity.Balance;
import com.pokerapp.mintransactioncalculator.entity.GameResult;
import com.pokerapp.mintransactioncalculator.entity.GameSetting;
import com.pokerapp.mintransactioncalculator.entity.Leaderboard;
import com.pokerapp.mintransactioncalculator.entity.Transaction;
import com.pokerapp.mintransactioncalculator.entity.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class GameService {
    private String scoreboardBucketName =  "scoreleaderboard";
    private String instanceId = "9aaee532-e17d-11ed-b5ea-0242ac120002";

    private String region = "us-west-2";
    private GameSetting gameSetting = new GameSetting();
    private Map<String, User> users = new HashMap<>();
    private List<Balance> balances = new ArrayList<Balance>();
    private User leader;
    private AmazonS3 s3Client;

    GameService() {
        this.leader = new User("", 1, 0);

        BasicAWSCredentials credentials = new BasicAWSCredentials(System.getenv("AWS_ACCESS_KEY"), System.getenv("AWS_SECRET_KEY"));
        s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://s3." + region + ".amazonaws.com", region))
            .build();
    }

    public void updateGame(GameSetting gameSetting) {
        this.gameSetting = gameSetting;
    }

    public GameSetting getGame() {
        return this.gameSetting;
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
        this.balances = users.values().stream()
                .map(user -> new Balance(user.name, user.stack / oneDollarChip - user.buyIn * gameSetting.dollarValue))
                .collect(Collectors.toList());

        int sum = this.balances.stream().map(b -> b.balance).reduce(0, Integer::sum);
        this.balances.add(new Balance("Leader " + leader.name, -sum));
        long positiveCount = balances.stream().filter(b -> b.balance > 0).count();
        if (positiveCount > 10) {
            throw new Exception("More than 10 positive balance is not allowed");
        }

        Collections.reverse(this.balances);
        return GameHelper.minTransactions(this.balances);
    }

    public void updateLeader(String username) {
        this.leader.name = username;
    }

    public String getLeader() {
        return this.leader.name;
    }

    public String getLeaderboardId() {
        return this.gameSetting.leaderboardId;
    }

    public List<Balance> getBalances() {
        return this.balances;
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

    public void publishLeaderboard() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -12);
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }

        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) {
            day = "0" + day;
        }
        
        ObjectMapper objectMapper = new ObjectMapper();

        GameResult result = new GameResult(minTransactions(), getBalances());
        String jsonString = objectMapper.writeValueAsString(result);
        jsonString = jsonString.replace("Leader ", "");

        uploadContent(instanceId + "/" + year + "/" + month + "/" + day, jsonString);
    }

    public Leaderboard getLeaderboard() throws JsonMappingException, JsonProcessingException {
        Leaderboard leaderboard = new Leaderboard();
        List<String> years = getS3Folders(instanceId);
        Map<String, Integer> winCounts = new HashMap<String, Integer>();
        for (String year : years) {
            String numericYear = findLeafFolder(year);
            List<String> months = getS3Folders(year);
            for (String month : months) {
                String numericMonth = findLeafFolder(month);
                List<String> days = getS3Files(month);

                Map<String, Balance> userBalances = new HashMap<String, Balance>();
                for (String day : days) {
                    if (day.endsWith("/")) {
                        continue;
                    }

                    String dailyResult = getS3Content(day);
                    ObjectMapper objectMapper = new ObjectMapper();
                    GameResult gameResult = objectMapper.readValue(dailyResult, GameResult.class);
                    for (Balance balance : gameResult.getBalances()) {
                        if (!userBalances.containsKey(balance.name)) {
                            userBalances.put(balance.name, new Balance(balance.name));
                        }

                        Balance updateBalance = userBalances.get(balance.name);
                        updateBalance.balance += balance.balance;
                    }
                }

                List<Balance> balances = new ArrayList<Balance>(userBalances.values());

                Collections.sort(balances, (o1, o2) -> o2.balance - o1.balance);
                leaderboard.leaderboard.put(numericYear + "-" + numericMonth, new GameResult(balances));

                // No star for current month.
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR, -12);
                String currentYear = String.valueOf(cal.get(Calendar.YEAR));
                String currentMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);
                if (currentMonth.length() == 1) {
                    currentMonth = "0" + currentMonth;
                }
        
                if (currentYear.length() == 1) {
                    currentYear = "0" + currentYear;
                }

                if (numericYear.equals(currentYear) && numericMonth.equals(currentMonth)) {
                    continue;
                }

                String winner = balances.get(0).name;
                if (!winCounts.containsKey(winner)) {
                    winCounts.put(winner, 0);
                }

                winCounts.put(winner, winCounts.get(winner) + 1);
            }
        }

        for (String leaderboardKey : leaderboard.leaderboard.keySet()) {
            GameResult result = leaderboard.leaderboard.get(leaderboardKey);
            for (Balance balance : result.balances) {
                if (winCounts.containsKey(balance.name)) {
                    balance.name += String.join("", Collections.nCopies(winCounts.get(balance.name), "⭐"));
                }
            }
        }

        return leaderboard;
    }
    
    private List<String> getS3Folders(String prefix) {
        String delimiter = "/";
        if (!prefix.endsWith(delimiter)) {
            prefix += delimiter;
        }

        // List all folders in the bucket
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(scoreboardBucketName)
                .withPrefix(prefix)
                .withDelimiter(delimiter);
        ListObjectsV2Result result = s3Client.listObjectsV2(request);
        return result.getCommonPrefixes();
    }
    
    private List<String> getS3Files(String prefix) {
        String delimiter = "/";
        if (!prefix.endsWith(delimiter)) {
            prefix += delimiter;
        }

        // List all folders in the bucket
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(scoreboardBucketName)
                .withPrefix(prefix)
                .withDelimiter(delimiter);
        ListObjectsV2Result result = s3Client.listObjectsV2(request);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        return objects.stream().map(o -> o.getKey()).collect(Collectors.toList());
    }

    private String getS3Content(String filename) {
        // Download the contents of the file
        S3Object s3Object = s3Client.getObject(scoreboardBucketName, filename);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String result = "";
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void uploadContent(String key, String content) {
        // Convert the string content to a byte array and create an object metadata
        byte[] contentBytes = content.getBytes();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentBytes.length);
        
        // Upload the string content as an object to the specified bucket
        PutObjectRequest putObjectRequest = new PutObjectRequest(scoreboardBucketName, key, new ByteArrayInputStream(contentBytes), metadata);
        s3Client.putObject(putObjectRequest);
    }

    private String findLeafFolder(String path) {
        // Check if the path ends with a file separator
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Find the last index of the file separator
        int lastIndex = path.lastIndexOf("/");

        if (lastIndex == -1) {
            // Handle the case where the path doesn't contain any file separator
            return path;
        } else {
            // Extract the substring after the last file separator
            return path.substring(lastIndex + 1);
        }
    }
}
