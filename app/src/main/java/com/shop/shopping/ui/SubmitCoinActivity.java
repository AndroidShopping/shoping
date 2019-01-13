package com.shop.shopping.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.shop.shopping.MyPayService;
import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.present.ConfirmPresenter;
import com.shop.shopping.utils.ToastUtils;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;

public class SubmitCoinActivity extends BaseAcivity<ConfirmPresenter> {
    public static final String RESULT = "result";
    public static final String ORDER_ID = "orderId";
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.submit_view)
    Button submitView;
    private ServiceConnection conn;
    private MyPayService myPayService;
    private H handler;

    private static final class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    public static void start(Context context, ShopResult result, String orderId) {
        Intent intent = new Intent(context, SubmitCoinActivity.class);
        intent.putExtra(RESULT, result);
        intent.putExtra(ORDER_ID, orderId);
        context.startActivity(intent);

    }

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
        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        Intent intent = getIntent();
        getP().confirmOrder(intent.getStringExtra(ORDER_ID));

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_submit_coin;
    }

    @Override
    public ConfirmPresenter newP() {
        return new ConfirmPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myPayService = ((MyPayService.PayBinder) service).getService();
                myPayService.setHandler(new H());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myPayService = null;
            }
        };
        bindService(new Intent(this, MyPayService.class), conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myPayService.setHandler(null);
        handler = null;
        unbindService(conn);
    }

    public void onStartLoading() {
    }

    public void showError(NetError error) {
    }

    public void showData(ConfirmOrderResult confirmOrderResult) {

        if (confirmOrderResult.isBizError()) {
            ToastUtils.show(confirmOrderResult.message);
            return;
        }
        Intent intent = getIntent();
        ShopResult result = intent.getParcelableExtra(RESULT);
        PrintOrderActivity.start(SubmitCoinActivity.this,
                result, intent.getStringExtra(ORDER_ID), 123);
    }


}
