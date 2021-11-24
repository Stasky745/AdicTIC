package com.adictic.common.util;

import android.os.Build;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {
    public static String getAES(String message) {
        MessageDigest sha = null;
        byte[] key;
        SecretKeySpec secretKey = null;
        try {
            key = "ssd32da5hyjaicwn2do49fja4".getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
            } else {
                return android.util.Base64.encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)), android.util.Base64.DEFAULT);
            }
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decryptAES(String message) {
        if(message==null || message.trim().equals("")) return "";
        MessageDigest sha = null;
        byte[] key;
        SecretKeySpec secretKey = null;
        try {
            key = "ssd32da5hyjaicwn2do49fja4".getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
            } else {
                return new String(cipher.doFinal(android.util.Base64.decode(message, android.util.Base64.DEFAULT)));
            }
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static String getSHA256(String message) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(message.getBytes(StandardCharsets.UTF_8));
            result = String.format("%064x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
