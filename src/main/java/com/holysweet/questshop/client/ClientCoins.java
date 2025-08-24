package com.holysweet.questshop.client;

public final class ClientCoins {
    private static int BALANCE = 0;

    private ClientCoins() {}

    public static void set(int value) { BALANCE = Math.max(0, value); }

    public static int get() { return BALANCE; }
}
