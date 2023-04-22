package com.pokerapp.mintransactioncalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pokerapp.mintransactioncalculator.entity.Balance;
import com.pokerapp.mintransactioncalculator.entity.GameSetting;
import com.pokerapp.mintransactioncalculator.entity.Transaction;
import com.pokerapp.mintransactioncalculator.entity.User;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class GameService {
    private GameSetting gameSetting = new GameSetting();
    private Map<String, User> users = new HashMap<>();
    private List<Balance> balances = new ArrayList<Balance>();
    private User leader;

    GameService() {
        this.leader = new User("", 1, 0);
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

    public String publishLeaderboard() {
        try {
            String leaderboardId = getLeaderboardId();

            // create URL object
            URL url = new URL("https://keepthescore.co/api/" + leaderboardId + "/board/");

            // create HttpURLConnection object
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set request method
            con.setRequestMethod("GET");
            con.setRequestProperty("Referer", "https://keepthescore.co/board/" + leaderboardId);

            // read response body
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            
            JSONArray rounds = jsonObject.getJSONArray("rounds");
            for (int i = 0; i < rounds.length(); ++i) {
                JSONObject round = rounds.getJSONObject(i);
                String dateStr = round.getString("date");

                SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                Date date = dt.parse(dateStr);

                long twoDaysAgo = System.currentTimeMillis() - 1000*60*60*24*2;
                Date prev = new Date(twoDaysAgo);
                if (date.after(prev)) {
                    return "Leaderboard is already published. Skipped this operation.";
                }
            }

            JSONArray players = jsonObject.getJSONArray("players");

            List<JSONObject> sortedPlayers = new ArrayList<JSONObject>();
            for (int i = 0; i < players.length(); i++) {
                sortedPlayers.add(players.getJSONObject(i));
            }

            Collections.sort(sortedPlayers, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private static final String KEY_NAME = "name";
        
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    valA = a.getString(KEY_NAME);
                    valB = b.getString(KEY_NAME);
                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });

            // create request body
            String requestBody = "";
            for (int i = 0; i < sortedPlayers.size(); ++i) {
                JSONObject playerJson = sortedPlayers.get(i);
                String playerName = playerJson.getString("name").split(" ")[0].toLowerCase();

                Balance balance = balances.stream().filter(b -> b.name.replace("Leader ", "").toLowerCase().equals(playerName))
                        .findFirst().orElse(null);

                if (requestBody.length() > 0) {
                    requestBody += "&";
                }

                if (balance == null) {
                    // User did not play or did not lose or did not win. Emit zeros!
                    requestBody += "scores-" + i + "-player_name=" + playerJson.getString("name").replace(" ", "+");
                    requestBody += "&scores-" + i + "-score_format=INTEGER";
                    requestBody += "&scores-" + i + "-player_id=" + String.valueOf(playerJson.getInt("id"));
                    requestBody += "&scores-" + i + "-single_score=0";
                    continue;
                }

                requestBody += "scores-" + i + "-player_name=" + playerJson.getString("name").replace(" ", "+");
                requestBody += "&scores-" + i + "-score_format=INTEGER";
                requestBody += "&scores-" + i + "-player_id=" + String.valueOf(playerJson.getInt("id"));
                requestBody += "&scores-" + i + "-single_score=" + String.valueOf(balance.balance);
            }

            Calendar now = Calendar.getInstance();
            requestBody += "&comment=" + URLEncoder.encode((now.get(Calendar.MONTH) + 1) + "/" + (now.get(Calendar.DAY_OF_MONTH) + 1), "UTF-8");

            // create URL object
            url = new URL("https://keepthescore.co/round_add/" + leaderboardId + "/");

            // create HttpURLConnection object
            con = (HttpURLConnection) url.openConnection();

            // set request method
            con.setRequestMethod("POST");
            con.setRequestProperty("Referer", "https://keepthescore.co/round_add/" + leaderboardId + "/");

            // set request headers
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // enable output and disable caching
            con.setDoOutput(true);
            con.setUseCaches(false);

            // write request body
            OutputStream outputStream = con.getOutputStream();
            byte[] requestBodyBytes = requestBody.getBytes("UTF-8");
            outputStream.write(requestBodyBytes);
            outputStream.flush();
            outputStream.close();

            // read response body
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return "Leaderboard published.";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            return e.toString() + sStackTrace;
        }
    }
}
