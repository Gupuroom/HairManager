package com.example.hm_project.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static java.util.Base64.*;


/***
 *  Crypto ( 암호화 )
 *  1 - aesKeyGen ( AES 대칭키 생성 [난수] )
 *  2 - encryptAES256 ( AES256 암호화 )
 *  3 - decryptAES256 ( AES256 복호화 )
 */


public class Crypto {

    public static String secretKEY = "";

    // 1 - aesKeyGen ( AES 대칭키 생성 [난수] )
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void aesKeyGen() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        KeyGenerator generator = KeyGenerator.getInstance("AES");  // 키생성에 사용할 암호 알고리즘
        SecureRandom secureRandom = new SecureRandom(); // 안전한 난수 생성 'math random'보다 보안 강도가 높음
        generator.init(256, secureRandom); // 충분한 키 길이 및 난수를 이용하여 키 초기화
        Key secureKey = generator.generateKey();

        // 누가버전까지는 Base64.encodeBase64String NotMethod 이슈발생
        // 대칭키 객체를 'String'으로 변환

        secretKEY = Base64.getEncoder().encodeToString(secureKey.getEncoded());
    }

    // 2 - encryptAES256 ( AES256 암호화 )
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptAES256(String msg) throws Exception {

        String key = secretKEY;
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        byte[] saltBytes = bytes;

        // Password-Based Key Derivation function 2
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // 70000번 해시하여 256 bit 길이의 키를 만든다.
        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltBytes, 70000, 256);


        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        // 알고리즘/모드/패딩
        // CBC : Cipher Block Chaining Mode
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, secret);

        AlgorithmParameters params = cipher.getParameters();

        // Initial Vector(1단계 암호화 블록용)

        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] encryptedTextBytes = cipher.doFinal(msg.getBytes("UTF-8"));
        byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];

        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);

        return Base64.getEncoder().encodeToString(buffer);
    }

    // 3 - decryptAES256 ( AES256 복호화 )
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decryptAES256(String msg) throws Exception {

        String key = secretKEY;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(msg));

        byte[] saltBytes = new byte[20];
        buffer.get(saltBytes, 0, saltBytes.length);
        byte[] ivBytes = new byte[cipher.getBlockSize()];
        buffer.get(ivBytes, 0, ivBytes.length);
        byte[] encryoptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
        buffer.get(encryoptedTextBytes);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltBytes, 70000, 256);

        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] decryptedTextBytes = cipher.doFinal(encryoptedTextBytes);

        return new String(decryptedTextBytes);
    }
}



