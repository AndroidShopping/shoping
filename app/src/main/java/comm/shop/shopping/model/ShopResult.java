package comm.shop.shopping.model;

import java.util.List;

/**
 * Created by wanglei on 2016/12/10.
 */

public class ShopResult extends BaseModel {

    List<ShopCategory> data;

    public int getAllBuyedGoodCount() {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (ShopCategory datum : data) {
            count += datum.getBuyGoodCount();
        }
        return count;
    }

    public List<ShopCategory> getData() {
        return data;
    }

    public void setData(List<ShopCategory> data) {
        this.data = data;
    }

    public int getAllSelectCount() {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (ShopCategory datum : data) {
            List<ShopItem> shopItem = datum.getShopItem();
            for (ShopItem item : shopItem) {
                count += item.buyCount;
            }
        }
        return count;
    }

    public int getAllSelectPrice() {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (ShopCategory datum : data) {
            List<ShopItem> shopItem = datum.getShopItem();
            for (ShopItem item : shopItem) {
                count += item.getAllBuyPrice();
            }
        }
        return count;
    }

    public void clearAllSelectItem() {
        if (data == null) {
            return;
        }
        for (ShopCategory datum : data) {
            for (ShopItem shopItem : datum.getShopItem()) {
                shopItem.setBuyCount(0);
            }

        }
    }

    /**
     * 获取选择的商品种类数
     *
     * @return
     */
    public int getBuyedKindOfGoodCount() {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (ShopCategory datum : data) {
            List<ShopItem> shopItem = datum.getShopItem();
            for (ShopItem item : shopItem) {
                if (item.getBuyCount() != 0) {
                    count += 1;
                }
            }
        }
        return count;

    }
}
