package comm.shop.shopping.net;

import cn.droidlover.xdroidmvp.net.XApi;

/**
 * Created by wanglei on 2016/12/31.
 */

public class Api {
    private static ShopService shopService;

    public static ShopService getShopService() {
        if (shopService == null) {
            synchronized (Api.class) {
                if (shopService == null) {
                    shopService = XApi.getInstance().getRetrofit(URLUtils.findBaseUrl(), true)
                            .create(ShopService.class);
                }
            }
        }
        return shopService;
    }
}
