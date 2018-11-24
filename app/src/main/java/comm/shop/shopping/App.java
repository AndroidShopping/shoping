package comm.shop.shopping;

import android.app.Application;
import android.content.Context;

import android_serialport_api.SerialPortFinder;
import android_serialport_api.SerialPortUtil;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.NetProvider;
import cn.droidlover.xdroidmvp.net.RequestHandler;
import cn.droidlover.xdroidmvp.net.XApi;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by wanglei on 2016/12/31.
 */

public class App extends Application {


    private static Context context;
    private SerialPortUtil ICT104;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SerialPortFinder finder = new SerialPortFinder();
        String[] allDevicesPath = finder.getAllDevicesPath();
        ICT104 = new SerialPortUtil();
        if (allDevicesPath != null) {
            ICT104.initSerialPort(allDevicesPath[0], SerialPortUtil.DEFAULT_BO_TE, 0);
        } else {
            ICT104.initSerialPort("/dev/ttyS4", SerialPortUtil.DEFAULT_BO_TE, 0);
        }


        XApi.registerProvider(new NetProvider() {

            @Override
            public Interceptor[] configInterceptors() {
                return new Interceptor[0];
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
                return 0;
            }

            @Override
            public long configReadTimeoutMills() {
                return 0;
            }

            @Override
            public boolean configLogEnable() {
                return true;
            }

            @Override
            public boolean handleError(NetError error) {
                return false;
            }

            @Override
            public boolean dispatchProgressEnable() {
                return false;
            }
        });
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onTerminate() {
        //程序终止关闭串口
        ICT104.closeSerialPort();
        super.onTerminate();
    }

    //获取SerialPortUtil
    public SerialPortUtil getSerialPorICT104() {
        return ICT104;
    }
}
