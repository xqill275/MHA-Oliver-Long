package com.example.mha;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import com.example.mha.TestHelperClasses.NhsUtils;

public class NhsUtilsTest {

    @Test
    public void getDigits_correctConversion() {
        assertEquals(
                Arrays.asList(1,2,3,4,5),
                NhsUtils.getDigits("12345")
        );
    }

    @Test
    public void getDigits_handlesZeros() {
        assertEquals(
                Arrays.asList(0,0,8,7,0,0,3,3,8),
                NhsUtils.getDigits("008700338")
        );
    }

    // Known valid NHS numbers
    @Test
    public void verifyNhsNum_validExamples_returnTrue() {
        assertTrue(NhsUtils.verifyNhsNum("9434765919")); // official example
        assertTrue(NhsUtils.verifyNhsNum("0008700338")); // your example
    }

    @Test
    public void verifyNhsNum_wrongCheckDigit_returnFalse() {
        assertFalse(NhsUtils.verifyNhsNum("9434765918")); // last digit changed
    }

    @Test
    public void verifyNhsNum_nonNumeric_returnFalse() {
        assertFalse(NhsUtils.verifyNhsNum("ABC4765919"));
        assertFalse(NhsUtils.verifyNhsNum("94347X5919"));
    }

    @Test
    public void verifyNhsNum_tooShort_returnFalse() {
        assertFalse(NhsUtils.verifyNhsNum("12345"));
    }

    @Test
    public void verifyNhsNum_tooLong_returnFalse() {
        assertFalse(NhsUtils.verifyNhsNum("1234567890123"));
    }

    @Test
    public void verifyNhsNum_invalidCheckDigit10_returnFalse() {
        // Construct a number where expected digit is 10
        // Example: 111111111? leads to check digit = 10 (invalid)
        assertFalse(NhsUtils.verifyNhsNum("1111111110"));
    }
}
