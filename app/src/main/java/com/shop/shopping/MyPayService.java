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

import com.felhr.usbserial.BuildConfig;
import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialPortBuilder;
import com.felhr.usbserial.SerialPortCallback;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.felhr.utils.HexData;
import com.shop.shopping.entity.PayState;
import com.shop.shopping.utils.TextUtils;
import com.shop.shopping.utils.YingBiQiUtils;
import com.shop.shopping.utils.ZhiBiQiUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.shop.shopping.entity.PayState.PAY_RECEIVE_MONEY;

public class MyPayService extends Service implements SerialPortCallback {
    public static final int WHAT_WRITE_DATA = 0;
    public static final int WHAT_CLOSE_IO = 1;
    private static final int WHAT_DO_START_PAY = 2;
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private boolean isDebug = true;

    private static final int WHAT_ON_ZHI_BI_QI_READ_DATA = 3;
    private static final int WHAT_ON_YING_BI_QI_READ_DATA = 4;
    private static final int WHAT_ON_TUI_BI_QI_2_YUAN_READ_DATA = 5;
    private static final int WHAT_ON_TUI_BI_QI_5_MAO_READ_DATA = 6;
    public static final int WHAT_DO_CANCEL = 7;
    String TAG = "MyPayService";
    private static final int BAUD_RATE = 9600; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;
    public static final int STATE_CHU_BI_ERROR = 1;
    public static final int STATE_OK = 2;
    private static int PAYOUT_SMALL_COUNT = 50;//退币器的最小币值 5欧角
    private static int PAYOUT_BIG_COUNT = 200;//退币器退币的最大币值 2欧元

    private static int TUI_BI_QI_2_YUAN_PORT = 0;
    private static int ZHI_BI_QI_SHOU_BI_PORT = 1;
    private static int TUI_BI_QI_5_MAO_PORT = 2;

    private static int YING_BI_SHOU_BI_PORT = 3;
    public static volatile boolean hasInit = false;


    private List<UsbSerialDevice> serialPorts;

    private Context context;
    private SerialPortBuilder builder;

    private Handler writeHandler;
    private WriteThread writeThread;

    private volatile Handler mHandler;
    private Handler innerHandler;
    private ReadThreadCOM readThreadCOM1;
    private ReadThreadCOM readThreadCOM;
    private ReadThreadCOM readThreadCOM2;
    private ReadThreadCOM readThreadCOM3;


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
        Log.d(TAG, "doPayOut: moneyCount =" + moneyCount);
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
            write(TuibiOperation.buildRequest(TuibiOperation.COMMAND_PAY_OUT, (byte) payoutBigCount), TUI_BI_QI_2_YUAN_PORT);
        }

        if (payoutSmallCount != 0) {
            write(TuibiOperation.buildRequest(TuibiOperation.COMMAND_PAY_OUT, (byte) payoutSmallCount), TUI_BI_QI_5_MAO_PORT);
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
        Log.d(TAG, "doPayFor: moneyCount =" + moneyCount);
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = PayState.START_PAY;
            message.sendToTarget();
        }
        if (innerHandler != null) {
            innerHandler.obtainMessage(WHAT_DO_START_PAY, moneyCount).sendToTarget();
        }

    }

    /**
     * 取消收款
     */
    public void doCancel() {
        Log.d(TAG, "doCancel: ");
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = PayState.START_PAY;
            message.sendToTarget();
        }
        if (innerHandler != null) {
            innerHandler.obtainMessage(WHAT_DO_CANCEL).sendToTarget();
        }
    }

    @Override
    public void onSerialPortsDetected(List<UsbSerialDevice> serialPorts) {
        this.serialPorts = serialPorts;
        if (serialPorts.size() < 3) {
            hasInit = false;
            return;
        } else {
            hasInit = true;
        }
        configUsbDevice(serialPorts.get(YING_BI_SHOU_BI_PORT), 19200,
                UsbSerialInterface.DATA_BITS_8,
                UsbSerialInterface.STOP_BITS_1,
                UsbSerialInterface.PARITY_NONE,
                UsbSerialInterface.FLOW_CONTROL_OFF, YING_BI_SHOU_BI_PORT);
        configUsbDevice(serialPorts.get(ZHI_BI_QI_SHOU_BI_PORT), 9600,
                UsbSerialInterface.DATA_BITS_8,
                UsbSerialInterface.STOP_BITS_1,
                UsbSerialInterface.PARITY_EVEN,
                UsbSerialInterface.FLOW_CONTROL_OFF, ZHI_BI_QI_SHOU_BI_PORT);
//
        configUsbDevice(serialPorts.get(TUI_BI_QI_5_MAO_PORT),

                9600,
                UsbSerialInterface.DATA_BITS_8,
                UsbSerialInterface.STOP_BITS_1,
                UsbSerialInterface.PARITY_EVEN,
                UsbSerialInterface.FLOW_CONTROL_OFF, TUI_BI_QI_5_MAO_PORT);
        configUsbDevice(serialPorts.get(TUI_BI_QI_2_YUAN_PORT), 9600,
                UsbSerialInterface.DATA_BITS_8,
                UsbSerialInterface.STOP_BITS_1,
                UsbSerialInterface.PARITY_EVEN,
                UsbSerialInterface.FLOW_CONTROL_OFF, TUI_BI_QI_2_YUAN_PORT);
        readThreadCOM1 = new ReadThreadCOM(WHAT_ON_YING_BI_QI_READ_DATA, YING_BI_SHOU_BI_PORT,
                serialPorts.get(YING_BI_SHOU_BI_PORT).getInputStream());
        readThreadCOM1.start();
        readThreadCOM = new ReadThreadCOM(WHAT_ON_ZHI_BI_QI_READ_DATA, ZHI_BI_QI_SHOU_BI_PORT,
                serialPorts.get(ZHI_BI_QI_SHOU_BI_PORT).getInputStream());
        readThreadCOM.start();
        readThreadCOM2 = new ReadThreadCOM(WHAT_ON_TUI_BI_QI_5_MAO_READ_DATA, TUI_BI_QI_5_MAO_PORT,
                serialPorts.get(TUI_BI_QI_5_MAO_PORT).getInputStream());
        readThreadCOM2.start();
        readThreadCOM3 = new ReadThreadCOM(WHAT_ON_TUI_BI_QI_2_YUAN_READ_DATA, TUI_BI_QI_2_YUAN_PORT,
                serialPorts.get(TUI_BI_QI_2_YUAN_PORT).getInputStream());
        readThreadCOM3.start();
        if (writeThread == null) {
            writeThread = new WriteThread();
            writeThread.start();
        }
        try {
            countDownLatch.await();
            doResetShouBiqi();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        this.context = this;
        MyPayService.SERVICE_CONNECTED = true;
        innerHandler = new Handler() {
            int count = 0;
            boolean hasPayCompelete = false;
            int currentReceiveMoney = 0;
            private boolean isFirst = true;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case WHAT_DO_START_PAY:
                        if (!isFirst && !hasPayCompelete) {
                            mHandler.obtainMessage(PayState.PAY_LAST_ERROR).sendToTarget();
                            return;
                        }
                        isFirst = false;
                        hasPayCompelete = false;
                        count = (int) msg.obj;
                        currentReceiveMoney = 0;
                        write(new byte[]{0x3e}, ZHI_BI_QI_SHOU_BI_PORT);
                        write(new byte[]{0x02}, ZHI_BI_QI_SHOU_BI_PORT);
                        write(new byte[]{'Y', 'D', 'M', 0x01, 0xd, 0xa}, YING_BI_SHOU_BI_PORT);
                        break;
                    case WHAT_DO_CANCEL:
                        hasPayCompelete = true;
                        doResetShouBiqi();
                        doPayOut(currentReceiveMoney);
                        break;
                    case WHAT_ON_ZHI_BI_QI_READ_DATA:
                        byte[] data = (byte[]) msg.obj;
                        if (data != null) {

                            if (ZhiBiQiUtils.isReceiveMoney(data)) {
                                /**
                                 * 当前收到了纸币
                                 */
                                int receiveMoney = ZhiBiQiUtils.convertMoneyTOCent(data);
                                if (hasPayCompelete) {
                                    /**
                                     * 拒收，且进入禁能状态
                                     */
                                    write(new byte[]{0x0f}, ZHI_BI_QI_SHOU_BI_PORT);
                                    write(new byte[]{0x5e}, ZHI_BI_QI_SHOU_BI_PORT);
                                } else {
                                    /**
                                     * 增加收款额
                                     */
                                    currentReceiveMoney += receiveMoney;
                                    if (mHandler != null) {
                                        mHandler.obtainMessage(PAY_RECEIVE_MONEY, currentReceiveMoney).sendToTarget();
                                    }
                                    if (currentReceiveMoney >= count) {
                                        hasPayCompelete = true;
                                        write(new byte[]{0x02}, ZHI_BI_QI_SHOU_BI_PORT);
                                        doResetShouBiqi();
                                        if (currentReceiveMoney > count) {
                                            /**
                                             * 需要退币
                                             */
                                            doPayOut(currentReceiveMoney - count);
                                        }
                                        if (mHandler != null) {
                                            mHandler.obtainMessage(PayState.PAY_OK, currentReceiveMoney,
                                                    currentReceiveMoney - count).sendToTarget();
                                        }

                                    } else {

                                        write(new byte[]{0x02}, ZHI_BI_QI_SHOU_BI_PORT);
                                    }
                                }
                            }
                        }

                        break;
                    case WHAT_ON_YING_BI_QI_READ_DATA:
                        data = (byte[]) msg.obj;
                        if (data != null) {
                            if (isDebug) {
                                Log.d(TAG, "handleMessage:  = " +
                                        TextUtils.printHexString(data));
                            }

                            if (YingBiQiUtils.isReceiveMoney(data)) {
                                /**
                                 * 当前收到了硬币
                                 */
                                int receiveMoney = YingBiQiUtils.getMoney(data);
                                if (hasPayCompelete) {
                                    /**
                                     * 拒收，且进入禁能状态
                                     */
                                    if (currentReceiveMoney != receiveMoney) {
                                        write(new byte[]{'Y', 'D', 'M', 0x31, 0xd, 0xa}, YING_BI_SHOU_BI_PORT);
                                    }

                                    write(new byte[]{'Y', 'D', 'M', 0x02, 0xd, 0xa}, YING_BI_SHOU_BI_PORT);
                                } else {
                                    /**
                                     * 增加收款额
                                     */
                                    currentReceiveMoney += receiveMoney;
                                    if (mHandler != null) {
                                        mHandler.obtainMessage(PAY_RECEIVE_MONEY, currentReceiveMoney).sendToTarget();
                                    }
                                    if (currentReceiveMoney >= count) {
                                        hasPayCompelete = true;
                                        /**
                                         * 拒收，且进入禁能状态
                                         */
//                                        write(new byte[]{'Y', 'D', 'M', 0x31, 0xd, 0xa}, ZHI_BI_QI_SHOU_BI_PORT);
                                        write(new byte[]{'Y', 'D', 'M', 0x02, 0xd, 0xa}, YING_BI_SHOU_BI_PORT);
                                        doResetShouBiqi();
                                        if (currentReceiveMoney > count) {
                                            /**
                                             * 需要退币
                                             */
                                            doPayOut(currentReceiveMoney - count);
                                        }
                                        if (mHandler != null) {
                                            mHandler.obtainMessage(PayState.PAY_OK, currentReceiveMoney,
                                                    currentReceiveMoney - count).sendToTarget();
                                        }

                                    } else {
//                                        write(new byte[]{0x02}, ZHI_BI_QI_SHOU_BI_PORT);
                                    }
                                }
                            }
                        }

                        break;
                    default:


                }

            }
        };
        builder = SerialPortBuilder.createSerialPortBuilder(this);
        boolean ret = builder.openSerialPorts(context);

    }

    private void doResetShouBiqi() {
        write(new byte[]{0x5e}, ZHI_BI_QI_SHOU_BI_PORT);
        write(new byte[]{'Y', 'D', 'M', 0x02, 0x0d, 0x0a}, YING_BI_SHOU_BI_PORT);
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
        try {

            if (readThreadCOM != null) {
                readThreadCOM.setRunAable(false);
            }
            if (readThreadCOM1 != null) {
                readThreadCOM1.setRunAable(false);

            }
            if (readThreadCOM2 != null) {
                readThreadCOM2.setRunAable(false);
            }
            if (readThreadCOM3 != null) {
                readThreadCOM3.setRunAable(false);
            }
            if (serialPorts != null) {
                for (UsbSerialDevice serialPort : serialPorts) {
                    if (serialPort != null && serialPort.isOpen) {
                        serialPort.close();
                        SerialInputStream inputStream = serialPort.getInputStream();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        private int port;

        public ReadThreadCOM(int whatRead, int port, SerialInputStream serialInputStream) {
            this.inputStream = serialInputStream;
            whatForReadData = whatRead;
            this.port = port;

        }

        public void setRunAable(boolean isRun) {
            keep.set(isRun);
        }

        @Override
        public void run() {
            while (keep.get()) {
                if (inputStream == null) {
                    return;
                }
                try {
                    int len;
                    // byte[] rbuf = new byte[4096];
                    byte[] rbuf = new byte[20];
                    len = inputStream.read(rbuf);
                    if (len < 0) {
                        Log.d(TAG, "read fail");
                        return;
                    }
                    if (len > 0) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "port:  = " + port);
                            Log.d(TAG, "  whatForReadData = " + whatForReadData);

                            Log.d(TAG, "handleMessage:  = " +
                                    TextUtils.printHexString(rbuf));
                            Log.d(TAG, "  whatForReadData = " + whatForReadData);
                        }
                        innerHandler.obtainMessage(whatForReadData, rbuf).sendToTarget();
                    } else {
                        Log.d(TAG, "no data receive:  = ");
                    }
                    try {
                        Thread.sleep(50);
//                        if (writeHandler != null) {
//
//                            writeHandler.obtainMessage(WHAT_WRITE_DATA, 0, 0, new byte[]{2}).sendToTarget();
//                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
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
                                Log.d(TAG, "WriteThread ,handleMessage: data =" + HexData.hexToString(data) + ",port=" + port);
                                try {
                                    serialDevice.getOutputStream().write(data);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                    }

                }
            };
            countDownLatch.countDown();
            Looper.loop();
        }
    }


}
