package android_serialport_api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerialPortUtil {
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    //串口返回回调
    private SerialPortDataCallBack serialPortDataCallBack;
    //flag 结束串口发送线程
    private boolean sendFlag = true;
    //Dream音效命令List
    private List<byte[]> dreamProList = new ArrayList<>();
    //超时时间（超时检测线程是每次休眠固定时间，通过休眠次数来判断超时）
    private int timeOutCount = 10;
    //休眠次数
    private int sleepCount = 0;
    //是否返回标识
    private boolean isReturn = true;
    public static final int DEFAULT_BO_TE = 19200;

    public void initSerialPort(final String filePath, final int baudRate, final int flags) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSerialPort == null) {
                        mSerialPort = new SerialPort(new File(filePath), baudRate, flags);
                    }
                    mOutputStream = mSerialPort.getOutputStream();
                    mInputStream = mSerialPort.getInputStream();
                    //开启线程循环接收串口返回的指令
                    new ReadThread().start();
                    //开始发送串口指令线程以及超时判断线程
                    new SendOrderThread().start();
                    new TimeOutThread().start();
                    if (serialPortDataCallBack != null) {
                        //初始化完成，回调结果
                        serialPortDataCallBack.onSerialPortInitFinish(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (serialPortDataCallBack != null) {
                        //初始化完成，回调结果
                        serialPortDataCallBack.onSerialPortInitFinish(false);
                    }
                }
            }
        }).start();
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        //将sendFlag置为false
        sendFlag = false;
    }

    //发送串口命令
    private void sendOrder(byte[] bytes) {
        //发送byte[]指令
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(bytes);
            mOutputStream.write(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //发送最前面的指令
    private synchronized void sendFirst() {
        if (dreamProList.size() > 0 && isReturn) {
            isReturn = false;
            byte[] buffer = dreamProList.get(0);
            sendOrder(buffer);
        }
    }

    //移除最前面的指令
    private synchronized void removeFirst() {
        if (dreamProList.size() > 0) {
            dreamProList.remove(0);
        }
        isReturn = true;
        sleepCount = 0;
    }

    //清空指令
    public synchronized void clearOrders() {
        if (dreamProList != null) {
            dreamProList.clear();
        }
    }

    //添加指令
    public synchronized void addOrder(byte[] bytes) {
        if (dreamProList != null) {
            dreamProList.add(bytes);
        }
    }

    //添加指令集合
    public synchronized void addOrder(List<byte[]> bytes) {
        if (dreamProList != null) {
            dreamProList.addAll(bytes);
        }
    }

    //循环读取串口数据
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    size = mInputStream.available();
                    byte[] buffer = new byte[size];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        isReturn = true;
                        removeFirst();
                        if (serialPortDataCallBack != null) {
                            serialPortDataCallBack.onDataReceived(buffer, size);
                        }
                    }
                    Thread.sleep(5);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //超时检测线程
    private class TimeOutThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (sendFlag) {
                try {
                    Thread.sleep(50);
                    if (sleepCount > timeOutCount) {
                        if (serialPortDataCallBack != null) {
                            serialPortDataCallBack.onDataReceived(null, -1);
                        }
                        //超时移除指令
                        removeFirst();
                    } else if (dreamProList.size() > 0) {
                        sleepCount++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //命令发送线程
    private class SendOrderThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (sendFlag) {
                try {
                    sendFirst();
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //设置回调接口
    public void setSerialPortDataCallBack(SerialPortDataCallBack serialPortDataCallBack) {
        this.serialPortDataCallBack = serialPortDataCallBack;
    }

    //设置超时时间
    public void setTomeOut(long mesc) {
        //超时检测线程是每次休眠固定时间，通过休眠次数来判断超时
        timeOutCount = (int) (mesc / 50);
    }
}
