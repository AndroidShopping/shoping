package comm.shop.shopping.net;

import comm.shop.shopping.model.ShopResult;
import io.reactivex.Flowable;
import retrofit2.http.GET;

/**
 * Created by wanglei on 2016/12/31.
 */

public interface ShopService {

    @GET("/app/mock/data/695567")
    Flowable<ShopResult> getShopItemList();
}
