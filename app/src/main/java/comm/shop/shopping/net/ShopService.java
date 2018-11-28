package comm.shop.shopping.net;

import java.util.List;

import comm.shop.shopping.model.OrderResult;
import comm.shop.shopping.model.ShopItem;
import comm.shop.shopping.model.ShopResult;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by wanglei on 2016/12/31.
 */

public interface ShopService {

    @GET("/app/mock/data/695567")
    Flowable<ShopResult> getShopItemList();

    @GET("/app/mock/118728/shop/addShopOrder/")
    Flowable<OrderResult> createPreviewOrder(@Query(value = "total") int total,
                                             @Query(value = "order") String items);
}
