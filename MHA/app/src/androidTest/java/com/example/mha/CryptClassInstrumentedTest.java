package com.example.mha;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CryptClassInstrumentedTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        String original = "Hello World";

        String encrypted = CryptClass.encrypt(original);
        assertNotNull(encrypted);

        String decrypted = CryptClass.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testDifferentCiphertext() throws Exception {
        String text = "Repeatable";

        String c1 = CryptClass.encrypt(text);
        String c2 = CryptClass.encrypt(text);

        assertNotEquals(c1, c2); // different due to random IV
    }

    @Test
    public void testInvalidBase64() {
        assertNull(CryptClass.decrypt("not-base64"));
    }
}