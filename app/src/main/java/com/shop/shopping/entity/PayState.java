package com.shop.shopping.entity;

public interface PayState {
    int START_PAY = 1;
    int PAY_ERROR = 2;
    int PAY_OK = 3;
    int PAY_LAST_ERROR = 4;
}
