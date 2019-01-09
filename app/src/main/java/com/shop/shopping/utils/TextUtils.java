package com.shop.shopping.utils;

import android.support.annotation.StringRes;

import cn.droidlover.xdroidmvp.shopping.R;
import com.shop.shopping.App;

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
}
