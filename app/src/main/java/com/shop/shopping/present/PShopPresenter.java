package com.shop.shopping.present;

import android.util.Log;

import com.shop.shopping.model.ShopCategory;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.net.Api;
import com.shop.shopping.ui.MainActivity;

import java.util.List;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;

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
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("PShopPresenter", "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(ShopResult shopResult) {
                        List<ShopCategory> data = shopResult.getData();
                        if (data != null) {
                            for (ShopCategory datum : data) {
                                List<ShopItem> shopItem = datum.getShopItem();
                                if (shopItem != null) {
                                    for (ShopItem item : shopItem) {
                                        item.setNumber(0);
                                    }
                                }
                            }
                        }
                        getV().showData(shopResult);
                    }
                });
    }
}
