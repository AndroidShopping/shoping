package android_serialport_api;

public interface SerialPortDataCallBack {
    //接收数据回调
    void onDataReceived(byte[] buffer, int size);
    //串口初始化完成回调
    void onSerialPortInitFinish(boolean result);
}
