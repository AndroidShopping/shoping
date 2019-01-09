package com.shop.shopping.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanglei on 2016/12/10.
 */

public class ShopResult extends BaseModel implements Parcelable {

    public List<ShopItem> getSelectedItem() {
        List<ShopItem> items = new ArrayList<>();
        List<ShopCategory> data = getData();
        for (ShopCategory datum : data) {
            List<ShopItem> shopItem = datum.getShopItem();
            for (ShopItem item : shopItem) {
                int buyCount = item.getBuyCount();
                if (buyCount != 0) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    public ShopItem getItem(int position) {
        List<ShopCategory> data = getData();
        int count = 0;
        for (ShopCategory datum : data) {
            for (ShopItem shopItem : datum.getShopItem()) {
                int buyCount = shopItem.getBuyCount();
                if (buyCount != 0) {
                    if (count == position) {
                        return shopItem;
                    } else {
                        count++;
                    }
                }
            }
        }
        return null;
    }

    public static final int TAG = 233;
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
                count += item.getNumber();
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
     * 获取当前选中的商品条目数
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

    @Override
    public int getTag() {
        return TAG;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.data);
        dest.writeInt(this.status);
        dest.writeString(this.message);
    }

    public ShopResult() {
    }

    protected ShopResult(Parcel in) {
        this.data = new ArrayList<ShopCategory>();
        in.readList(this.data, ShopCategory.class.getClassLoader());
        this.status = in.readInt();
        this.message = in.readString();
    }

    public static final Parcelable.Creator<ShopResult> CREATOR = new Parcelable.Creator<ShopResult>() {
        @Override
        public ShopResult createFromParcel(Parcel source) {
            return new ShopResult(source);
        }

        @Override
        public ShopResult[] newArray(int size) {
            return new ShopResult[size];
        }
    };

    public String findCategoryName(ShopItem item) {
        List<ShopCategory> data = getData();
        for (ShopCategory datum : data) {
            List<ShopItem> shopItem = datum.getShopItem();
            for (ShopItem shopItem1 : shopItem) {
                if (shopItem1 == item) {
                    return datum.getName();
                }
            }
        }
        return "";
    }
}
