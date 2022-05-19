package com.mojo.consoleplus;
import java.security.MessageDigest;
import java.util.UUID;

public class AuthHandler {
    public static String signatureStub;
    
    public AuthHandler(){
        try {
            signatureStub = UUID.randomUUID().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AuthHandler(String stub) {
        signatureStub = stub;
    }

    public Boolean auth(int uid, long expire, String dg) {
        return digestUid(uid+":"+expire).equals(dg);
    }

    public String genKey(int uid, long expire){
        String part1 = uid +":"+expire;

        return part1 + ":" + digestUid(part1);
    }

    private String digestUid(String payload) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return bytesToHex(digest.digest((payload + ":" + signatureStub).getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
