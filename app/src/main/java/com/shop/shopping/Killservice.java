package com.shop.shopping;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.shop.shopping.ui.MainActivity;

public class Killservice extends Service {
    public Killservice() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        startActivity(new Intent(this, MainActivity.class));
        stopSelf();
    }
}
