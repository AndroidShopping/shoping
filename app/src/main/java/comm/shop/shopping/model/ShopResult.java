package comm.shop.shopping.model;

import java.util.List;

/**
 * Created by wanglei on 2016/12/10.
 */

public class ShopResult extends BaseModel {

    List<ShopCategory> data;

    public List<ShopCategory> getData() {
        return data;
    }

    public void setData(List<ShopCategory> data) {
        this.data = data;
    }
}
