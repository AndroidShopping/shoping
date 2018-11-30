package comm.shop.shopping.ui;

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
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.utils.TextUtils;

public class PrintOrderActivity extends BaseAcivity {
    public static final String RESULT = "result";
    public static final String ORDER_ID = "orderId";
    public static final String REPAY = "repay";
    public static final int TOTAL_SECOND = 30 * 1000;
    public static final int INTERVAL_SECON = 1 * 1000;
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
}
