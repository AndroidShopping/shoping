// PayService.aidl
package com.shop.shopping;
import com.shop.shopping.PayCallback;

// Declare any non-default types here with import statements

interface PayService {
   void startPay(long moneyCount,long timeout,PayCallback callback);
   int  queryDeviceState();
}
