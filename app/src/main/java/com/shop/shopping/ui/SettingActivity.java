package com.shop.shopping.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.cache.SharedPref;
import cn.droidlover.xdroidmvp.shopping.R;
import com.shop.shopping.App;
import com.shop.shopping.boothprint.util.ToastUtil;
import com.shop.shopping.model.Keys;
import com.shop.shopping.utils.PatternUtils;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.ip_tip)
    TextView ipTip;
    @BindView(R.id.tv_ip)
    EditText tvIp;
    @BindView(R.id.port_tip)
    TextView portTip;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.ok_view)
    TextView okView;

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingActivity.class);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        SharedPref instance = SharedPref.getInstance(this);
        String ip = instance.getString(Keys.IP, "");
        String port = instance.getString(Keys.PORT, "");
        tvIp.setText(ip);
        tvIp.setSelection(ip.length());
        etPort.setText(port);
        etPort.setSelection(port.length());
        okView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = tvIp.getText().toString();
                boolean isIp = PatternUtils.isIP(ip);
                if (!isIp) {
                    ToastUtil.showToast(SettingActivity.this, R.string.ip_unvalid);
                    return;
                }
                String port = etPort.getText().toString();
                if (PatternUtils.isPort(port)) {
                    ToastUtil.showToast(SettingActivity.this, R.string.port_unvalid);
                    return;
                }
                SharedPref.getInstance(App.getContext()).putString(Keys.IP, ip);
                SharedPref.getInstance(App.getContext()).putString(Keys.PORT, port);
                finish();

            }
        });
    }


}
