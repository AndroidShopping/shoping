package com.shop.shopping.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.shopping.R;
import com.shop.shopping.boothprint.BtService;
import com.shop.shopping.boothprint.base.AppInfo;
import com.shop.shopping.boothprint.bt.BtInterface;
import com.shop.shopping.boothprint.bt.BtUtil;
import com.shop.shopping.boothprint.print.PrintUtil;
import com.shop.shopping.boothprint.util.ToastUtil;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.utils.TextUtils;

public class PrintOrderActivity extends BaseAcivity implements BtInterface {
    public static final String RESULT = "result";
    public static final String ORDER_ID = "orderId";
    public static final String REPAY = "repay";
    public static final int TOTAL_SECOND = 30 * 1000;
    public static final int INTERVAL_SECON = 1 * 1000;
    private BluetoothAdapter bluetoothAdapter;
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.repay_money_view)
    TextView repayMoneyView;
    @BindView(R.id.repay_container)
    LinearLayout repayContainer;
    @BindView(R.id.submit_view)
    Button submitView;
    @BindView(R.id.back_return_view)
    TextView backReturnView;
    private CountDownTimer mTimer;

    public static void start(Context context, ShopResult result, String orderId, int repay) {
        Intent intent = new Intent(context, PrintOrderActivity.class);
        intent.putExtra(RESULT, result);
        intent.putExtra(ORDER_ID, orderId);
        intent.putExtra(REPAY, repay);
        context.startActivity(intent);

    }

    /**
     * blue tooth broadcast receiver
     */
    protected BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            String action = intent.getAction();
            if (android.text.TextUtils.isEmpty(action)) {
                return;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btStartDiscovery(intent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btFinishDiscovery(intent);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                btStatusChanged(intent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                btFoundDevice(intent);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                btBondStatusChange(intent);
            } else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                btPairingRequest(intent);
            }
        }
    };


    @Override
    public void initData(Bundle savedInstanceState) {
        titlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_BUTTON
                        || action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    onBackPressed();
                }
            }
        });
        Intent intent = getIntent();
        int repay = intent.getIntExtra(REPAY, 0);
        if (repay == 0) {
            repayContainer.setVisibility(View.GONE);
        } else {
            repayContainer.setVisibility(View.VISIBLE);
            repayMoneyView.setText(TextUtils.getPriceText(repay));
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //已绑定蓝牙设备
        String blueAddress = PrintUtil.getDefaultBluethoothDeviceAddress(this);
        if (!BtUtil.isOpen(bluetoothAdapter) || !PrintUtil.isBondPrinter(this, bluetoothAdapter) ||
                android.text.TextUtils.isEmpty(blueAddress)) {
            ToastUtil.showToast(this, R.string.disconnect_printer);

            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                bluetoothAdapter.enable();
                ToastUtil.showToast(this, R.string.ble_state_cloes);
                return;
            }
            if (android.text.TextUtils.isEmpty(AppInfo.btAddress)) {
                startActivity(new Intent(this, SearchBluetoothActivity.class));
            }

        } else {
            mTimer = new CountDownTimer(TOTAL_SECOND, INTERVAL_SECON) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long second = millisUntilFinished / INTERVAL_SECON;
                    String format = String.format("%s(%ds)", TextUtils.getString(R.string.on_return_main), second);
                    backReturnView.setText(format);

                }

                @Override
                public void onFinish() {
                    MainActivity.start(PrintOrderActivity.this);

                }
            };
            mTimer.start();


            intent.setClass(getApplicationContext(), BtService.class);
            intent.setAction(PrintUtil.ACTION_PRINT_TICKET);
            startService(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_print_order;
    }

    @Override
    public Object newP() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    public void btStartDiscovery(Intent intent) {

    }

    @Override
    public void btFinishDiscovery(Intent intent) {

    }

    @Override
    public void btStatusChanged(Intent intent) {

    }


    @Override
    public void btFoundDevice(Intent intent) {

    }

    @Override
    public void btBondStatusChange(Intent intent) {

    }

    @Override
    public void btPairingRequest(Intent intent) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        BtUtil.registerBluetoothReceiver(mBtReceiver, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BtUtil.unregisterBluetoothReceiver(mBtReceiver, this);
    }
}
