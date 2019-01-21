package com.shop.shopping.utils;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import com.shop.shopping.App;

import java.text.DecimalFormat;

import cn.droidlover.xdroidmvp.shopping.R;

public final class TextUtils {

    final static DecimalFormat NF_YUAN = new DecimalFormat("#####################0.00");
    final static DecimalFormat NF_FEN = new DecimalFormat("#####################0");

    public static String getString(@StringRes int id) {
        return App.getContext().getString(id);
    }


    public static boolean isEmpty(String s) {
        if (s == null || s.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    public static String getPriceText(int price) {

        try {

            int yuan = price / 100;
            int fen = price - yuan * 100;
            return getString(R.string.mark) + String.format("%d.%02d", yuan, fen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

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
