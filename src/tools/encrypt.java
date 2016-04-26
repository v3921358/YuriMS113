/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 *
 * @author yuri
 */
public class encrypt {
    
public static boolean checkSHA1(String password, String hash){
    String sha1 = "";
    try
    {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(password.getBytes("UTF-8"));
        sha1 = byteToHex(crypt.digest());
    }
    catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
    {
        e.printStackTrace();
    }
    
    return sha1.equals(hash);
}
    
public static boolean checkSHA512(String password, String hash){
    String sha1 = "";
    try
    {
        MessageDigest crypt = MessageDigest.getInstance("SHA-512");
        crypt.reset();
        crypt.update(password.getBytes("UTF-8"));
        sha1 = byteToHex(crypt.digest());
    }
    catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
    {
        e.printStackTrace();
    }
    
    return sha1.equals(hash);
}
    
 public static  String sha1(String password)
{
    String sha1 = "";
    try
    {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(password.getBytes("UTF-8"));
        sha1 = byteToHex(crypt.digest());
    }
    catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
    {
        e.printStackTrace();
    }
    
    return sha1;
}

private static String byteToHex(final byte[] hash)
{
    Formatter formatter = new Formatter();
    for (byte b : hash)
    {
        formatter.format("%02x", b);
    }
    String result = formatter.toString();
    formatter.close();
    return result;
}
    
    
    
}
