package comm.shop.shopping.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.model.ShopCategory;
import comm.shop.shopping.model.ShopItem;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.utils.TextUtils;

public class ShopPopupWindow extends AlertDialog {
    RecyclerView recyclerView;

    public ShopPopupWindow(final ShopResult result, Context context) {
        super(context);
        View inflate = LayoutInflater.from(context).inflate(R.layout.shop_pop_up_window, null);
        inflate.findViewById(R.id.del_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.clearAllSelectItem();
                EventBus.getDefault().post(result);
                dismiss();
            }
        });
        recyclerView = inflate.findViewById(R.id.recycler_view);
        final MyAdapter adapter = new MyAdapter(result);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.isNoGoodToBuy()) {
                    dismiss();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        setContentView(inflate);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolderView> {
        private final ShopResult shopResult;


        public MyAdapter(ShopResult shopResult) {
            this.shopResult = shopResult;
        }

        public boolean isNoGoodToBuy() {
            return shopResult.getAllBuyedGoodCount() == 0;
        }

        public ShopItem getItem(int position) {
            List<ShopCategory> data = shopResult.getData();
            int count = 0;
            for (ShopCategory datum : data) {
                for (ShopItem shopItem : datum.getShopItem()) {
                    int buyCount = shopItem.getBuyCount();
                    if (buyCount != 0) {
                        if (count == position) {
                            return shopItem;
                        } else {
                            count++;
                        }
                    }
                }
            }
            return null;
        }

        @NonNull
        @Override
        public MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_pop_up_item, parent, false);
            return new MyHolderView(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolderView holder, int position) {
            final ShopItem item = getItem(position);
            holder.nameView.setText(item.getName());
            holder.moneyView.setText(TextUtils.getPriceText(item.getPrice()));
            holder.tvGoodsSelectNum.setText(item.getBuyCount() + "");
            holder.ivGoodsAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setBuyCount(item.getBuyCount() + 1);
                    notifyDataSetChanged();
                    EventBus.getDefault().post(shopResult);
                }
            });
            holder.ivGoodsAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setBuyCount(item.getBuyCount() - 1);
                    notifyDataSetChanged();
                    EventBus.getDefault().post(shopResult);
                }
            });

        }

        @Override
        public int getItemCount() {
            return shopResult.getBuyedKindOfGoodCount();
        }
    }

    static class MyHolderView extends RecyclerView.ViewHolder {
        @BindView(R.id.name_view)
        TextView nameView;
        @BindView(R.id.money_view)
        TextView moneyView;
        @BindView(R.id.ivGoodsAdd)
        ImageView ivGoodsAdd;
        @BindView(R.id.tvGoodsSelectNum)
        TextView tvGoodsSelectNum;
        @BindView(R.id.ivGoodsMinus)
        ImageView ivGoodsMinus;

        public MyHolderView(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }
    }

}
