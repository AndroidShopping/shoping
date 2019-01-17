package com.shop.shopping.utils;

import android.support.annotation.StringRes;

import com.shop.shopping.App;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import cn.droidlover.xdroidmvp.shopping.R;

public final class TextUtils {

    final static DecimalFormat NF_YUAN = new DecimalFormat("#####################0.00");
    final static DecimalFormat NF_FEN = new DecimalFormat("#####################0");

    public static String getString(@StringRes int id) {
        return App.getContext().getString(id);
    }

    public static BigDecimal moneyFen2Yuan(String fen) {
        return formatYuan(new BigDecimal(fen).divide(new BigDecimal("100")));
    }

    public static BigDecimal moneyYuan2Fen(BigDecimal yuan) {
        return yuan == null ? null : formatFen(yuan.multiply(new BigDecimal("100")));
    }

    public static String moneyFen2YuanStr(BigDecimal fen) {
        return fen == null ? null : NF_YUAN.format(moneyFen2Yuan(fen.toString()));
    }

    public static String moneyYuan2FenStr(BigDecimal yuan) {
        return yuan == null ? null : NF_FEN.format(yuan.multiply(new BigDecimal("100")));
    }

    public static BigDecimal formatYuan(BigDecimal yuan) {
        return yuan == null ? null : new BigDecimal(NF_YUAN.format(yuan));
    }

    public static BigDecimal formatFen(BigDecimal fen) {
        return fen == null ? null : new BigDecimal(NF_FEN.format(fen));
    }

    public static boolean isEmpty(String s) {
        if (s == null || s.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getPriceText(int price) {

        return getString(R.string.mark) + moneyFen2Yuan("" + price).toString();
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
