package comm.shop.shopping.adapter.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import cn.droidlover.xdroidmvp.event.BusProvider;
import cn.droidlover.xdroidmvp.imageloader.ILFactory;
import cn.droidlover.xdroidmvp.imageloader.ILoader;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.model.ShopItem;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.stickyheadergrid.StickyHeaderGridAdapter;
import comm.shop.shopping.utils.TextUtils;

public class GoodAdapter extends StickyHeaderGridAdapter {

    private ShopResult shopResult;
    private Context mContext;
    private Activity mActivity;
    private ImageView buyImg;

    public GoodAdapter(Context context, ShopResult shopResult) {
        this.mContext = context;
        this.shopResult = shopResult;
        setHasStableIds(true);

    }


    @Override
    public int getSectionItemCount(int section) {
        return shopResult.getData().get(section).getShopItem().size();
    }

    @Override
    public int getSectionCount() {
        if (shopResult == null) {
            return 0;
        }
        return shopResult.getData().size();
    }


    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 开始动画
     *
     * @param view
     */
    private void startAnim(View view) {
        buyImg = new ImageView(mActivity);
        buyImg.setBackgroundResource(R.mipmap.icon_goods_add_item);// 设置buyImg的图片
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        int[] startLocation = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        view.getLocationInWindow(startLocation);// 这是获取购买按钮的在屏幕的X、Y坐标（这也是动画开始的坐标）
        ((comm.shop.shopping.ui.MainActivity) mActivity).setAnim(buyImg, startLocation);// 开始执行动画
    }

    /**
     * 判断商品是否有添加到购物车中
     *
     * @param i  条目下标
     * @param vh ViewHolder
     */
    private void isSelected(int i, ViewHolder vh) {
        if (i == 0) {
            vh.tvGoodsSelectNum.setVisibility(View.GONE);
            vh.ivGoodsMinus.setVisibility(View.GONE);
        } else {
            vh.tvGoodsSelectNum.setVisibility(View.VISIBLE);
            vh.tvGoodsSelectNum.setText(i + "");
            vh.ivGoodsMinus.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public MyHeadViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_goods_list, parent, false);
        return new MyHeadViewHolder(itemView);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goods_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindHeaderViewHolder(StickyHeaderGridAdapter.HeaderViewHolder headerViewHolder, int section) {
        MyHeadViewHolder headerViewHolder1 = (MyHeadViewHolder) headerViewHolder;
        headerViewHolder1.tvGoodsItemTitle.setText(shopResult.getData().get(section).getName());

    }


    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, final int section, int offset) {
        final ViewHolder holder = (ViewHolder) viewHolder;


        final ShopItem shopItem = shopResult.getData().get(section).getShopItem().get(offset);
        //设置名
        holder.goodsCategoryName.setText(shopItem.getName());
        //设置价格
        holder.tvGoodsPrice.setText(TextUtils.getPriceText(shopItem.getPrice()));

        ILoader.Options options = new ILoader.Options(R.drawable.loading, R.drawable.loading_error);
        ILFactory.getLoader().loadNet(holder.ivGoodsImage, shopItem.getPicPath(), options);

        //通过判别对应位置的数量是否大于0来显示隐藏数量
        isSelected(shopItem.getBuyCount(), holder);
        //加号按钮点击
        holder.ivGoodsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopItem.setBuyCount(shopItem.getBuyCount() + 1);

                if (shopItem.getBuyCount() <= 1) {
                    holder.ivGoodsMinus.setAnimation(getShowAnimation());
                    holder.tvGoodsSelectNum.setAnimation(getShowAnimation());
                    holder.ivGoodsMinus.setVisibility(View.VISIBLE);
                    holder.tvGoodsSelectNum.setVisibility(View.VISIBLE);
                }
                startAnim(holder.ivGoodsAdd);
                isSelected(shopItem.getBuyCount(), holder);
                BusProvider.getBus().post(shopResult);

            }
        });
        //减号点击按钮点击
        holder.ivGoodsMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shopItem.getBuyCount() > 0) {
                    shopItem.setBuyCount(shopItem.getBuyCount() - 1);
                    isSelected(shopItem.getBuyCount(), holder);
                    if (shopItem.getBuyCount() <= 0) {
                        holder.ivGoodsMinus.setAnimation(getHiddenAnimation());
                        holder.tvGoodsSelectNum.setAnimation(getHiddenAnimation());
                        holder.ivGoodsMinus.setVisibility(View.GONE);
                        holder.tvGoodsSelectNum.setVisibility(View.GONE);
                    }
                    BusProvider.getBus().post(shopResult);
                }
            }
        });


    }

    public void updateShopResult(ShopResult shopResult) {

        this.shopResult = shopResult;
        notifyAllSectionsDataSetChanged();
    }


    public static class MyHeadViewHolder extends HeaderViewHolder {
        TextView tvGoodsItemTitle;

        public MyHeadViewHolder(View itemView) {
            super(itemView);
            tvGoodsItemTitle = itemView.findViewById(R.id.tvGoodsItemTitle);
        }
    }


    /**
     * 显示减号的动画
     *
     * @return
     */
    private Animation getShowAnimation() {
        AnimationSet set = new AnimationSet(true);
        RotateAnimation rotate = new RotateAnimation(0, 720, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(rotate);
        TranslateAnimation translate = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 2f
                , TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 0);
        set.addAnimation(translate);
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        set.addAnimation(alpha);
        set.setDuration(500);
        return set;
    }


    /**
     * 隐藏减号的动画
     *
     * @return
     */
    private Animation getHiddenAnimation() {
        AnimationSet set = new AnimationSet(true);
        RotateAnimation rotate = new RotateAnimation(0, 720, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(rotate);
        TranslateAnimation translate = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 4f
                , TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 0);
        set.addAnimation(translate);
        AlphaAnimation alpha = new AlphaAnimation(1, 0);
        set.addAnimation(alpha);
        set.setDuration(500);
        return set;
    }

    public interface OnShopCartGoodsChangeListener {
        void onNumChange();
    }

    public static class ViewHolder extends ItemViewHolder {

        public final ImageView ivGoodsImage;
        public final TextView goodsCategoryName;
        public final TextView tvGoodsPrice;
        public final ImageView ivGoodsMinus;
        public final TextView tvGoodsSelectNum;
        public final ImageView ivGoodsAdd;
        public final View root;

        public ViewHolder(View root) {
            super(root);
            ivGoodsImage = root.findViewById(R.id.ivGoodsImage);
            goodsCategoryName = root.findViewById(R.id.goodsCategoryName);
            tvGoodsPrice = root.findViewById(R.id.tvGoodsPrice);
            ivGoodsMinus = root.findViewById(R.id.ivGoodsMinus);
            tvGoodsSelectNum = root.findViewById(R.id.tvGoodsSelectNum);
            ivGoodsAdd = root.findViewById(R.id.ivGoodsAdd);
            this.root = root;
        }
    }


}
