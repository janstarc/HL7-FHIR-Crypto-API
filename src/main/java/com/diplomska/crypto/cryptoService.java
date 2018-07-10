package com.diplomska.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.*;
import javax.servlet.ServletContext;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.diplomska.crypto.cryptoDB.getKeyAlias;

public class cryptoService {

    private static Cipher cipher;
    private static SecretKey secretKey;
    private static KeyStore keyStore;
    private static ServletContext ctx;

    public void init(ServletContext context) throws NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException, CertificateException, IOException {

        Security.addProvider(new BouncyCastleProvider());
        ctx = context;

        // Cipher - represents a cryptographic algorithm --> Algorithm is set here
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // Create/Load a KeyStore
            // TODO Figure out what to do if the keystore is not avaliable!
        keyStore = getKeyStore("123abc", "/WEB-INF/crypto/keystore.ks", context);
        System.out.println("KeyStore: " + keyStore);

        // Get the key from the keystore
        Key keyEntry = getEntryFromKeyStore("keyAlias", "keyPassword", keyStore);
        System.out.println("KeyEntry: " + keyEntry);

        if(keyEntry != null){
            // Assign the key from the keyStore to the secret key
            secretKey = (SecretKey) keyEntry;
            System.out.println("Secret key HASH: " + secretKey.hashCode());
        }
    }

    /**
     *
     *  Premisliti situacijo glede kljucev
     *      -Pri encrypt imamo dovolj informacij - le pogledamo, pod katerim kljucem moramo sifrirati
     *
     *      -Pre decrypt nimamo dovolj informacij, ker ne vemo, pod katerim kljucem je user sifriran
     */

    // Convert Array->String
    // https://stackoverflow.com/questions/9098022/problems-converting-byte-array-to-string-and-back-to-byte-array
        // Plain text --> V resnici gre za user_id
    public static String encrypt(String patientId) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        String keyAlias = getKeyAlias(patientId);
        SecretKey secKey = (SecretKey) getEntryFromKeyStore(keyAlias, "keyPassword", keyStore);

        cipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] textToArray = patientId.getBytes();                          // Convert from plainText to byte[]
        byte[] cipherText = cipher.doFinal(textToArray);                    // Encrypt to byte[]
        String cipherString = Base64.encodeToString(cipherText, Base64.NO_WRAP);        // Convert byte[] to hash String without loss

        return cipherString;
    }

    // TODO Change or delete this!
    public static String encryptName(String patientId) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        SecretKey secKey = (SecretKey) getEntryFromKeyStore("keyAlias", "keyPassword", keyStore);

        cipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] textToArray = patientId.getBytes();                          // Convert from plainText to byte[]
        byte[] cipherText = cipher.doFinal(textToArray);                    // Encrypt to byte[]
        String cipherString = Base64.encodeToString(cipherText, Base64.NO_WRAP);        // Convert byte[] to hash String without loss

        return cipherString;
    }

    public static String decrypt(String cipherText) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        cipher.init(Cipher.DECRYPT_MODE, secretKey);                            // Set DECRYPT mode
        byte[] encryptedArray = Base64.decode(cipherText, Base64.NO_WRAP);      // 1 to 1 conversion from hash String to byte[]
        byte[] decryptedArray = cipher.doFinal(encryptedArray);                 // Decryption of byte[]
        String decryptedString = new String(decryptedArray);                    // Convert from byte[] to plainText

        return decryptedString;
    }

    public static String reencryptWithNewKey(String newKeyAlias, String userId) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException {

        // Get old key
        /*
        String oldKeyAlias = getKeyAlias(userId);
        Key keyEntry = getEntryFromKeyStore(oldKeyAlias, "keyPassword", keyStore);
        SecretKey oldKey = (SecretKey) keyEntry;
        cipher.init(Cipher.DECRYPT_MODE, oldKey);
        */
        SecretKey newKey = (SecretKey) getEntryFromKeyStore(newKeyAlias, "keyPasword", keyStore);
        //cipher.init(Cipher.ENCRYPT_MODE, newKey);
        //String newHash = encrypt(userId, // TODO KEY ALIAS);

        return null;
    }

    public static String addNewKeyToKeyStore(String keyAlias) throws NoSuchAlgorithmException, KeyStoreException {
        secretKey = generateSecretKey("AES", 256);

        // Get the entry pass object. keyPassword & entryPassword - password of the entry, not the entire keyStore
        //String keyAlias = getTimestamp();
        saveKeyToKeystore(secretKey, "keyPassword", keyAlias, keyStore);

        // Save the keystore to file
        String keyStorePath = ctx.getRealPath("/WEB-INF/crypto/keystore.ks");       // https://stackoverflow.com/questions/4340653/file-path-to-resource-in-our-war-web-inf-folder; https://stackoverflow.com/questions/35837285/different-ways-to-get-servlet-context
        System.out.println("KeyStorePath - SAVE: " + keyStorePath);

        try (FileOutputStream keyStoreOutputStream = new FileOutputStream(keyStorePath)){
            keyStore.store(keyStoreOutputStream, "123abc".toCharArray());
        } catch (Exception e){
            e.printStackTrace();
        }

        return keyAlias;
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

        //KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(entryPass.toCharArray());
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
    private static KeyStore getKeyStore(String keyStorePass, String keyStoreFile, ServletContext context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        String keyStorePath = context.getRealPath(keyStoreFile);

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

    // Generates secret key for symmetric encryption
    private static SecretKey generateSecretKey(String algorithm, int keyBitSize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(keyBitSize, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey;
    }

    private static String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        return sdf.format(new Date());
    }
}
