package com.todo.encryptDecrypt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import com.todo.encryptDecrypt.Base64;

public class cryptoService {

    private static Cipher cipher;
    private static SecretKey secretKey;


    public void init(ServletContext context) throws NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException, CertificateException, IOException {
        Security.addProvider(new BouncyCastleProvider());

        // Cipher - represents a cryptographic algorithm --> Algorithm is set here
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");


        // Try to get the key from the keystore
        // Create/Load a KeyStore
        //Path currentRelativePath = Paths.get("");
        //System.out.println("Current relative path = " + currentRelativePath.toRealPath().toString());


        KeyStore keyStore = getKeyStore("123abc", "/WEB-INF/crypto/keystore.ks", context);
        System.out.println("KeyStore: " + keyStore);

        // Get the key from the keystore
        Key keyEntry = getEntryFromKeyStore("keyAlias", "keyPassword", keyStore);
        System.out.println("KeyEntry: " + keyEntry);

        if(keyEntry != null){
            // Assign the key from the keyStore to the secret key
            secretKey = (SecretKey) keyEntry;
            System.out.println("Secret key HASH: " + secretKey.hashCode());
        } else {
            // Generate key - safely
            secretKey = generateSecretKey("AES", 256);
            // Get the entry pass object. keyPassword & entryPassword - password of the entry, not the entire keyStore
            saveKeyToKeystore(secretKey, "keyPassword", "keyAlias", keyStore);

            // Save the keystore to file
            //File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
            //System.out.println("CatlinaBase: "  + catalinaBase.getAbsoluteFile().toString());
            //File keyStorePath = new File(catalinaBase, "crypto/keystore.ks");
            //System.out.println("KeyStorePath: " + keyStorePath.getAbsolutePath());
            //String keyStorePathString = keyStorePath.getAbsolutePath();
            String keyStorePath = context.getRealPath("/WEB-INF/crypto/keystore.ks");
            System.out.println("KeyStorePath - SAVE: " + keyStorePath);

            try (FileOutputStream keyStoreOutputStream = new FileOutputStream(keyStorePath)){
                keyStore.store(keyStoreOutputStream, "123abc".toCharArray());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // Convert Array->String
    // https://stackoverflow.com/questions/9098022/problems-converting-byte-array-to-string-and-back-to-byte-array
    public static String encrypt(String plainText) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] textToArray = plainText.getBytes();
        //System.out.println("Original text --> Array " + Arrays.toString(textToArray));
        byte[] cipherText = cipher.doFinal(textToArray);
        //System.out.println("Cipher: " + Arrays.toString(cipherText));
        String cipherString = Base64.encodeToString(cipherText, Base64.NO_WRAP);

        //System.out.println("Cipher text: " + cipherString);

        return cipherString;
    }

    public static String decrypt(String cipherText) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        //System.out.println("To decrypt - cipherText: " + cipherText);
        byte[] encryptedArray = Base64.decode(cipherText, Base64.NO_WRAP);
        //System.out.println("EncryptedArray: " + Arrays.toString(encryptedArray));
        byte[] decryptedArray = cipher.doFinal(encryptedArray);
        //System.out.println("DecryptedArray:" + Arrays.toString(decryptedArray));
        //System.out.println("Decrypted text: " + new String(decryptedArray));
        String decryptedString = new String(decryptedArray);
        //System.out.println("Decrypted Array: " + decryptedString);

        return decryptedString;
    }

    private static void calculateMAC(byte[] plainText, SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException {

        // MAC (Message Authentication Code)
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] macBytes = mac.doFinal(plainText);
        System.out.println("MAC: " + new String(macBytes));
    }

    // Message digest - check if the message was modified during transport
    private static void calculateMessageDigest(byte[] plainText) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(plainText);
        System.out.println("Digest : " + new String(digest));

    }

    private static Key getEntryFromKeyStore(String keyAlias, String entryPass, KeyStore keyStore) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(entryPass.toCharArray());
        //KeyStore.Entry keyEntry = keyStore.getEntry(keyAlias, entryPassword);
        Key keyEntry = keyStore.getKey(keyAlias, entryPass.toCharArray());

        return keyEntry;
    }

    private static void saveKeyToKeystore(SecretKey secretKey, String entryPass, String keyAlias, KeyStore keyStore) throws KeyStoreException {

        char[] keyPassword = entryPass.toCharArray();
        KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(keyPassword);

        // Prepare the secret key and store it to the keyStore
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(keyAlias, secretKeyEntry, entryPassword);
    }


    // Creates an empty keystore if it does not exist on disk/loads existing keystore
    // pass - Password of the entire keystore
    private static KeyStore getKeyStore(String keyStorePass, String keyStoreFile, ServletContext context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        //File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
        //System.out.println("CatlinaBase: "  + catalinaBase.getAbsoluteFile().toString());
        //File keyStorePath = new File(catalinaBase, keyStoreFile);
        //System.out.println("KeyStorePath: " + keyStorePath.getAbsolutePath());

        String keyStorePath = context.getRealPath(keyStoreFile);
        System.out.println("KeyStorePath - READ: " + keyStorePath);

        //InputStream inputStream = new FileInputStream( propertyFile );

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] keyStorePassword = keyStorePass.toCharArray();
        try(InputStream keyStoreData = new FileInputStream(keyStorePath)){
            keyStore.load(keyStoreData, keyStorePassword);
            System.out.println("Keystore found on disk");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("New keystore initialized");
            keyStore.load(null, keyStorePassword);
        }

        return keyStore;
    }

    // Generates secret key for encryption
    private static SecretKey generateSecretKey(String algorithm, int keyBitSize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(keyBitSize, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey;
    }
}
