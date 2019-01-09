package com.shop.shopping.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.imageloader.ILFactory;
import cn.droidlover.xdroidmvp.imageloader.ILoader;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;
import com.shop.shopping.model.OrderResult;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.present.SubmitPresenter;
import com.shop.shopping.utils.TextUtils;
import com.shop.shopping.utils.ToastUtils;

public class SubmitActivity extends BaseAcivity<SubmitPresenter> {
    public static final String RESULT = "result";
    @BindView(R.id.goods_recycleView)
    RecyclerView goodsRecycleView;
    @BindView(R.id.submit_view)
    Button submitView;
    ShopResult shopResult;

    public static void start(Context context, ShopResult result) {
        Intent intent = new Intent(context, SubmitActivity.class);
        intent.putExtra(RESULT, result);
        context.startActivity(intent);

    }


    public void showError(NetError error) {
        ToastUtils.show(error.getMessage());
    }

    public void showData(OrderResult orderResult) {
        if (shopResult.isBizError()) {
            ToastUtils.show(shopResult.getErrorMsg());
        } else {
//            SubmitCoinActivity.start(SubmitActivity.this, shopResult, orderResult.getData().getOrderId());
            SubmitCoinActivity.start(SubmitActivity.this, shopResult, "3333");


        }
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        goodsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        goodsRecycleView.setAdapter(new MyAdapter());
        shopResult = getIntent().getParcelableExtra(RESULT);
        String string = TextUtils.getString(R.string.go_pay);
        SpannableStringBuilder builder = new SpannableStringBuilder(string + " ");
        int length = builder.length();
        builder.append(TextUtils.getPriceText(shopResult.getAllSelectPrice()));
        builder.setSpan(new AbsoluteSizeSpan(16, true), length, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        submitView.setText(builder);
        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SubmitPresenter) getP()).createPrevieOrder(shopResult.getAllSelectPrice(), shopResult.getSelectedItem());
            }
        });

        final CommonTitleBar titleBar = (CommonTitleBar) findViewById(R.id.titlebar);
        titleBar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_BUTTON
                        || action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    onBackPressed();
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_submit;
    }

    @Override
    public SubmitPresenter newP() {
        return new SubmitPresenter();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.good_pic_view)
        ImageView goodPicView;
        @BindView(R.id.category_view)
        TextView categoryView;
        @BindView(R.id.biao_zhun_view)
        TextView biaoZhunView;
        @BindView(R.id.count_view)
        TextView countView;
        @BindView(R.id.all_price_view)
        TextView allPriceView;


        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submit, parent, false);
            return new MyViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ShopItem item = shopResult.getItem(position);
            ILoader.Options options = new ILoader.Options(R.drawable.loading, R.drawable.loading_error);
            ILFactory.getLoader().loadNet(holder.goodPicView, item.getPicPath(), options);
            holder.allPriceView.setText(item.formatAllCost());
            holder.countView.setText("X" + item.getBuyCount());
            String categoryName = shopResult.findCategoryName(item);
            holder.categoryView.setText(item.getName() + "-" + categoryName);
            holder.biaoZhunView.setText(item.getStandData());
        }

        @Override
        public int getItemCount() {
            return shopResult.getBuyedKindOfGoodCount();
        }
    }

}
