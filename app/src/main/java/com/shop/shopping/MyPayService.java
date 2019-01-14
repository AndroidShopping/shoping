package com.shop.shopping;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialPortBuilder;
import com.felhr.usbserial.SerialPortCallback;
import com.felhr.usbserial.UsbSerialDevice;
import com.shop.shopping.entity.PayState;
import com.shop.shopping.utils.TextUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android_serialport_api.SerialPort;

public class MyPayService extends Service implements SerialPortCallback {
    public static final int WHAT_WRITE_DATA = 0;
    public static final int WHAT_CLOSE_IO = 1;
    private static final int WHAT_DO_START_PAY = 2;
    private static final int WHAT_ON_ZHI_BI_QI_READ_DATA = 3;
    private static final int WHAT_ON_YING_BI_QI_READ_DATA = 4;
    private static final int WHAT_ON_TUI_BI_QI_2_YUAN_READ_DATA = 5;
    private static final int WHAT_ON_TUI_BI_QI_5_MAO_READ_DATA = 6;
    String TAG = "MyPayService";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;
    public static final int STATE_CHU_BI_ERROR = 1;
    public static final int STATE_OK = 2;
    private static int PAYOUT_SMALL_COUNT = 50;//退币器的最小币值 5欧角
    private static int PAYOUT_BIG_COUNT = 200;//退币器退币的最大币值 2欧元
    private static int PAYOUT_SMALL_PORT = 0;
    private static int PAYOUT_BIG_PORT = 1;

    private static int TUI_BI_QI_2_YUAN_PORT = 3;
    private static int YING_BI_SHOU_BI_PORT = 0;
    private static int TUI_BI_QI_5_MAO_PORT = 0;
    private static int ZHI_BI_QI_SHOU_BI_PORT = 2;


    private List<UsbSerialDevice> serialPorts;

    private Context context;
    private SerialPortBuilder builder;

    private Handler writeHandler;
    private WriteThread writeThread;


    private String zhiBiQiSerialName = "/dev/ttyS1";

    private String yingBiQiSerialName = "/dev/ttyS3";
    private volatile Handler mHandler;
    private volatile SerialPort zhiBiQiSerailPort, yingBiQiSerailPort;
    private Handler innerHandler;
    private Handler zhiBiQiWriteHandler;
    private Handler yingBiQiWriteHandler;


    public int checkDeviceState() {
        if (serialPorts == null || serialPorts.size() != 2) {
            return STATE_CHU_BI_ERROR;

        }
        return STATE_OK;

    }

    /**
     * 退款接口
     *
     * @param moneyCount 需要退款的额度，单位是欧分
     */
    public void doPayOut(int moneyCount) {
        int payoutSmallCount = 0;
        int payoutBigCount = 0;
        boolean hasFount = false;
        for (int i = 0; i < 256; i++) {
            payoutSmallCount = i;
            for (int j = 0; j < 256; j++) {
                int count = i * PAYOUT_SMALL_COUNT + j * PAYOUT_BIG_COUNT;
                if (count == moneyCount) {
                    hasFount = true;
                    payoutBigCount = j;
                    break;
                }
                if (count > moneyCount) {
                    break;
                }
            }
            if (hasFount) {
                break;
            }

        }

        if (payoutBigCount != 0) {
            write(TuibiOperation.buildRequest(TuibiOperation.COMMAND_PAY_OUT, (byte) payoutBigCount), PAYOUT_BIG_PORT);
        }

        if (payoutSmallCount != 0) {
            write(TuibiOperation.buildRequest(TuibiOperation.COMMAND_PAY_OUT, (byte) payoutSmallCount), PAYOUT_SMALL_PORT);
        }


    }

    UsbSerialDevice configUsbDevice(UsbSerialDevice device, int baudRate, int dataBits,
                                    int stopBits, int parity, int flowControl, int port) {
        if (!device.isOpen) {
            if (device.syncOpen()) {
                device.setBaudRate(baudRate);
                device.setDataBits(dataBits);
                device.setStopBits(stopBits);
                device.setParity(parity);
                device.setFlowControl(flowControl);
                device.setPortName(UsbSerialDevice.COM_PORT + port);
            }
        }


        return device;

    }

    /**
     * 用户开始支付接口
     *
     * @param moneyCount 用户需要支付的金额，单位为分
     */
    public void doPayFor(int moneyCount) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = PayState.START_PAY;
            message.sendToTarget();
        }
        if (innerHandler != null) {
            innerHandler.obtainMessage(WHAT_DO_START_PAY, moneyCount).sendToTarget();
        }

    }

    @Override
    public void onSerialPortsDetected(List<UsbSerialDevice> serialPorts) {
        this.serialPorts = serialPorts;
//        if (serialPorts.size() < 3) {
//            return;
//        }
        UsbSerialDevice yinbBiShouBiQi = serialPorts.get(YING_BI_SHOU_BI_PORT);
        configUsbDevice(yinbBiShouBiQi, 19200, 8, 0,
                0, 0, YING_BI_SHOU_BI_PORT);
//        configUsbDevice(serialPorts.get(ZHI_BI_QI_SHOU_BI_PORT), 9600, 8, 0,
//                2, 0, ZHI_BI_QI_SHOU_BI_PORT);
//
//        configUsbDevice(serialPorts.get(TUI_BI_QI_5_MAO_PORT), 9600, 8, 0,
//                2, 0, TUI_BI_QI_5_MAO_PORT);
//        configUsbDevice(serialPorts.get(TUI_BI_QI_2_YUAN_PORT), 9600, 8, 0,
//                2, 0, TUI_BI_QI_2_YUAN_PORT);
        new ReadThreadCOM(WHAT_ON_YING_BI_QI_READ_DATA, yinbBiShouBiQi.getInputStream()).start();
//        new ReadThreadCOM(WHAT_ON_ZHI_BI_QI_READ_DATA, serialPorts.get(ZHI_BI_QI_SHOU_BI_PORT).getInputStream()).start();
//        new ReadThreadCOM(WHAT_ON_TUI_BI_QI_5_MAO_READ_DATA, serialPorts.get(TUI_BI_QI_5_MAO_PORT).getInputStream()).start();
//        new ReadThreadCOM(WHAT_ON_TUI_BI_QI_2_YUAN_READ_DATA, serialPorts.get(TUI_BI_QI_2_YUAN_PORT).getInputStream()).start();
        if (writeThread == null) {
            writeThread = new WriteThread();
            writeThread.start();
        }


    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        this.context = this;
        MyPayService.SERVICE_CONNECTED = true;
        innerHandler = new Handler() {
            int count = 0;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case WHAT_DO_START_PAY:
                        count = (int) msg.obj;
                        break;
//                    case WHAT_ON_ZHI_BI_QI_READ_DATA:
                    default:
                        byte[] data = (byte[]) msg.obj;
                        Log.d(TAG, "handleMessage:  = " +
                                TextUtils.printHexString(data));
                        break;
                    case WHAT_ON_YING_BI_QI_READ_DATA:
                        break;
                }

            }
        };
        builder = SerialPortBuilder.createSerialPortBuilder(this);
        boolean ret = builder.openSerialPorts(context, BAUD_RATE,
                8,
                0,
                2,
                0);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PayBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (builder != null) {
            builder.unregisterListeners(context);
        }
        if (writeHandler != null) {
            writeHandler.obtainMessage(WHAT_CLOSE_IO).sendToTarget();
        }
        MyPayService.SERVICE_CONNECTED = false;
    }

    public void write(byte[] data, int port) {
        if (writeHandler != null) {
            writeHandler.obtainMessage(WHAT_WRITE_DATA, port, 0, data).sendToTarget();
        }

    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }


    public static class PayBinder extends Binder {
        MyPayService service;

        public PayBinder(MyPayService service) {
            this.service = service;
        }

        public MyPayService getService() {
            return service;
        }
    }

    private class ReadThreadCOM extends Thread {
        private AtomicBoolean keep = new AtomicBoolean(true);
        private SerialInputStream inputStream;
        private int whatForReadData;

        public ReadThreadCOM(int whatRead, SerialInputStream serialInputStream) {
            this.inputStream = serialInputStream;
            whatForReadData = whatRead;
        }

        @Override
        public void run() {
            while (keep.get()) {
                if (inputStream == null) {
                    return;
                }
                try {
                    int available = inputStream.available();
                    if (available != 0) {
                        byte[] data = new byte[available];
                        inputStream.read(data);
                        innerHandler.obtainMessage(whatForReadData, data).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static String toASCII(int value) {
        int length = 4;
        StringBuilder builder = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            builder.append((char) ((value >> (8 * i)) & 0xFF));
        }
        return builder.toString();
    }

    private class SerailWriteThread extends Thread {
        OutputStream outputStream;
        Handler serailHandler;

        public SerailWriteThread(OutputStream stream, Handler handler) {
            this.outputStream = stream;
            serailHandler = handler;
        }

        @Override
        @SuppressLint("HandlerLeak")
        public void run() {
            try {
                Thread.sleep(5000);
                outputStream.write(0x2);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Looper.prepare();

            serailHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    byte[] data = (byte[]) msg.obj;
                    try {
                        outputStream.write(data);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
        }
    }

    private class WriteThread extends Thread {

        @Override
        @SuppressLint("HandlerLeak")
        public void run() {
            Looper.prepare();
            writeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case WHAT_CLOSE_IO:
                            for (UsbSerialDevice serialPort : serialPorts) {
                                if (serialPort != null && serialPort.isOpen()) {
                                    serialPort.close();
                                }
                            }
                            break;
                        default:
                            int port = msg.arg1;
                            byte[] data = (byte[]) msg.obj;
                            if (port <= serialPorts.size() - 1) {
                                UsbSerialDevice serialDevice = serialPorts.get(port);
                                serialDevice.getOutputStream().write(data);
                            }
                    }

                }
            };
            Looper.loop();
        }
    }


}
