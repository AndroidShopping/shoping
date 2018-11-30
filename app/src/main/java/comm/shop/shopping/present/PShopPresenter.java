package comm.shop.shopping.present;

import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.net.Api;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;
import comm.shop.shopping.ui.fragment.GoodsFragment;

/**
 * Created by wanglei on 2016/12/31.
 */

public class PShopPresenter extends XPresent<GoodsFragment> {


    public void getShopProductList() {
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
                        getV().showData( shopResult);
                    }
                });
    }
}
