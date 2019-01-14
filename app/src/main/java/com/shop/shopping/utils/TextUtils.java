package com.shop.shopping.utils;

import android.support.annotation.StringRes;

import com.shop.shopping.App;

import cn.droidlover.xdroidmvp.shopping.R;

public final class TextUtils {
    public static String getString(@StringRes int id) {
        return App.getContext().getString(id);
    }

    public static boolean isEmpty(String s) {
        if (s == null || s.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getPriceText(int price) {
        return getString(R.string.mark) + price;
    }

    public static String printHexString(byte[] b) {
        String a = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            a = a + hex;
        }

        return a;
    }
}
