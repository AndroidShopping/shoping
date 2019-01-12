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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import cn.droidlover.xdroidmvp.shopping.BuildConfig;
import cn.droidlover.xdroidmvp.shopping.R;

public class MyPayService extends Service implements SerialPortCallback {
    String TAG = "MyPayService";

    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;

    private List<UsbSerialDevice> serialPorts;

    private Context context;
    private UsbManager usbManager;
    private SerialPortBuilder builder;

    private Handler writeHandler;
    private WriteThread writeThread;

    private ReadThreadCOM readThreadCOM1, readThreadCOM2;
    private SerailReadThread zhiBiQiReadThread, yingBiReadThread;
    private SerailWriteThread zhiBiQiWriteThread, yingBiWriteThread;
    private Handler zhiBiQiHandler, yingBiHandler;

    private IBinder binder = new UsbBinder();
    private String zhiBiQiSerialName = "/dev/ttyS1";

    private Handler mHandler;
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
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        write(new byte[]{0x2}, 0);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        write(new byte[]{0x30}, 0);
    }

    @Override
    public void onCreate() {
        this.context = this;
        MyPayService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        builder = SerialPortBuilder.createSerialPortBuilder(this);


        boolean ret = builder.openSerialPorts(context, BAUD_RATE,
                8,
                0,
                2,
                0);
        SerialPortFinder finder = new SerialPortFinder();
        String[] allDevicesPath = finder.getAllDevicesPath();
        for (String s : allDevicesPath) {
            try {

                if (zhiBiQiSerialName.toLowerCase().equals(s.toLowerCase())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SerialPort serialPort = null;
                            try {
                                serialPort = new SerialPort(new File(s), 9600, 2,8,1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Handler writeHandler = null;
                            new SerailWriteThread(serialPort.getOutputStream(), null).start();
                            new SerailReadThread(serialPort.getInputStream(), s).start();
                        }
                    }).start();

                } else {
                    writeHandler = null;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (builder != null)
            builder.unregisterListeners(context);
        MyPayService.SERVICE_CONNECTED = false;
    }

    public void write(byte[] data, int port) {
        if (writeThread != null)
            writeHandler.obtainMessage(0, port, 0, data).sendToTarget();
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

    public class UsbBinder extends Binder {
        public MyPayService getService() {
            return MyPayService.this;
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
        }

        public void setKeep(boolean keep) {
            this.keep.set(keep);
        }

    }

    private class ReadThreadCOM extends Thread {
        private int port;
        private AtomicBoolean keep = new AtomicBoolean(true);
        private SerialInputStream inputStream;

        public ReadThreadCOM(int port, SerialInputStream serialInputStream) {
            this.port = port;
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

        public void setKeep(boolean keep) {
            this.keep.set(keep);
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
                outputStream.write(0x2);
                outputStream.flush();
            } catch (IOException e) {
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
                    int port = msg.arg1;
                    byte[] data = (byte[]) msg.obj;
                    if (port <= serialPorts.size() - 1) {
                        UsbSerialDevice serialDevice = serialPorts.get(port);
//                        serialDevice.write(data);
                        serialDevice.getOutputStream().write(data);
                    }
                }
            };
            Looper.loop();
        }
    }
}
