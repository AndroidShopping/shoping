package comm.shop.shopping.event;

import java.util.List;

import comm.shop.shopping.entity.GoodsListBean;

public class MessageEvent {
    public int  num;
    public int  price;
    public List<GoodsListBean.DataEntity.GoodscatrgoryEntity.GoodsitemEntity> goods;

    public MessageEvent(int totalNum, int price,List<GoodsListBean.DataEntity.GoodscatrgoryEntity.GoodsitemEntity> goods) {
        this.num = totalNum;
        this.price = price;
        this.goods = goods;
    }
}
