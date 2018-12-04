package comm.shop.shopping.boothprint;

import android.bluetooth.BluetoothAdapter;
import android.text.TextUtils;

import comm.shop.shopping.App;
import comm.shop.shopping.boothprint.print.PrintUtil;
import comm.shop.shopping.ui.MainActivity;


/**
 * Created by liuguirong on 8/1/17.
 */

public class BluetoothController {

    public static void init(MainActivity activity) {
        if (null == activity.mAdapter) {
            activity.mAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (null == activity.mAdapter) {
            activity.mBtEnable = false;
            return;
        }
        if (!activity.mAdapter.isEnabled()) {
            //没有在开启中也没有打开
//            if ( activity.mAdapter.getState()!=BluetoothAdapter.STATE_TURNING_ON  && activity.mAdapter.getState()!=BluetoothAdapter.STATE_ON ){
            if (activity.mAdapter.getState() == BluetoothAdapter.STATE_OFF) {//蓝牙被关闭时强制打开
                activity.mAdapter.enable();

            }
        }

    }

    public static boolean turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.enable();
        }
        return false;
    }

    public static boolean hasBindBlueTooth() {
        String name = PrintUtil.getDefaultBluetoothDeviceName(App.getContext());
        return !TextUtils.isEmpty(name);
    }
}
