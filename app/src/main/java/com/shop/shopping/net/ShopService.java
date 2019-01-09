package com.shop.shopping.net;

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

    @GET("/app/mock/data/695567")
    Flowable<ShopResult> getShopItemList();


    @FormUrlEncoded
    @POST("/app/mock/118728/shop/addShopOrder/")
    Flowable<OrderResult> createPreviewOrder(@Field(value = "total") int total,
                                             @Field(value = "order") String items);
}