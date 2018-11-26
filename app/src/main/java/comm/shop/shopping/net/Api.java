package comm.shop.shopping.net;

import cn.droidlover.xdroidmvp.net.XApi;
import cn.droidlover.xdroidmvp.shopping.BuildConfig;

/**
 * Created by wanglei on 2016/12/31.
 */

public class Api {
    public static final String API_BASE_URL = BuildConfig.URL;

    private static ShopService shopService;

    public static ShopService getShopService() {
        if (shopService == null) {
            synchronized (Api.class) {
                if (shopService == null) {
                    shopService = XApi.getInstance().getRetrofit(API_BASE_URL, true).create(ShopService.class);
                }
            }
        }
        return shopService;
    }
}
