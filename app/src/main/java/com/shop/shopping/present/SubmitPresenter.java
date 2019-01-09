package com.shop.shopping.present;

import com.google.gson.Gson;

import java.util.List;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;
import com.shop.shopping.model.OrderResult;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.net.Api;
import com.shop.shopping.ui.SubmitActivity;

public class SubmitPresenter extends XPresent<SubmitActivity> {


    public void createPrevieOrder(int total,
                                  List<ShopItem> items) {

        Gson gson = new Gson();
        String s = gson.toJson(items);
        Api.getShopService().createPreviewOrder(total, s)
                .compose(XApi.<OrderResult>getApiTransformer())
                .compose(XApi.<OrderResult>getScheduler())
                .compose(getV().<OrderResult>bindToLifecycle())
                .subscribe(new ApiSubscriber<OrderResult>() {
                    @Override
                    protected void onFail(NetError error) {
                        getV().showError(error);
                    }

                    @Override
                    public void onNext(OrderResult shopResult) {
                        getV().showData(shopResult);
                    }
                });
    }
}
