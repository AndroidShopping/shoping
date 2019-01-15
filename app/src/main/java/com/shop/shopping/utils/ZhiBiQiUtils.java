package com.shop.shopping.utils;

public class ZhiBiQiUtils {

    public static int convertMoneyTOCent(byte[] data) {
        switch (data[0]) {
            case 0x40:
                return 5 * 100;
            case 0x41:
                return 10 * 100;
            case 0x42:
                return 20 * 100;
            case 0x43:
                return 50 * 100;
            case 0x44:
                return 100 * 100;
            case 0x45:
                return 200 * 100;
            case 0x46:
                return 500 * 100;
            default:
                return 0;
        }

    }

    public static boolean  isReceiveMoney(byte[] data) {
        return data[0] >= 40 & data[0] <= 46;
    }
}
