package com.shop.shopping;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyPayService extends IntentService {

    private boolean needStart = true;

    public MyPayService() {
        super("MyPayService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        while (needStart) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        needStart = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final PayService.Stub mBinder = new PayService.Stub() {
        @Override
        public void startPay(long moneyCount, long timeout, PayCallback callback) throws RemoteException {

        }

        @Override
        public int queryDeviceState() throws RemoteException {
            return 0;
        }
    };
}
