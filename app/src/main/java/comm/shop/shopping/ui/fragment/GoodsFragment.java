package comm.shop.shopping.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import cn.droidlover.xdroidmvp.mvp.XLazyFragment;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.adapter.adapter.PersonAdapter;
import comm.shop.shopping.adapter.adapter.RecycleGoodsCategoryListAdapter;
import comm.shop.shopping.event.GoodsListEvent;
import comm.shop.shopping.model.ShopCategory;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.present.PShopPresenter;
import comm.shop.shopping.stickyheadergrid.StickyHeaderGridLayoutManager;
import comm.shop.shopping.utils.DensityUtil;
import comm.shop.shopping.utils.ToastUtils;


/**
 * 商品
 */
public class GoodsFragment extends XLazyFragment<PShopPresenter> {

    @BindView(R.id.goods_category_list)
    RecyclerView mGoodsCateGoryList;
    private RecycleGoodsCategoryListAdapter mGoodsCategoryListAdapter;
    @BindView(R.id.goods_recycleView)
    RecyclerView recyclerView;
    private PersonAdapter personAdapter;
    private StickyHeaderGridLayoutManager gridLayoutManager;
    public static final int DEFAULT_ITEM_INTVEL = 10;


    private void initData(final ShopResult shopResult) {

        mGoodsCategoryListAdapter = new RecycleGoodsCategoryListAdapter(shopResult, getActivity());
        mGoodsCateGoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        mGoodsCateGoryList.setAdapter(mGoodsCategoryListAdapter);
        final List<ShopCategory> data = shopResult.getData();
        mGoodsCategoryListAdapter.setOnItemClickListener(new RecycleGoodsCategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int count = 0;
                for (int i = 0; i < position; i++) {
                    count += data.get(i).getShopItem().size();
                }

                recyclerView.smoothScrollToPosition(count + 1);
                mGoodsCategoryListAdapter.setCheckPosition(position);
            }
        });

        gridLayoutManager = new StickyHeaderGridLayoutManager(2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerItemDecoration(DensityUtil.dp2px(getContext(), DEFAULT_ITEM_INTVEL), 2));
        personAdapter = new PersonAdapter(getActivity(), shopResult);
        personAdapter.setmActivity(getActivity());
        recyclerView.setAdapter(personAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleHeaderPosition = gridLayoutManager.getFirstVisibleHeaderPosition(true);
                mGoodsCategoryListAdapter.setCheckPosition(firstVisibleHeaderPosition);

            }
        });
        personAdapter.setOnShopCartGoodsChangeListener(new PersonAdapter.OnShopCartGoodsChangeListener() {
            @Override
            public void onNumChange() {
                mGoodsCategoryListAdapter.notifyDataSetChanged();
            }
        });


    }


    /**
     * 添加 或者  删除  商品发送的消息处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoodsListEvent event) {
        mGoodsCategoryListAdapter.notifyDataSetChanged();
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        getP().getShopProductList();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_goods;
    }

    @Override
    public PShopPresenter newP() {
        return new PShopPresenter();
    }

    public void showData(ShopResult shopResult) {
        if (shopResult.isBizError()) {
            ToastUtils.show(shopResult.getMessage());
            return;
        }
        initData(shopResult);
    }

    public static class RecyclerItemDecoration extends RecyclerView.ItemDecoration {
        private int itemSpace;
        private int itemNum;

        /**
         * @param itemSpace item间隔
         * @param itemNum   每行item的个数
         */
        public RecyclerItemDecoration(int itemSpace, int itemNum) {
            this.itemSpace = itemSpace;
            this.itemNum = itemNum;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = itemSpace;
            outRect.left = itemSpace;
            if (parent.getChildLayoutPosition(view) % itemNum == 0) {
                outRect.right = itemSpace;
            } else {
            }
        }

    }

    public void showError(NetError error) {
        if (error != null) {
//            switch (error.getType()) {
//                case NetError.ParseError:
//                    errorView.setMsg("数据解析异常");
//                    break;
//
//                case NetError.AuthError:
//                    errorView.setMsg("身份验证异常");
//                    break;
//
//                case NetError.BusinessError:
//                    errorView.setMsg("业务异常");
//                    break;
//
//                case NetError.NoConnectError:
//                    errorView.setMsg("网络无连接");
//                    break;
//
//                case NetError.NoDataError:
//                    errorView.setMsg("数据为空");
//                    break;
//
//                case NetError.OtherError:
//                    errorView.setMsg("其他异常");
//                    break;
//            }
//            contentLayout.showError();
        }
    }

}
