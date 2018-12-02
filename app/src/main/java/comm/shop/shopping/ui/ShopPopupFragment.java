package comm.shop.shopping.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.model.ShopItem;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.utils.TextUtils;

@SuppressLint("ValidFragment")
public class ShopPopupFragment extends DialogFragment {

    private final MainActivity activity;
    private ShopResult result;

    public ShopPopupFragment(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setShopResult(ShopResult result) {
        this.result = result;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.shop_pop_up_window, null);
        inflate.findViewById(R.id.del_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.clearAllSelectItem();
                activity.onEvent(result);
                dismiss();
            }
        });
        RecyclerView recyclerView = inflate.findViewById(R.id.recycler_view);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        builder.setView(inflate);
        return builder.create();
    }


    private class MyAdapter extends RecyclerView.Adapter<MyHolderView> {
        private final ShopResult shopResult;


        public MyAdapter(ShopResult shopResult) {
            this.shopResult = shopResult;
        }

        public boolean isNoGoodToBuy() {
            return shopResult.getAllBuyedGoodCount() == 0;
        }


        @NonNull
        @Override
        public MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_pop_up_item, parent, false);
            return new MyHolderView(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolderView holder, int position) {
            final ShopItem item = shopResult.getItem(position);
            holder.nameView.setText(item.getName());
            holder.moneyView.setText(TextUtils.getPriceText(item.getPrice()));
            holder.tvGoodsSelectNum.setText(item.getBuyCount() + "");
            holder.ivGoodsAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setBuyCount(item.getBuyCount() + 1);
                    notifyDataSetChanged();
                    activity.onEvent(shopResult);
                }
            });
            holder.ivGoodsMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setBuyCount(item.getBuyCount() - 1);
                    notifyDataSetChanged();
                    activity.onEvent(shopResult);
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
            ButterKnife.bind(this, itemView);
        }
    }

}
