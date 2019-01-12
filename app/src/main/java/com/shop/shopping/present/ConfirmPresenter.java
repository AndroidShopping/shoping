package com.shop.shopping.present;

import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.ShopCategory;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.net.Api;
import com.shop.shopping.ui.SubmitCoinActivity;

import java.util.List;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;


public class ConfirmPresenter extends XPresent<SubmitCoinActivity> {
    public void getShopProductList(String orderId) {
        getV().onStartLoading();
        Api.getShopService().confirmOrder(orderId)
                .compose(XApi.<ConfirmOrderResult>getApiTransformer())
                .compose(XApi.<ConfirmOrderResult>getScheduler())
                .compose(getV().<ConfirmOrderResult>bindToLifecycle())
                .subscribe(new ApiSubscriber<ConfirmOrderResult>() {
                    @Override
                    protected void onFail(NetError error) {
                        getV().showError(error);
                    }

                    @Override
                    public void onNext(ConfirmOrderResult confirmOrderResult) {
                        getV().showData(confirmOrderResult);
                    }
                });
    }
}
