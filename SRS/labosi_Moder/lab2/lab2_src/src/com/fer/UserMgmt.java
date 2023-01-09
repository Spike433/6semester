package com.fer;

import java.io.*;
import java.util.*;
import com.lambdaworks.crypto.SCryptUtil;

public class UserMgmt {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 2) {
            System.out.println("Not enough arguments.");
            System.exit(0);
        }
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance.");
            System.exit(0);
        }
        String username = args[1];
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
        for (String line : inserts){
            String pom[] = line.split(" ");
            datMapa.put(pom[0], pom[1] + " " + pom[2]);
        }
        if (args[0].equals("add")) {
            if (datMapa.containsKey(args[1])){
                System.out.println("User already exists.");
                System.exit(0);
            }
            System.out.println("Password:");
            String password = new String(console.readPassword());
            System.out.println("Repeat password:");
            String repeatPassword = new String (console.readPassword());
            if (!password.equals(repeatPassword)){
                System.out.println("User add failed. Password mismatch.");
                System.exit(0);
            }
            if (password.length() < 8){
                System.out.println("User add failed. Password must be at least 8 characters.");
                System.exit(0);
            }
            datMapa.put(username, SCryptUtil.scrypt(password, 65536, 16, 1) + " " + "0");
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> linije : datMapa.entrySet()){
                sb.append(linije.getKey() + " " + linije.getValue() + "\n");
            }
            PrintWriter writer = new PrintWriter(database);
            writer.print(sb.toString());
            writer.flush();
            writer.close();
            System.out.println("User " + username + " successfuly added.");
        }
        else if (args[0].equals("passwd")) {
            if (!datMapa.containsKey(username)){
                System.out.println("User does not exist.");
                System.exit(0);
            }
            System.out.println("Password:");
            String password = new String(console.readPassword());
            System.out.println("Repeat password:");
            String repeatPassword = new String (console.readPassword());
            if (!password.equals(repeatPassword)){
                System.out.println("Password change failed. Password mismatch.");
                System.exit(0);
            }
            if (password.length() < 8){
                System.out.println("User add failed. Password must be at least 8 characters.");
                System.exit(0);
            }
            String pom = datMapa.get(username);
            String forcedChange = pom.substring(pom.length()-1, pom.length());
            datMapa.put(username, SCryptUtil.scrypt(password, 65536, 16, 1) + " " + forcedChange);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> linije : datMapa.entrySet()){
                sb.append(linije.getKey() + " " + linije.getValue() + "\n");
            }
            PrintWriter writer = new PrintWriter(database);
            writer.print(sb.toString());
            writer.flush();
            writer.close();
            System.out.println("Password change successful.");

        }
        else if(args[0].equals("forcepass")){
            if (!datMapa.containsKey(username)){
                System.out.println("User does not exist.");
                System.exit(0);
            }
            String pom = datMapa.get(username);
            datMapa.put(username, pom.substring(0, pom.length()-2) + " " + "1");
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> linije : datMapa.entrySet()){
                sb.append(linije.getKey() + " " + linije.getValue() + "\n");
            }
            PrintWriter writer = new PrintWriter(database);
            writer.print(sb.toString());
            writer.flush();
            writer.close();
            System.out.println("User will be requested to change password on next login.");

        }
        else if(args[0].equals("del")){
            if (!datMapa.containsKey(username)){
                System.out.println("User does not exist.");
                System.exit(0);
            }
            datMapa.remove(username);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> linije : datMapa.entrySet()){
                sb.append(linije.getKey() + " " + linije.getValue() + "\n");
            }
            PrintWriter writer = new PrintWriter(database);
            writer.print(sb.toString());
            writer.flush();
            writer.close();
            System.out.println("User successfuly removed.");
        }
    }
}
