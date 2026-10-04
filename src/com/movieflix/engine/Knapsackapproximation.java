package com.movieflix.engine;
import java.util.*;

public class KnapsackApproximation {
    public static class Item {
        public double weight, value;
        public Item(double w, double v) { this.weight = w; this.value = v; }
    }

    public static double greedyApproximation(List<Item> items, double capacity) {
        items.sort((a, b) -> Double.compare(b.value / b.weight, a.value / a.weight));
        double totalValue = 0, currentWeight = 0;
        for (Item item : items) {
            if (currentWeight + item.weight <= capacity) {
                currentWeight += item.weight;
                totalValue += item.value;
            }
        }
        return totalValue;
    }
}