package comm.shop.shopping.utils;

import android.support.annotation.StringRes;

import comm.shop.shopping.App;

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
}
