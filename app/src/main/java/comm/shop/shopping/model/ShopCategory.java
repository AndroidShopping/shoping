package comm.shop.shopping.model;

import java.util.List;

public class ShopCategory {
    String id;
    String name;
    String description;
    String picPath;
    List<ShopItem> shopItem;
    int bugNum;

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

    public int getBugNum() {
        return bugNum;
    }

    public void setBugNum(int bugNum) {
        this.bugNum = bugNum;
    }
}
