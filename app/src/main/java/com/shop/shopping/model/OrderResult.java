package com.shop.shopping.model;

public class OrderResult extends BaseModel {
    private OrderResultInfo data;

    public OrderResultInfo getData() {
        return data;
    }

    public void setData(OrderResultInfo data) {
        this.data = data;
    }
}
