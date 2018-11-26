package comm.shop.shopping.event;

import comm.shop.shopping.model.ShopResult;

/**
 * Created by dalong on 2016/12/27.
 */

public class GoodsListEvent {
    public ShopResult result;

    public GoodsListEvent(ShopResult result) {
        this.result = result;
    }
}
