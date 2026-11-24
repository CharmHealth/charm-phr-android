package com.charm.phr.mphr.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceManager;

import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;


public class KeyStoreUtil {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore"; //No I18N
    public static final String IAM_AUTHTOKEN = "IamAuthToken"; //No I18N
//    public static final String PASSCODE = "Passcode"; //No I18N
//    public static final String USERNAME = "UserName"; //No I18N


    //bfr api M
//    private static final String KEY_STORE_ALIAS_1 = "KeyStoreAlias1"; //No I18N
//    public static final String ENCRYPTED_KEY = "EncryptedKey"; //No I18N

    //aftr api M
    private static final String KEY_STORE_ALIAS_2 = "KeyStoreAlias2"; //No I18N
    public static final String AUTHTOKEN_VECTOR = "AuthTokenVector";//No I18N
//    public static final String PASSCODE_VECTOR = "PassCodeVector";//No I18N
//    public static final String USERNAME_VECTOR = "UserNameVector";//No I18N

//    public static final String SECURITY_ANSWER_1 = "SecurityAnswer1"; //No I18N
//    public static final String SECURITY_ANSWER_2 = "SecurityAnswer2"; //No I18N
//    public static final String SECURITY_ANSWER_3 = "SecurityAnswer3"; //No I18N
//    public static final String SECURITY_ANSWER_4 = "SecurityAnswer4"; //No I18N
//    public static final String SECURITY_ANSWER_5 = "SecurityAnswer5"; //No I18N
//    public static final String SECURITY_ANSWER_VECTOR_1 = "SecurityAnswerVector1"; //No I18N
//    public static final String SECURITY_ANSWER_VECTOR_2 = "SecurityAnswerVector2"; //No I18N
//    public static final String SECURITY_ANSWER_VECTOR_3 = "SecurityAnswerVector3"; //No I18N
//    public static final String SECURITY_ANSWER_VECTOR_4 = "SecurityAnswerVector4"; //No I18N
//    public static final String SECURITY_ANSWER_VECTOR_5 = "SecurityAnswerVector5"; //No I18N

//    //bfr api M
//    private static void generatePrivateKeyEntry(Context context) throws Exception {
//        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
//        keyStore.load(null);
//        if (!keyStore.containsAlias(KEY_STORE_ALIAS_1)) {
//            // Generate a key pair for encryption
//            Calendar start = Calendar.getInstance();
//            Calendar end = Calendar.getInstance();
//            end.add(Calendar.YEAR, 30);
//            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
//                    .setAlias(KEY_STORE_ALIAS_1)
//                    .setSubject(new X500Principal("CN=" + KEY_STORE_ALIAS_1)) //No I18N
//                    .setSerialNumber(BigInteger.TEN)
//                    .setStartDate(start.getTime())
//                    .setEndDate(end.getTime())
//                    .build();
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
//            kpg.initialize(spec);
//            kpg.generateKeyPair();
//        }
//    }


//    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding"; //No I18N

//    private static byte[] rsaEncrypt(byte[] secret, Context context) throws Exception {
//        generatePrivateKeyEntry(context);
//        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
//        keyStore.load(null);
//
//        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_STORE_ALIAS_1, null);
//        // Encrypt the text
//        Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
//        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
//        cipherOutputStream.write(secret);
//        cipherOutputStream.close();
//
//        byte[] vals = outputStream.toByteArray();
//        return vals;
//    }

//    private static byte[] rsaDecrypt(byte[] encrypted) throws Exception {
//        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
//        keyStore.load(null);
//
//        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_STORE_ALIAS_1, null);
//        Cipher output = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
//        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
//        CipherInputStream cipherInputStream = new CipherInputStream(
//                new ByteArrayInputStream(encrypted), output);
//        ArrayList<Byte> values = new ArrayList<>();
//        int nextByte;
//        while ((nextByte = cipherInputStream.read()) != -1) {
//            values.add((byte) nextByte);
//        }
//
//        byte[] bytes = new byte[values.size()];
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = values.get(i).byteValue();
//        }
//        return bytes;
//    }


//    private static void generateEncryptedKey(Context context) throws Exception {
//        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        String enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
//        if (enryptedKeyB64 == null) {
//            byte[] key = new byte[16];
//            SecureRandom secureRandom = new SecureRandom();
//            secureRandom.nextBytes(key);
//            byte[] encryptedKey = rsaEncrypt(key, context);
//            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
//            SharedPreferences.Editor edit = pref.edit();
//            edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
//            edit.commit();
//        }
//    }


//    private static final String AES_MODE = "AES/ECB/PKCS7Padding"; //No I18N

//    private static Key getSecretKey(Context context) throws Exception {
//        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        String enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
//        if (enryptedKeyB64 == null) {
//            generateEncryptedKey(context);
//        }
//        enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
//        byte[] encryptedKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
//        byte[] key = rsaDecrypt(encryptedKey);
//        return new SecretKeySpec(key, "AES");  //No I18N
//    }

    //

    //aftr api M


    private static void generateKey() throws Exception {

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_STORE_ALIAS_2)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_STORE_ALIAS_2,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build());

            keyGenerator.generateKey();
        }

    }

    //
    private static byte[] getByteArrayFromString(String encryptedStr) {
        String[] encDataArr = encryptedStr.split(", ");
        int encDataArrLength = encDataArr.length;
        byte[] encbyteArr = new byte[encDataArr.length];
        for (int i = 0; i < encDataArrLength; i++) {
            String str = encDataArr[i];
            if (str.contains("[")) {
                str = str.replace("[", "");
            } else if (str.contains("]")) {
                str = str.replace("]", "");
            }
            encbyteArr[i] = Byte.parseByte(str);
        }
        return encbyteArr;
    }


    private static String getDecodedString(String encryptedStr, Context context, String type) throws Exception {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        byte[] encbyteArr = null;
        if (encryptedStr != null) {
            encbyteArr = getByteArrayFromString(encryptedStr);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        if (encbyteArr != null) {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(KEY_STORE_ALIAS_2, null);
            Key secretKey2 = secretKeyEntry.getSecretKey();
            Cipher cipher2 = Cipher.getInstance("AES/GCM/NoPadding");
            String ivStr = null;

            switch (type) {
//                case PASSCODE:
//                    ivStr = prefs.getString(PASSCODE_VECTOR, null);
//                    break;
                case IAM_AUTHTOKEN:
                    ivStr = prefs.getString(AUTHTOKEN_VECTOR, null);
                    break;
//                    case USERNAME:
//                        ivStr = prefs.getString(USERNAME_VECTOR, null);
//                        break;
//                    case SECURITY_ANSWER_VECTOR_1:
//                        ivStr = prefs.getString(SECURITY_ANSWER_VECTOR_1, null);
//                        break;
//                    case SECURITY_ANSWER_VECTOR_2:
//                        ivStr = prefs.getString(SECURITY_ANSWER_VECTOR_2, null);
//                        break;
//                    case SECURITY_ANSWER_VECTOR_3:
//                        ivStr = prefs.getString(SECURITY_ANSWER_VECTOR_3, null);
//                        break;
//                    case SECURITY_ANSWER_VECTOR_4:
//                        ivStr = prefs.getString(SECURITY_ANSWER_VECTOR_4, null);
//                        break;
//                    case SECURITY_ANSWER_VECTOR_5:
//                        ivStr = prefs.getString(SECURITY_ANSWER_VECTOR_5, null);
//                        break;
            }


            if (ivStr != null) {

                byte[] ivbyteArray = getByteArrayFromString(ivStr);
                cipher2.init(Cipher.DECRYPT_MODE, secretKey2, new GCMParameterSpec(128, ivbyteArray));
                byte[] decodedBytes = cipher2.doFinal(encbyteArr);
                byte[] tt = cipher2.doFinal(encbyteArr);
                return new String(decodedBytes);
            }

        }
//        } else {
//            if (encbyteArr != null) {
//                Cipher c = Cipher.getInstance(AES_MODE, "BC");
//                c.init(Cipher.DECRYPT_MODE, getSecretKey(context));
//                byte[] decodedBytes = c.doFinal(encbyteArr);
//                return new String(decodedBytes);
//            }
//        }
        return "";
    }


    //region authtoken


    public static void saveAuthToken(Context context, String input) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        generateKey();
        Key secretKey = keyStore.getKey(KEY_STORE_ALIAS_2, null);

//            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
//                    .getEntry(KEY_STORE_ALIAS_2, null);
//            Key secretKey2 = secretKeyEntry.getSecretKey();

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] ivArr = c.getIV();
        byte[] encryptedArr = c.doFinal(input.getBytes());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IAM_AUTHTOKEN, Arrays.toString(encryptedArr));
        editor.putString(AUTHTOKEN_VECTOR, Arrays.toString(ivArr));
        editor.commit();


        String strii = prefs.getString(IAM_AUTHTOKEN, null);
        String tt = strii;


//        } else {
//            Cipher c = Cipher.getInstance(AES_MODE, "BC");
//            c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));
//            byte[] encodedBytes = c.doFinal(input.getBytes());
//
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(IAM_AUTHTOKEN, Arrays.toString(encodedBytes));
//            editor.commit();
//
////            String strii = prefs.getString(IAM_AUTHTOKEN, "");
////            String tt = strii;
//        }
    }

/*
    public static void saveUserName(Context context, String input) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            generateKey();
            Key secretKey = keyStore.getKey(KEY_STORE_ALIAS_2, null);

//            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
//                    .getEntry(KEY_STORE_ALIAS_2, null);
//            Key secretKey2 = secretKeyEntry.getSecretKey();

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] ivArr = c.getIV();
            byte[] encryptedArr = c.doFinal(input.getBytes());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(USERNAME, Arrays.toString(encryptedArr));
            editor.putString(USERNAME_VECTOR, Arrays.toString(ivArr));
            editor.commit();


            String strii = prefs.getString(USERNAME, null);
            String tt = strii;


        } else {
            Cipher c = Cipher.getInstance(AES_MODE, "BC");
            c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));
            byte[] encodedBytes = c.doFinal(input.getBytes());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(USERNAME, Arrays.toString(encodedBytes));
            editor.commit();

//            String strii = prefs.getString(IAM_AUTHTOKEN, "");
//            String tt = strii;
        }
    }
*/

    public static String getAuthToken(Context context) throws Exception {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String encryptedStr = prefs.getString(IAM_AUTHTOKEN, null);
        return getDecodedString(encryptedStr, context, IAM_AUTHTOKEN);
    }

//    public static String getUserName(Context context) throws Exception {
//        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String encryptedStr = prefs.getString(USERNAME, null);
//        return getDecodedString(encryptedStr, context, USERNAME);
//    }

    //endregion


    //region passcode

 /*   public static Boolean verifyPassCode(Context context, String enteredPassCode) throws Exception {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String encryptedStr = prefs.getString(PASSCODE, null);
        return enteredPassCode.equals(getDecodedString(encryptedStr, context, PASSCODE));
    }*/

//    public static void savePassCode(Context context, String input) throws Exception {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor prefEditor = prefs.edit();
        //        prefEditor.remove(PASSCODE).remove(PASSCODE_VECTOR);
        //        prefEditor.commit();
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
//            keyStore.load(null);
//            generateKey();
//            Key secretKey = keyStore.getKey(KEY_STORE_ALIAS_2, null);
//
//            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
//            c.init(Cipher.ENCRYPT_MODE, secretKey);
//            byte[] ivArr = c.getIV();
//            byte[] encryptedArr = c.doFinal(input.getBytes());
//
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(PASSCODE, Arrays.toString(encryptedArr));
//            editor.putString(PASSCODE_VECTOR, Arrays.toString(ivArr));
//            editor.commit();
//
//
////        } else {
////
////            Cipher c = Cipher.getInstance(AES_MODE, "BC");
////            c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));
////            byte[] encodedBytes = c.doFinal(input.getBytes());
////
////            SharedPreferences.Editor editor = prefs.edit();
////            editor.putString(PASSCODE, Arrays.toString(encodedBytes));
////            editor.commit();
////
////        }
//
//    }

    //endregion

    /*
    //region security answer

    public static void saveSecurityAnsArray(Context context, String[] answerArray) throws Exception {

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        //Array to store encrypted answers
        String[] encryptedStringArr = new String[5];
        String[] vectorArr = new String[5];


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //For API greater than or equal to marshmallow(23)
            for (int i = 0; i < answerArray.length; i++) {

                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                generateKey();
                Key secretKey = keyStore.getKey(KEY_STORE_ALIAS_2, null);
                Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
                c.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] ivArr = c.getIV();
                vectorArr[i] = Arrays.toString(ivArr);
                byte[] encryptedByteArr = c.doFinal(answerArray[i].getBytes());
                encryptedStringArr[i] = Arrays.toString(encryptedByteArr);

            }

            SharedPreferences.Editor prefEditor = pref.edit();
            //Removing old answers and vectors
            prefEditor.remove(SECURITY_ANSWER_1);
            prefEditor.remove(SECURITY_ANSWER_2);
            prefEditor.remove(SECURITY_ANSWER_3);
            prefEditor.remove(SECURITY_ANSWER_4);
            prefEditor.remove(SECURITY_ANSWER_5);
            prefEditor.remove(SECURITY_ANSWER_VECTOR_1);
            prefEditor.remove(SECURITY_ANSWER_VECTOR_2);
            prefEditor.remove(SECURITY_ANSWER_VECTOR_3);
            prefEditor.remove(SECURITY_ANSWER_VECTOR_4);
            prefEditor.remove(SECURITY_ANSWER_VECTOR_5);
            prefEditor.commit();

            //saving encrypted answers in shared pref
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(SECURITY_ANSWER_1, encryptedStringArr[0]);
            editor.putString(SECURITY_ANSWER_2, encryptedStringArr[1]);
            editor.putString(SECURITY_ANSWER_3, encryptedStringArr[2]);
            editor.putString(SECURITY_ANSWER_4, encryptedStringArr[3]);
            editor.putString(SECURITY_ANSWER_5, encryptedStringArr[4]);

            editor.putString(SECURITY_ANSWER_VECTOR_1, vectorArr[0]);
            editor.putString(SECURITY_ANSWER_VECTOR_2, vectorArr[1]);
            editor.putString(SECURITY_ANSWER_VECTOR_3, vectorArr[2]);
            editor.putString(SECURITY_ANSWER_VECTOR_4, vectorArr[3]);
            editor.putString(SECURITY_ANSWER_VECTOR_5, vectorArr[4]);
            editor.commit();

        } else {
            //Api less than Marshmallow(23)
            for (int i = 0; i < answerArray.length; i++) {
                Cipher c = Cipher.getInstance(AES_MODE, "BC");
                c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));

                byte[] encryptedByteArr = c.doFinal(answerArray[i].getBytes());
                encryptedStringArr[i] = Arrays.toString(encryptedByteArr);
            }
            //saving encrypted answers in shared pref
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(SECURITY_ANSWER_1, encryptedStringArr[0]);
            editor.putString(SECURITY_ANSWER_2, encryptedStringArr[1]);
            editor.putString(SECURITY_ANSWER_3, encryptedStringArr[2]);
            editor.putString(SECURITY_ANSWER_4, encryptedStringArr[3]);
            editor.putString(SECURITY_ANSWER_5, encryptedStringArr[4]);
            editor.commit();

        }


    }

    public static Boolean verifySecurityAnswer(Context context, Integer[] questionNoArr, String[] enteredAnswerArr) throws Exception {
        //
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean[] resultArr = new Boolean[questionNoArr.length];
        Boolean correctAnswer = true;

        for (int i = 0; i < questionNoArr.length; i++) {

            if (questionNoArr[i] == 1) {
                String encryptedAnswer = prefs.getString(SECURITY_ANSWER_1, null);
                resultArr[i] = enteredAnswerArr[i].equalsIgnoreCase(getDecodedString(encryptedAnswer, context, SECURITY_ANSWER_VECTOR_1));

            } else if (questionNoArr[i] == 2) {
                String encryptedAnswer = prefs.getString(SECURITY_ANSWER_2, null);
                resultArr[i] = enteredAnswerArr[i].equalsIgnoreCase(getDecodedString(encryptedAnswer, context, SECURITY_ANSWER_VECTOR_2));

            } else if (questionNoArr[i] == 3) {
                String encryptedAnswer = prefs.getString(SECURITY_ANSWER_3, null);
                resultArr[i] = enteredAnswerArr[i].equalsIgnoreCase(getDecodedString(encryptedAnswer, context, SECURITY_ANSWER_VECTOR_3));

            } else if (questionNoArr[i] == 4) {
                String encryptedAnswer = prefs.getString(SECURITY_ANSWER_4, null);
                resultArr[i] = enteredAnswerArr[i].equalsIgnoreCase(getDecodedString(encryptedAnswer, context, SECURITY_ANSWER_VECTOR_4));

            } else if (questionNoArr[i] == 5) {
                String encryptedAnswer = prefs.getString(SECURITY_ANSWER_5, null);
                resultArr[i] = enteredAnswerArr[i].equalsIgnoreCase(getDecodedString(encryptedAnswer, context, SECURITY_ANSWER_VECTOR_5));

            }

        }

        for (Boolean aResultArr : resultArr) {
            if (!aResultArr) {
                correctAnswer = false;
            }
        }

        return correctAnswer;
    }

    //endregion
*/


}
