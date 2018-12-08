package comm.shop.shopping.net;

import cn.droidlover.xdroidmvp.cache.SharedPref;
import cn.droidlover.xdroidmvp.shopping.BuildConfig;
import comm.shop.shopping.App;
import comm.shop.shopping.model.Keys;
import comm.shop.shopping.utils.PatternUtils;

public class URLUtils {
    public static final String findBaseUrl() {
        SharedPref instance = SharedPref.getInstance(App.getContext());
        String ip = instance.getString(Keys.IP, "");
        String port = instance.getString(Keys.PORT, "");
        boolean ip1 = PatternUtils.isIP(ip);
        boolean port1 = PatternUtils.isPort(port);
//        http://rap2api.taobao.org/
        if (ip1 && port1) {
            return "http://" + ip + ":" + port + "/";
        }
        return BuildConfig.URL;

    }

}
