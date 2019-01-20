package com.shop.shopping.net;

import cn.droidlover.xdroidmvp.cache.SharedPref;
import cn.droidlover.xdroidmvp.shopping.BuildConfig;
import com.shop.shopping.App;
import com.shop.shopping.model.Keys;
import com.shop.shopping.utils.PatternUtils;

public class URLUtils {
    public static final String findBaseUrl() {
        SharedPref instance = SharedPref.getInstance(App.getContext());
        String ip = instance.getString(Keys.IP, "192.168.1.157");
        String port = instance.getString(Keys.PORT, "9999");
        boolean ip1 = PatternUtils.isIP(ip);
        boolean port1 = PatternUtils.isPort(port);
        if (ip1 && port1) {
            return "http://" + ip + ":" + port + "/";
        }
        return BuildConfig.URL;

    }

}
