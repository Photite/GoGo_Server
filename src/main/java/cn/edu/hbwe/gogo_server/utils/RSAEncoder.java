package cn.edu.hbwe.gogo_server.utils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

/**
 * @author Photite
 */
public class RSAEncoder {
    private static BigInteger n = null;
    private static BigInteger e = null;

    public static String encrypt(String pwd, String nStr, String eStr) {
        n = new BigInteger(nStr, 16);
        e = new BigInteger(eStr, 16);

        BigInteger r = doPublic(Objects.requireNonNull(pkcs1pad2(pwd, (n.bitLength() + 7) >> 3)));
        String sp = r.toString(16);
        if ((sp.length() & 1) != 0) {
            sp = "0" + sp;
        }
        return sp;
    }

    private static BigInteger doPublic(BigInteger x) {
        return x.modPow(e, n);
    }

    private static BigInteger pkcs1pad2(String s, int n) {
        // TODO: fix for utf-8
        if (n < s.length() + 11) {
            System.err.println("Message too long for RSAEncoder");
            return null;
        }
        byte[] ba = new byte[n];
        int i = s.length() - 1;
        while (i >= 0 && n > 0) {
            int c = s.codePointAt(i--);
            // encode using utf-8
            if (c < 128) {
                ba[--n] = (byte) c;
            } else if (c < 2048) {
                ba[--n] = (byte) ((c & 63) | 128);
                ba[--n] = (byte) ((c >> 6) | 192);
            } else {
                ba[--n] = (byte) ((c & 63) | 128);
                ba[--n] = (byte) (((c >> 6) & 63) | 128);
                ba[--n] = (byte) ((c >> 12) | 224);
            }
        }
        ba[--n] = 0;

        byte[] temp = new byte[1];
        Random rdm = new Random(47L);

        // random non-zero pad
        while (n > 2) {
            temp[0] = 0;
            while (temp[0] == 0) {
                rdm.nextBytes(temp);
            }
            ba[--n] = temp[0];
        }
        ba[--n] = 2;
        ba[--n] = 0;
        return new BigInteger(ba);
    }
}
