package com.shop.shopping.utils;

public class YingBiQiUtils {

    public static int getMoney(byte[] data) {
        int bai = data[1] - 0x30;
        int shi = data[2] - 0x30;
        int ge = data[3] - 0x30;
        int moneyIndex = bai * 100 + shi * 10 + ge;
        if (moneyIndex == 0) {
            return 20;
        }
        if (moneyIndex == 1) {
            return 50;
        }
        if (moneyIndex == 2) {
            return 100;
        }
        if (moneyIndex == 3) {
            return 200;
        }
        return 0;


    }

    public static boolean isReceiveMoney(byte[] data) {
        if (data[0] != 0x44) {
            return false;
        }
        return getMoney(data) != 0;
    }
}
