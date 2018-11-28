package comm.shop.shopping.present;

import com.google.gson.Gson;

import java.util.List;

import cn.droidlover.xdroidmvp.mvp.XPresent;
import cn.droidlover.xdroidmvp.net.ApiSubscriber;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.XApi;
import comm.shop.shopping.model.OrderResult;
import comm.shop.shopping.model.ShopItem;
import comm.shop.shopping.net.Api;
import comm.shop.shopping.ui.SubmitActivity;

public class SubmitPresenter extends XPresent<SubmitActivity> {


    public void createPrevieOrder(int total,
                                  List<ShopItem> items) {
        Gson gson = new Gson();
        Api.getShopService().createPreviewOrder(total, gson.toJson(items))
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
