package com.fer;

import java.io.*;
import java.util.*;

import com.lambdaworks.crypto.SCryptUtil;

public class Login {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 1) {
            System.out.println("Not enough arguments.");
            System.exit(0);
        }
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance.");
            System.exit(0);
        }
        String username = args[0];
        File database = new File("database.txt");
        database.createNewFile();
        Scanner sc = new Scanner(database).useDelimiter("\\Z");
        String wholeDatabase;
        String[] inserts = new String[0];
        if (sc.hasNext()){
            wholeDatabase = sc.next();
            inserts = wholeDatabase.split("\n");
        }
        Map<String, String> datMapa = new HashMap<>();
        for (String line : inserts) {
            String pom[] = line.split(" ");
            datMapa.put(pom[0], pom[1] + " " + pom[2]);
        }
        int numOfTries = 0;
        while (numOfTries < 3) {
            numOfTries++;
            System.out.println("Password:");
            String password = new String(console.readPassword());
            if (datMapa.get(username) != null) {
                String pom = datMapa.get(username);
                String hashedPassword = pom.substring(0, pom.length() - 2);
                String forcedChange = pom.substring(pom.length() - 1, pom.length());
                if (SCryptUtil.check(password, hashedPassword)) {
                    if (!forcedChange.equals("0")) {
                        while (true) {
                            System.out.println("New password:");
                            String newPassword = new String (console.readPassword());
                            System.out.println("Repeat new password:");
                            String repeatNewPassword = new String (console.readPassword());
                            if (!newPassword.equals(repeatNewPassword)) {
                                System.out.println("Password change failed. Password mismatch. Try again");
                            }
                            else if (newPassword.length() < 8){
                                System.out.println("Password change failed. Password must be at least 8 characters.");
                            }
                            else {
                                datMapa.put(username, SCryptUtil.scrypt(newPassword, 65536, 16, 1) + " " + "0");
                                StringBuilder sb = new StringBuilder();
                                for (Map.Entry<String, String> linije : datMapa.entrySet()) {
                                    sb.append(linije.getKey() + " " + linije.getValue() + "\n");
                                }
                                PrintWriter writer = new PrintWriter(database);
                                writer.print(sb.toString());
                                writer.flush();
                                writer.close();
                                System.out.println("Password change successful.");
                                break;
                            }
                        }
                    }
                    System.out.println("Login successful.");
                    break;
                }
                else {
                    System.out.println("Username or password incorrect.");
                    if (numOfTries >= 3) {
                        System.out.println("Exceeded 3 tries. Exiting.");
                        System.exit(0);
                    }
                }
            } else {
                System.out.println("Username or password incorrect.");
                if (numOfTries >= 3) {
                    System.out.println("Exceeded 3 tries. Exiting.");
                    System.exit(0);
                }
            }
        }
    }
}
