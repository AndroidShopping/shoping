package com.shop.shopping.net;

import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.OrderResult;
import com.shop.shopping.model.ShopResult;

import io.reactivex.Flowable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by wanglei on 2016/12/31.
 */

public interface ShopService {

    @GET("/shop/getShopCategory")
    Flowable<ShopResult> getShopItemList();


    @FormUrlEncoded
    @POST("/shop/addShopOrder")
    Flowable<OrderResult> createPreviewOrder(@Field(value = "total") int total,
                                             @Field(value = "order") String items);

    @FormUrlEncoded
    @POST("/shop/updateShopOrder")
    Flowable<ConfirmOrderResult> confirmOrder(@Field(value = "id") String id);
}
