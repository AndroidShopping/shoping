// PayCallback.aidl
package com.shop.shopping;

// Declare any non-default types here with import statements

interface PayCallback {
  void onPayStart();
  void onPayTimeout();
  void onDeviceError(int deviceType,int errorCode,String errorDescription);
  void onPaySuccess();
}
