package com.example.mha;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class secretKeyManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "NHSkey";

    public static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        // If the key already exists, return it
        if (keyStore.containsAlias(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            return entry.getSecretKey();
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build();

        keyGenerator.init(keySpec);
        return keyGenerator.generateKey();
    }

}
