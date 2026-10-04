package com.movieflix.engine;
import java.util.Random;

public class UniversalHashing {
    private long a, b, p, m;

    public UniversalHashing(long universeSize, long tableSize) {
        this.p = 2147483647; // Large prime
        this.m = tableSize;
        Random rand = new Random();
        this.a = rand.nextLong() % (p - 1) + 1;
        this.b = rand.nextLong() % p;
    }

    public int hash(long x) {
        return (int) (((a * x + b) % p) % m);
    }
}