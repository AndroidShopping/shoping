package comm.shop.shopping.event;

import comm.shop.shopping.model.ShopResult;

public class MessageEvent {
    public ShopResult result;

    public MessageEvent( ShopResult result) {
        this.result = result;
    }
}
