package com.movieflix.engine;

public class MillerRabin {
    private static long power(long base, long exp, long mod) {
        long res = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1) res = (res * base) % mod;
            base = (base * base) % mod;
            exp >>= 1;
        }
        return res;
    }

    public static boolean isPrime(long n, int k) {
        if (n <= 1 || n == 4) return false;
        if (n <= 3) return true;
        long d = n - 1;
        while (d % 2 == 0) d /= 2;
        for (int i = 0; i < k; i++) {
            long a = 2 + (long)(Math.random() % (n - 4));
            long temp = d;
            long mod = power(a, temp, n);
            while (temp != n - 1 && mod != 1 && mod != n - 1) {
                mod = (mod * mod) % n;
                temp *= 2;
            }
            if (mod != n - 1 && temp % 2 == 0) return false;
        }
        return true;
    }
}