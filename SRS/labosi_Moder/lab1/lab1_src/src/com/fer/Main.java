package com.fer;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args[0].equals("init")) {
            //inicijalizacija samo na pocetku
            File database = new File("database.txt");
            database.createNewFile();
            File saltIV = new File("saltIV.bin");
            saltIV.createNewFile();
            String initialText = "PrviRedZa Enkripciju";
            String encryptedInitialText = encrypt(initialText, args[1]);
            PrintWriter writer = new PrintWriter(database);
            writer.print(encryptedInitialText);
            writer.close();
            createHMAC(encryptedInitialText, args[1]);
            System.out.println("Password manager initialized\n");
        }
        else if (args[0].equals("put")) {
            File database = new File("database.txt");
            String encryptedText = new Scanner(database).useDelimiter("\\Z").next();
            String decryptedText = decrypt(encryptedText, args[1]);
            if (decryptedText == null) {
                System.exit(0);
                //program zavrsava ako je integrity narusen, ili je krivi masterpassword upisan
            }else if(!checkHMAC(encryptedText, args[1])){
                System.out.println("Master password incorrect or integrity check failed\n");
                System.exit(0);
            }
            else {
                //konvertiram u mapu cisto iz razloga jer je lakse s njom raditi, iako ta solucija nije bas za velike performanse
                String[] inserts = decryptedText.split("\n");
                Map<String, String> mapaStranica = new HashMap<>();
                for (int i = 0; i < inserts.length; i++) {
                    mapaStranica.put(inserts[i].split(" ")[0], inserts[i].split(" ")[1]);
                }
                //do tud sam napravil mapu sa svim insertima, te samo insertam
                //ili updateam password na danoj stranici ako vec postoji
                mapaStranica.put(args[2], args[3]);

                //te napravim opet string, enkriptiram i natrag opet pospremim
                StringBuilder sb = new StringBuilder();
                boolean first = false;
                for (Map.Entry<String, String> entry : mapaStranica.entrySet()) {
                    if (first == false){
                        sb.append(entry.getKey() + " " + entry.getValue());
                        first = true;
                        continue;
                    }
                    sb.append("\n" + entry.getKey() + " " + entry.getValue());
                }

                encryptedText = encrypt(sb.toString(), args[1]);
                PrintWriter writer = new PrintWriter(database);
                writer.print(encryptedText);
                writer.flush();
                writer.close();
                createHMAC(encryptedText, args[1]);
                System.out.println("Stored password for " + args[2] + "\n");
            }
        }else if(args[0].equals("get")){
            File database = new File("database.txt");
            String encryptedText = new Scanner(database).useDelimiter("\\Z").next();
            String decryptedText = decrypt(encryptedText, args[1]);
            if (decryptedText == null) {
                System.exit(0);
                //program zavrsava ako je integrity narusen, ili je krivi masterpassword upisan
            } else if(!checkHMAC(encryptedText, args[1])){
                System.out.println("Master password incorrect or integrity check failed\n");
                System.exit(0);
            } else {
                //konvertiram u mapu cisto iz razloga jer je lakse s njom raditi, iako ta solucija nije bas za velike performanse
                String[] inserts = decryptedText.split("\n");
                Map<String, String> mapaStranica = new HashMap<>();
                for (int i = 0; i < inserts.length; i++) {
                    mapaStranica.put(inserts[i].split(" ")[0], inserts[i].split(" ")[1]);
                }
                //do ovog dijela je isto ko i u put komandi, a dalje samo dohvatim i ispišem (ako postoji)
                String gettedPassword = mapaStranica.get(args[2]);
                if (gettedPassword == null){
                    System.out.println("No stored password for " + args[2] + "\n");
                }else{
                    System.out.println("Password for " + args[2] + " is: " + gettedPassword + "\n");
                }
            }
        }
    }

    public static String encrypt(String encString, String masterPassword) {
        try {
            SecureRandom random = new SecureRandom();
            // treba spremiti IV i salt u datoteku
            byte[] iv = random.generateSeed(16);
            byte[] salt = random.generateSeed(16);
            File saltIV = new File("saltIV.bin");
            FileOutputStream fos = new FileOutputStream(saltIV);
            fos.write(iv);
            fos.write(salt);
            fos.flush();
            fos.close();
            //kreiranje kljuceva za koristenje u AES-u, te enkriptiranje u text formatu
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, 65536, 256);
            SecretKeySpec secretKey = new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), "AES");

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder()
                    .encodeToString(c.doFinal(encString.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String masterPassword) {
        try {
            File saltIV = new File("saltIV.bin");
            FileInputStream fos = new FileInputStream(saltIV);
            byte[] saltIvTogether = fos.readAllBytes();
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            try {
                System.arraycopy(saltIvTogether, 0, iv, 0, 16);
                System.arraycopy(saltIvTogether, 16, salt, 0, 16);
            }
            catch (Exception e){
                System.out.println("Someone tampered with salt and IV data, exiting program");
                System.exit(0);
            }
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, 65536, 256);
            SecretKeySpec secretKey = new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), "AES");

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            c.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(c.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Master password incorrect or integrity check failed\n");
        }
        return null;
    }

    //2 funkcije za redom s kojima kreiram i provjeravam HMAC
    //slicne stvari kao i gore za enkriptiranje i dekriptiranje
    //samo se koristi drugi derivirani kljuc od masterkey-a
    public static void createHMAC(String data, String key) {
        try {
            File hmac = new File("hmac.bin");
            hmac.createNewFile();
            FileOutputStream fos = new FileOutputStream(hmac);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmacbytes = mac.doFinal(data.getBytes());
            fos.write(hmacbytes);
            fos.flush();
            fos.close();
        }
        catch (InvalidKeyException e){
            System.out.println("Master password incorrect or integrity check failed\n");
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("Wrong Algorithm\n");
        }
        catch (IOException e){
            System.out.println("File not found\n");
        }
    }

    public static boolean checkHMAC(String data, String key){
        try {
            File hmac = new File("hmac.bin");
            FileInputStream fos = new FileInputStream(hmac);
            byte[] hmacReadBytes = fos.readAllBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmacbytes = mac.doFinal(data.getBytes());
            return Arrays.equals(hmacbytes, hmacReadBytes);
        }
        catch (InvalidKeyException e){
            System.out.println("Master password incorrect or integrity check failed\n");
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("Wrong Algorithm\n");
        }
        catch (IOException e){
            System.out.println("File not found\n");
        }
        return false;
    }
}
