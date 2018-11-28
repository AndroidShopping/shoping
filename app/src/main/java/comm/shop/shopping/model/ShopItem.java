package comm.shop.shopping.model;

import android.os.Parcel;
import android.os.Parcelable;

import comm.shop.shopping.utils.TextUtils;

public class ShopItem implements Parcelable {
    String id;
    int price;
    String name;
    String description;
    int number;
    String picPath;
    int isShelf;
    String unit;
    String standData;

    public String getStandData() {
        return "全脂，标准";
    }

    public void setStandData(String standData) {
        this.standData = standData;
    }

    public int getBuyCount() {
        return number;
    }

    public void setBuyCount(int buyCount) {
        this.number = buyCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getIsShelf() {
        return isShelf;
    }

    public void setIsShelf(int isShelf) {
        this.isShelf = isShelf;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getAllBuyPrice() {
        return price * getBuyCount();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.price);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeInt(this.number);
        dest.writeString(this.picPath);
        dest.writeInt(this.isShelf);
        dest.writeString(this.unit);
    }

    public ShopItem() {
    }

    protected ShopItem(Parcel in) {
        this.id = in.readString();
        this.price = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        this.number = in.readInt();
        this.picPath = in.readString();
        this.isShelf = in.readInt();
        this.unit = in.readString();
    }

    public static final Parcelable.Creator<ShopItem> CREATOR = new Parcelable.Creator<ShopItem>() {
        @Override
        public ShopItem createFromParcel(Parcel source) {
            return new ShopItem(source);
        }

        @Override
        public ShopItem[] newArray(int size) {
            return new ShopItem[size];
        }
    };

    public String formatAllCost() {
        int allCount = price * number;
        return TextUtils.getPriceText(allCount);
    }
}
