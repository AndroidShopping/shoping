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
import com.shop.shopping.boothprint.util.ToastUtil;
import com.shop.shopping.entity.PayState;
import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.present.ConfirmPresenter;
import com.shop.shopping.utils.ToastUtils;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.lang.ref.WeakReference;

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
    private boolean hasClick = false;
    private int payMoneyCount;
    /**
     * 是否因为网络原因或者服务器错误而退币
     */
    private boolean havePayOutBeacuseServerError;

    private static final class H extends Handler {
        WeakReference<SubmitCoinActivity> activity;

        public H(SubmitCoinActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.activity == null || this.activity.get() == null) {
                return;
            }
            SubmitCoinActivity submitCoinActivity = activity.get();
            int what = msg.what;
            switch (what) {
                case PayState.START_PAY:
                    break;
                case PayState.PAY_ERROR:
                    ToastUtil.showToast(submitCoinActivity, R.string.chu_bi_shi_bai);
                    break;
                case PayState.PAY_OK:
                    Intent intent = submitCoinActivity.getIntent();
                    submitCoinActivity.getP().confirmOrder(intent.getStringExtra(ORDER_ID));

                default:
                    break;

            }
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
                if (hasClick) {
                    ToastUtil.showToast(SubmitCoinActivity.this, R.string.have_click);
                    return;
                }
                hasClick = true;
                Intent intent = getIntent();
                ShopResult result = intent.getParcelableExtra(RESULT);
                payMoneyCount = result.getAllSelectPrice();
                myPayService.doPayFor(payMoneyCount);
            }
        });


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
                myPayService.setHandler(new H(SubmitCoinActivity.this));
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
        unbindService(conn);
    }

    public void onStartLoading() {
    }

    public void showError(NetError error) {
        if (!havePayOutBeacuseServerError) {
            havePayOutBeacuseServerError = true;
            myPayService.doPayOut(payMoneyCount);
        }
    }

    public void showData(ConfirmOrderResult confirmOrderResult) {

        if (confirmOrderResult.isBizError()) {
            ToastUtils.show(confirmOrderResult.message);
            if (!havePayOutBeacuseServerError) {
                havePayOutBeacuseServerError = true;
                myPayService.doPayOut(payMoneyCount);
            }
            return;
        }
        Intent intent = getIntent();
        ShopResult result = intent.getParcelableExtra(RESULT);
        PrintOrderActivity.start(SubmitCoinActivity.this,
                result, intent.getStringExtra(ORDER_ID), 123);
    }


}
