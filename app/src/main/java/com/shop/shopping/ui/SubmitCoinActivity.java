package com.shop.shopping.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shop.shopping.MyPayService;
import com.shop.shopping.boothprint.util.ToastUtil;
import com.shop.shopping.entity.PayState;
import com.shop.shopping.model.ConfirmOrderResult;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.present.ConfirmPresenter;
import com.shop.shopping.utils.TextUtils;
import com.shop.shopping.utils.ToastUtils;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;

import static com.shop.shopping.entity.PayState.PAY_LAST_ERROR;
import static com.shop.shopping.entity.PayState.PAY_RECEIVE_MONEY;

public class SubmitCoinActivity extends BaseAcivity<ConfirmPresenter> {
    public static final String RESULT = "result";
    public static final String ORDER_ID = "orderId";
    public static final String PRINT_NUMBER = "print_number";
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.submit_view)
    Button submitView;
    @BindView(R.id.zong_tou_bi_view)
    TextView zongTouBiView;
    @BindView(R.id.current_shou_bi_view)
    TextView currentShouBiView;
    @BindView(R.id.cancel_view)
    Button cancelView;
    private ServiceConnection conn;
    private MyPayService myPayService;
    private boolean hasClick = false;
    private int payMoneyCount;
    private int haveReceive = 0;
    private int haveTuiBi = 0;
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
                    submitCoinActivity.haveReceive = msg.arg1;
                    submitCoinActivity.haveTuiBi = msg.arg2;
                    Intent intent = submitCoinActivity.getIntent();
                    submitCoinActivity.getP().confirmOrder(intent.getStringExtra(ORDER_ID));
                    break;
                case PAY_LAST_ERROR:
                    ToastUtil.showToast(submitCoinActivity, R.string.chu_bi_shi_bai);
                case PAY_RECEIVE_MONEY:
                    submitCoinActivity.currentShouBiView.setText(String.format("%s%s",
                            submitCoinActivity.getString(R.string.ying_shou_view),
                            TextUtils.getPriceText((Integer) msg.obj)));
                    break;
                default:
                    break;

            }
        }
    }

    public static void start(Context context, ShopResult result, String id, String printNumber) {
        Intent intent = new Intent(context, SubmitCoinActivity.class);
        intent.putExtra(RESULT, result);
        intent.putExtra(ORDER_ID, id);
        intent.putExtra(PRINT_NUMBER, printNumber);
        context.startActivity(intent);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tip).setMessage(R.string.quit).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (havePayOutBeacuseServerError) {
                    return;
                }
                havePayOutBeacuseServerError = true;
                myPayService.doCancel();
                finish();

            }
        }).setNegativeButton(R.string.no, null);
        builder.show();
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        titlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_BUTTON
                        || action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    showDialog();
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
                myPayService.doPayFor(payMoneyCount);
            }
        });

        zongTouBiView.setText(String.format("%s%s", getString(R.string.pay_count_number_view), TextUtils.getPriceText(payMoneyCount)));
        currentShouBiView.setText(String.format("%s%s", getString(R.string.ying_shou_view), TextUtils.getPriceText(0)));
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
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
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ShopResult result = intent.getParcelableExtra(RESULT);
        payMoneyCount = result.getAllSelectPrice();
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
                myPayService.doPayOut(haveReceive);
            }
            return;
        }
        Intent intent = getIntent();
        ShopResult result = intent.getParcelableExtra(RESULT);
        PrintOrderActivity.start(SubmitCoinActivity.this,
                result, intent.getStringExtra(PRINT_NUMBER), haveReceive, haveTuiBi);
        finish();
    }


}
