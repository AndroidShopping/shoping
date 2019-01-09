package com.shop.shopping.present;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.net.Api;
import com.shop.shopping.ui.MainActivity;

public class PShopPresenter extends XPresent<MainActivity> {


    public void getShopProductList() {
        getV().onStartLoading();
        Api.getShopService().getShopItemList()
                .compose(XApi.<ShopResult>getApiTransformer())
                .compose(XApi.<ShopResult>getScheduler())
                .compose(getV().<ShopResult>bindToLifecycle())
                .subscribe(new ApiSubscriber<ShopResult>() {
                    @Override
                    protected void onFail(NetError error) {
                        getV().showError(error);
                    }

                    @Override
                    public void onNext(ShopResult shopResult) {
                        getV().showData(shopResult);
                    }
                });
    }
}
