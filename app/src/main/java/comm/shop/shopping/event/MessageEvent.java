package comm.shop.shopping.event;

import comm.shop.shopping.model.ShopResult;

public class MessageEvent {
    public int num;
    public int price;
    public ShopResult result;

    public MessageEvent(int totalNum, int price, ShopResult result) {
        this.num = totalNum;
        this.price = price;
        this.result = result;
    }
}
