package com.shop.shopping.utils;

import android.util.Log;

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

    public static boolean isReceiveMoney(byte[] data) {
        Log.d("ZhiBiQiUtils", "isReceiveMoney: data[0]=" + data[0]);
        Log.d("ZhiBiQiUtils", "isReceiveMoney:0x40 = " + 0x40);
        Log.d("ZhiBiQiUtils", "isReceiveMoney:0x46 = " + 0x46);
        return (data[0] >= 0x40) & (data[0] <= 0x46);
    }
}
