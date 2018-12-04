package comm.shop.shopping.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;

import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.boothprint.bt.BluetoothActivity;
import comm.shop.shopping.boothprint.bt.BtUtil;
import comm.shop.shopping.boothprint.print.PrintQueue;
import comm.shop.shopping.boothprint.print.PrintUtil;
import comm.shop.shopping.boothprint.util.ToastUtil;

/**
 * 蓝牙搜索界面
 * Created by liuguirong on 2017/8/3.
 */

public class SearchBluetoothActivity extends BluetoothActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private BluetoothAdapter bluetoothAdapter;
    private ListView lv_searchblt;
    private TextView tv_title;
    private TextView tv_summary;
    private SearchBleAdapter searchBleAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchbooth);
        lv_searchblt = (ListView) findViewById(R.id.lv_searchblt);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_summary = (TextView) findViewById(R.id.tv_summary);
        //初始化蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        searchBleAdapter = new SearchBleAdapter(SearchBluetoothActivity.this, null);
        lv_searchblt.setAdapter(searchBleAdapter);
        init();
        searchDeviceOrOpenBluetooth();
        lv_searchblt.setOnItemClickListener(this);
        tv_title.setOnClickListener(this);
        tv_summary.setOnClickListener(this);
    }


    private void init() {
        if (!BtUtil.isOpen(bluetoothAdapter)) {
            tv_title.setText(R.string.disconnect_printer);
            tv_summary.setText(R.string.close_blue);

        } else {
            if (!PrintUtil.isBondPrinter(this, bluetoothAdapter)) {
                //未绑定蓝牙打印机器
                tv_title.setText(R.string.disconnect_printer);
                tv_summary.setText(R.string.search_bluetooth);

            } else {
                //已绑定蓝牙设备
                tv_title.setText(getPrinterName() + comm.shop.shopping.utils.TextUtils.getString(R.string.connected));
                String blueAddress = PrintUtil.getDefaultBluethoothDeviceAddress(this);
                if (TextUtils.isEmpty(blueAddress)) {
                    blueAddress = comm.shop.shopping.utils.TextUtils.getString(R.string.search_bluetooth);
                }
                tv_summary.setText(blueAddress);
            }
        }
    }

    @Override
    public void btStatusChanged(Intent intent) {

        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {//蓝牙被关闭时强制打开
            bluetoothAdapter.enable();
        }
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {//蓝牙打开时搜索蓝牙
            searchDeviceOrOpenBluetooth();
        }
    }

    private String getPrinterName() {
        String dName = PrintUtil.getDefaultBluetoothDeviceName(this);
        if (TextUtils.isEmpty(dName)) {
            dName = comm.shop.shopping.utils.TextUtils.getString(R.string.unkonw_device);
        }
        return dName;
    }

    private String getPrinterName(String dName) {
        if (TextUtils.isEmpty(dName)) {
            dName = comm.shop.shopping.utils.TextUtils.getString(R.string.unkonw_device);
        }
        return dName;
    }

    /**
     * 开始搜索
     * search device
     */
    private void searchDeviceOrOpenBluetooth() {
        if (BtUtil.isOpen(bluetoothAdapter)) {
            BtUtil.searchDevices(bluetoothAdapter);
        }
    }

    /**
     * 关闭搜索
     * cancel search
     */
    @Override
    protected void onStop() {
        super.onStop();
        BtUtil.cancelDiscovery(bluetoothAdapter);
    }

    @Override
    public void btStartDiscovery(Intent intent) {
        tv_title.setText(R.string.search_device);
        tv_summary.setText("");
    }

    @Override
    public void btFinishDiscovery(Intent intent) {
        tv_title.setText(R.string.search_ok);
        tv_summary.setText(R.string.re_search);
    }

    @Override
    public void btFoundDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (null != bluetoothAdapter && device != null) {
            searchBleAdapter.addDevices(device);
            String dName = device.getName() == null ? comm.shop.shopping.utils.TextUtils.getString(R.string.unkonw_device) : device.getName();
        }
    }

    @Override
    public void btBondStatusChange(Intent intent) {
        super.btBondStatusChange(intent);
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDING://正在配对
                break;
            case BluetoothDevice.BOND_BONDED://配对结束
                connectBlt(device);
                break;
            case BluetoothDevice.BOND_NONE://取消配对/未配对
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (null == searchBleAdapter) {
            return;
        }
        final BluetoothDevice bluetoothDevice = searchBleAdapter.getItem(position);
        if (null == bluetoothDevice) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(comm.shop.shopping.utils.TextUtils.getString(R.string.bind) + getPrinterName(bluetoothDevice.getName()) + "?")
                .setMessage(R.string.click_bind_device)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            BtUtil.cancelDiscovery(bluetoothAdapter);


                            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                                connectBlt(bluetoothDevice);
                            } else {
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(bluetoothDevice);
                            }
                            PrintQueue.getQueue(getApplicationContext()).disconnect();
                            String name = bluetoothDevice.getName();
                        } catch (Exception e) {
                            e.printStackTrace();
                            PrintUtil.setDefaultBluetoothDeviceAddress(getApplicationContext(), "");
                            PrintUtil.setDefaultBluetoothDeviceName(getApplicationContext(), "");
                            ToastUtil.showToast(SearchBluetoothActivity.this, R.string.bind_fail);
                        }
                    }
                })
                .create()
                .show();


    }

    /***
     * 配对成功连接蓝牙
     * @param bluetoothDevice
     */

    private void connectBlt(BluetoothDevice bluetoothDevice) {
        if (null != searchBleAdapter) {
            searchBleAdapter.setConnectedDeviceAddress(bluetoothDevice.getAddress());
        }
        init();
        searchBleAdapter.notifyDataSetChanged();
        PrintUtil.setDefaultBluetoothDeviceAddress(getApplicationContext(), bluetoothDevice.getAddress());
        PrintUtil.setDefaultBluetoothDeviceName(getApplicationContext(), bluetoothDevice.getName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_title:

                break;
            case R.id.tv_summary:
                searchDeviceOrOpenBluetooth();
                break;
        }
    }
}
