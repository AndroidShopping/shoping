package com.shop.shopping.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory implements Parcelable {
    String id;
    String name;
    String description;
    String picPath;
    List<ShopItem> shopItem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public List<ShopItem> getShopItem() {
        return shopItem;
    }

    public void setShopItem(List<ShopItem> shopItem) {
        this.shopItem = shopItem;
    }

    public int getBuyGoodCount() {
        int count = 0;
        for (ShopItem item : shopItem) {
            count += item.getBuyCount();
        }
        return count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.picPath);
        dest.writeList(this.shopItem);
    }

    public ShopCategory() {
    }

    protected ShopCategory(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.picPath = in.readString();
        this.shopItem = new ArrayList<ShopItem>();
        in.readList(this.shopItem, ShopItem.class.getClassLoader());
    }

    public static final Parcelable.Creator<ShopCategory> CREATOR = new Parcelable.Creator<ShopCategory>() {
        @Override
        public ShopCategory createFromParcel(Parcel source) {
            return new ShopCategory(source);
        }

        @Override
        public ShopCategory[] newArray(int size) {
            return new ShopCategory[size];
        }
    };
}
