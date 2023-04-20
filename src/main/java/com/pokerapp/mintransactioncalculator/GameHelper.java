package com.pokerapp.mintransactioncalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.pokerapp.mintransactioncalculator.entity.Balance;
import com.pokerapp.mintransactioncalculator.entity.GameSetting;
import com.pokerapp.mintransactioncalculator.entity.Transaction;
import com.pokerapp.mintransactioncalculator.entity.User;

public final class GameHelper {

  public static List<Transaction> minTransactions(List<Balance> balances) {
    balances.sort((a, b) -> a.balance - b.balance);
    var transactions = minDfs(0, balances.stream().mapToInt(b -> b.balance).toArray());
    return transactions
        .stream()
        .map(t -> new Transaction(balances.get(t[0]).name, balances.get(t[1]).name, t[2]))
        .toList();
  }

  private static List<int[]> minDfs(int start, int[] list) {
    while (start < list.length && list[start] == 0) {
      start++;
    }

    if (start == list.length) {
      return new ArrayList<int[]>();
    }

    int min = Integer.MAX_VALUE;
    List<int[]> res = null;

    for (int i = start + 1; i < list.length; i++) {
      if (list[i] * list[start] < 0) {
        if (Math.abs(list[i]) < Math.abs(list[start])) {
          int temp = list[i];
          list[start] += temp;
          list[i] = 0;
          var localMin = minDfs(start, list);

          if (localMin.size() + 1 < min) {
            min = localMin.size() + 1;
            res = new ArrayList<int[]>(localMin);
            res.add(new int[] { start, i, temp });
          }

          list[start] -= temp;
          list[i] = temp;
        } else {
          list[i] += list[start];
          var localMin = minDfs(start + 1, list);
          if (localMin.size() + 1 < min) {
            min = localMin.size() + 1;
            res = new ArrayList<int[]>(localMin);
            res.add(new int[] { start, i, -list[start] });
          }
          list[i] -= list[start];
        }
      }
    }

    return res;
  }

  public static Map<String, User> parseCsv(MultipartFile file, GameSetting setting) throws IOException {
    Map<String, User> players = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
      String line;
      boolean header = true;

      while ((line = br.readLine()) != null) {
        if (header) {
          header = false;
          continue;
        }

        String[] values = line.split(",");
        String playerNickname = values[0].replace("\"", "");
        int stack = Integer.parseInt(values[6]);

        if (players.containsKey(playerNickname)) {
          User playerData = players.get(playerNickname);
          playerData.setBuyIn(playerData.getBuyIn() + 1);
          playerData.setStack(playerData.getStack() + stack);
        } else {
          players.put(playerNickname, new User(playerNickname, 1, stack));
        }
      }
    }

    return players;
  }
}
