package comm.shop.shopping.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.model.ShopResult;

public class SubmitCoinActivity extends BaseAcivity {
    public static final String RESULT = "result";
    public static final String ORDER_ID = "orderId";
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.submit_view)
    Button submitView;

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
                Intent intent = getIntent();
                ShopResult result = intent.getParcelableExtra(RESULT);
                PrintOrderActivity.start(SubmitCoinActivity.this,
                        result,intent.getStringExtra(ORDER_ID),123);

            }
        });

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_submit_coin;
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
