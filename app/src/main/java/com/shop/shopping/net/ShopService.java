package com.shop.shopping.net;

import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.OrderResult;
import com.shop.shopping.model.ShopResult;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by wanglei on 2016/12/31.
 */

public interface ShopService {

    @GET("/shop/getShopCategory")
    Flowable<ShopResult> getShopItemList();


    @POST("/shop/addShopOrder")
    Flowable<OrderResult> createPreviewOrder(@Query(value = "total") int total,
                                             @Query(value = "order") String items);

    @POST("/shop/updateShopOrder")
    Flowable<ConfirmOrderResult> confirmOrder(@Query(value = "id") String id, @Query(value = "orderStatus") int state);
}
