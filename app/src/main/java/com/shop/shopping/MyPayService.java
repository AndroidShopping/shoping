package com.shop.shopping;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialPortBuilder;
import com.felhr.usbserial.SerialPortCallback;
import com.felhr.usbserial.UsbSerialDevice;
import com.shop.shopping.entity.PayState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android_serialport_api.SerialPort;
import cn.droidlover.xdroidmvp.shopping.BuildConfig;
import cn.droidlover.xdroidmvp.shopping.R;

public class MyPayService extends Service implements SerialPortCallback {
    public static final int WHAT_WRITE_DATA = 0;
    public static final int WHAT_CLOSE_IO = 1;
    String TAG = "MyPayService";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;
    public static final int STATE_CHU_BI_ERROR = 1;
    public static final int STATE_OK = 2;
    private static int PAYOUT_SMALL_COUNT = 50;//退币器的最小币值 5欧角
    private static int PAYOUT_BIG_COUNT = 200;//退币器退币的最大币值 2欧元
    private static int PAYOUT_SMALL_PORT = 0;
    private static int PAYOUT_BIG_PORT = 1;

    private List<UsbSerialDevice> serialPorts;

    private Context context;
    private SerialPortBuilder builder;

    private Handler writeHandler;
    private WriteThread writeThread;

    private ReadThreadCOM readThreadCOM1, readThreadCOM2;


    private String zhiBiQiSerialName = "/dev/ttyS1";

    private String yingBiQiSerialName = "/dev/ttyS3";
    private volatile Handler mHandler;
    private volatile SerialPort zhiBiQiSerailPort, yingBiQiSerailPort;


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

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                boolean ret = builder.openSerialPorts(context, BAUD_RATE,
                        8,
                        0,
                        2,
                        0);
                if (!ret)
                    Toast.makeText(context, R.string.cant, Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {

                UsbDevice usbDevice = arg1.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean ret = builder.disconnectDevice(usbDevice);

                if (ret)
                    Toast.makeText(context, R.string.usb_dis, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.dont_usb, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);

            }
        }
    };


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

    }

    @Override
    public void onSerialPortsDetected(List<UsbSerialDevice> serialPorts) {
        this.serialPorts = serialPorts;
        if (serialPorts.size() == 0)
            return;

        if (writeThread == null) {
            writeThread = new WriteThread();
            writeThread.start();
        }

        int index = 0;

        if (readThreadCOM1 == null && index <= serialPorts.size() - 1
                && serialPorts.get(index).isOpen()) {
            readThreadCOM1 = new ReadThreadCOM(index,
                    serialPorts.get(index).getInputStream());
            readThreadCOM1.start();
        }

        index++;
        if (readThreadCOM2 == null && index <= serialPorts.size() - 1
                && serialPorts.get(index).isOpen()) {
            readThreadCOM2 = new ReadThreadCOM(index,
                    serialPorts.get(index).getInputStream());
            readThreadCOM2.start();
        }
//        try {
//            Thread.sleep(500);
//            doPayOut(50);
//            Thread.sleep(10000);
//            doPayOut(100);
//            Thread.sleep(10000);
//            doPayOut(200);
//            Thread.sleep(10000);
//            doPayOut(250);
//            Thread.sleep(10000);
//            doPayOut(300);
//            Thread.sleep(10000);
//            doPayOut(350);
//            Thread.sleep(10000);
//            doPayOut(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onCreate() {
        this.context = this;
        MyPayService.SERVICE_CONNECTED = true;
        setFilter();
        builder = SerialPortBuilder.createSerialPortBuilder(this);
        boolean ret = builder.openSerialPorts(context, BAUD_RATE,
                8,
                0,
                2,
                0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    zhiBiQiSerailPort = new SerialPort(new File(zhiBiQiSerialName), 9600, 8, 1, 'e');
                    new SerailWriteThread(zhiBiQiSerailPort.getOutputStream(), null).start();
                    new SerailReadThread(zhiBiQiSerailPort.getInputStream(), zhiBiQiSerialName).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    yingBiQiSerailPort = new SerialPort(new File(yingBiQiSerialName), 9600, 2, 8, 'e');
                    new SerailWriteThread(yingBiQiSerailPort.getOutputStream(), null).start();
                    new SerailReadThread(yingBiQiSerailPort.getInputStream(), yingBiQiSerialName).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();


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


    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
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

    private class SerailReadThread extends Thread {
        private AtomicBoolean keep = new AtomicBoolean(true);
        private InputStream inputStream;
        private String serialName;

        public SerailReadThread(InputStream inputStream, String serialName) {
            this.inputStream = inputStream;
            this.serialName = serialName;
        }

        @Override
        public void run() {
            while (keep.get()) {
                if (inputStream == null) {
                    return;
                }
                if (BuildConfig.DEBUG) {
//                    Log.d(TAG, "SerailReadThread run: serialName =" + serialName);
                }
                int value = 0;
                try {
                    value = inputStream.read();
                    if (value != -1) {
                        String str = toASCII(value);
                        Log.d(TAG, "SerailReadThread run: " + value);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setKeep(boolean keep) {
            this.keep.set(keep);
        }

    }

    private class ReadThreadCOM extends Thread {
        private AtomicBoolean keep = new AtomicBoolean(true);
        private SerialInputStream inputStream;

        public ReadThreadCOM(int port, SerialInputStream serialInputStream) {
            this.inputStream = serialInputStream;
        }

        @Override
        public void run() {
            while (keep.get()) {
                if (inputStream == null)
                    return;
                int value = inputStream.read();
                if (value != -1) {
                    String str = toASCII(value);
//                    mHandler.obtainMessage(SYNC_READ, port, 0, str).sendToTarget();
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
