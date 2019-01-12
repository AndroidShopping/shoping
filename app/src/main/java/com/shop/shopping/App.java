package com.shop.shopping;

import android.app.Application;
import android.content.Context;
import cn.droidlover.xdroidmvp.net.LogInterceptor;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.NetProvider;
import cn.droidlover.xdroidmvp.net.RequestHandler;
import cn.droidlover.xdroidmvp.net.XApi;
import com.shop.shopping.boothprint.base.AppInfo;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by wanglei on 2016/12/31.
 */

public class App extends Application {


    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AppInfo.init(getApplicationContext());
        context = this;
        XApi.registerProvider(new NetProvider() {
            @Override
            public Interceptor[] configInterceptors() {
                Interceptor[] interceptors = new Interceptor[1];
                interceptors[0]= new LogInterceptor();
                return interceptors;
            }

            @Override
            public void configHttps(OkHttpClient.Builder builder) {

            }

            @Override
            public CookieJar configCookie() {
                return null;
            }

            @Override
            public RequestHandler configHandler() {
                return null;
            }

            @Override
            public long configConnectTimeoutMills() {
                return 10000;
            }

            @Override
            public long configReadTimeoutMills() {
                return 10000;
            }

            @Override
            public boolean configLogEnable() {
                return true;
            }

            @Override
            public boolean handleError(NetError error) {
                return true;
            }

            @Override
            public boolean dispatchProgressEnable() {
                return true;
            }
        });

    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onTerminate() {
        //程序终止关闭串口
//        ICT104.closeSerialPort();
        super.onTerminate();
    }

}
