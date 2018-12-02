package comm.shop.shopping.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.rxbus.RxBus;
import com.google.gson.Gson;

import butterknife.BindView;
import cn.droidlover.xdroidmvp.event.BusProvider;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.adapter.adapter.GoodAdapter;
import comm.shop.shopping.adapter.adapter.RecycleGoodsCategoryListAdapter;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.present.PShopPresenter;
import comm.shop.shopping.stickyheadergrid.StickyHeaderGridLayoutManager;
import comm.shop.shopping.utils.DensityUtil;
import comm.shop.shopping.utils.TextUtils;
import comm.shop.shopping.utils.ToastUtils;

public class MainActivity extends BaseAcivity<PShopPresenter> {


    @BindView(R.id.main_picture_view)
    ImageView mainPictureView;
    @BindView(R.id.refresh_view)
    TextView refreshView;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.shopCartNum)
    TextView shopCartNum;
    @BindView(R.id.totalPrice)
    TextView totalPrice;
    @BindView(R.id.noShop)
    TextView noShop;
    @BindView(R.id.go_cal)
    View goCal;
    @BindView(R.id.shopCartMain)
    RelativeLayout shopCartMain;
    private ViewGroup anim_mask_layout;
    ShopPopupFragment myFragment;

    @BindView(R.id.goods_category_list)
    RecyclerView mGoodsCateGoryList;
    private RecycleGoodsCategoryListAdapter mGoodsCategoryListAdapter;
    @BindView(R.id.goods_recycleView)
    RecyclerView recyclerView;
    private GoodAdapter goodAdapter;
    private StickyHeaderGridLayoutManager gridLayoutManager;
    public static final int DEFAULT_ITEM_INTVEL = 10;
    private ShopResult result;
    private int mIndex;
    private boolean move;
    private boolean isMoved;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void initData(Bundle savedInstanceState) {
        initView();
        initData();
        setStatusBar();
        myFragment = new ShopPopupFragment();
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFragment.setShopResult(result);
                myFragment.showNow(getSupportFragmentManager(), "dialog_show");
            }
        });

        goCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitActivity.start(MainActivity.this, result);
            }
        });
        getP().getShopProductList();
    }


    @Override
    public void bindEvent() {
        BusProvider.getBus().subscribe(this, new RxBus.Callback<ShopResult>() {
            @Override
            public void onEvent(ShopResult event) {
                mGoodsCategoryListAdapter.notifyDataSetChanged();
                goodAdapter.notifyDataSetChanged();

                if (event != null) {
                    int count = event.getAllSelectCount();
                    if (count > 0) {
                        shopCartNum.setText(String.valueOf(count));
                        shopCartNum.setVisibility(View.VISIBLE);
                        totalPrice.setVisibility(View.VISIBLE);
                        noShop.setVisibility(View.GONE);
//                        goCal.setEnabled(true);
                        image.setClickable(true);
                    } else {
                        shopCartNum.setVisibility(View.GONE);
                        totalPrice.setVisibility(View.GONE);
                        noShop.setVisibility(View.VISIBLE);
//                        goCal.setEnabled(false);
                        image.setClickable(false);
                    }
                    totalPrice.setText(TextUtils.getPriceText(event.getAllSelectPrice()));

                }
            }
        });
    }

    private void initData() {

        mGoodsCategoryListAdapter = new RecycleGoodsCategoryListAdapter(null, this);
        linearLayoutManager = new LinearLayoutManager(this);
        mGoodsCateGoryList.setLayoutManager(linearLayoutManager);
        mGoodsCateGoryList.setAdapter(mGoodsCategoryListAdapter);
        mGoodsCategoryListAdapter.setOnItemClickListener(new RecycleGoodsCategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                isMoved = true;
                int targetPosition = position;
                setChecked(position, true);
            }
        });
        gridLayoutManager = new StickyHeaderGridLayoutManager(2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerItemDecoration(DensityUtil.dp2px(this, DEFAULT_ITEM_INTVEL), 2));
        goodAdapter = new GoodAdapter(this, null);
        goodAdapter.setmActivity(this);
        recyclerView.setAdapter(goodAdapter);
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


    }

    private void setChecked(int position, boolean isLeft) {
        Log.d("p-------->", String.valueOf(position));
        if (isLeft) {
            mGoodsCategoryListAdapter.setCheckPosition(position);
            //此处的位置需要根据每个分类的集合来进行计算
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += result.getData().get(i).getShopItem().size();
            }
            count += position;
            setData(count);
//            ItemHeaderDecoration.setCurrentTag(String.valueOf(targetPosition));//凡是点击左边，将左边点击的位置作为当前的tag
        } else {
            if (isMoved) {
                isMoved = false;
            } else {
                mGoodsCategoryListAdapter.setCheckPosition(position);
            }
//            ItemHeaderDecoration.setCurrentTag(String.valueOf(position));//如果是滑动右边联动左边，则按照右边传过来的位置作为tag

        }
        moveToCenter(position);

    }

    //将当前选中的item居中
    private void moveToCenter(int position) {
        //将点击的position转换为当前屏幕上可见的item的位置以便于计算距离顶部的高度，从而进行移动居中
        View childAt = mGoodsCateGoryList.getChildAt(position - linearLayoutManager.findFirstVisibleItemPosition());
        if (childAt != null) {
            int y = (childAt.getTop() - mGoodsCateGoryList.getHeight() / 2);
            mGoodsCateGoryList.smoothScrollBy(0, y);
        }

    }

    public void setData(int n) {
        mIndex = n;
        recyclerView.stopScroll();
        smoothMoveToPosition(n);
    }

    private void smoothMoveToPosition(int n) {
        int firstItem = gridLayoutManager.getFirstVisibleItemPosition(true);
        int lastItem = gridLayoutManager.getLastVisibleItemPosition();

        Log.d("first--->", String.valueOf(firstItem));
        Log.d("last--->", String.valueOf(lastItem));
        if (n <= firstItem) {
            recyclerView.scrollToPosition(n);
        } else if (n <= lastItem) {
            Log.d("pos---->", String.valueOf(n) + "VS" + firstItem);
            int top = recyclerView.getChildAt(n - firstItem).getTop();
            Log.d("top---->", String.valueOf(top));
            recyclerView.scrollBy(0, top);
        } else {
            recyclerView.scrollToPosition(n);
            move = true;
        }
    }


    public void onStartLoading() {

    }


    public void refresh() {
        getP().getShopProductList();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
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
        Gson gson = new Gson();
        try {
            String string = "{\n" +
                    "  \"status\": 0,\n" +
                    "  \"message\": \"succes\",\n" +
                    "  \"data\": [\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶1\",\n" +
                    "      \"description\": \"奶茶2\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶2\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶3\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶4\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶5\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶6\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶7\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶8\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶9\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶10\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶11\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶12\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶13\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶14\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3233,\n" +
                    "      \"name\": \"奶茶15\",\n" +
                    "      \"description\": \"奶茶\",\n" +
                    "      \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "      \"shopItem\": [\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"6奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"5奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"4奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"3奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"2奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"1奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            shopResult = gson.fromJson(string, ShopResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.result = shopResult;
        mGoodsCategoryListAdapter.updateShopResult(shopResult);
        goodAdapter.updateShopResult(shopResult);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refresh();

    }


    private void initView() {
        shopCartMain = findViewById(R.id.shopCartMain);
        shopCartNum = findViewById(R.id.shopCartNum);
        totalPrice = findViewById(R.id.totalPrice);
        noShop = findViewById(R.id.noShop);
        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
    }


    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    /**
     * 设置动画（点击添加商品）
     *
     * @param v
     * @param startLocation
     */
    public void setAnim(final View v, int[] startLocation) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        anim_mask_layout.addView(v);//把动画小球添加到动画层
        final View view = addViewToAnimLayout(anim_mask_layout, v, startLocation);
        int[] endLocation = new int[2];// 存储动画结束位置的X、Y坐标
        shopCartNum.getLocationInWindow(endLocation);
        // 计算位移
        int endX = 0 - startLocation[0] + 40;// 动画位移的X坐标
        int endY = endLocation[1] - startLocation[1];// 动画位移的y坐标

        TranslateAnimation translateAnimationX = new TranslateAnimation(0, endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, 0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationY.setFillAfter(true);

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(400);// 动画的执行时间
        view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
        });

    }

    /**
     * 初始化动画图层
     *
     * @return
     */
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setId(Integer.MAX_VALUE - 1);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * 将View添加到动画图层
     *
     * @param parent
     * @param view
     * @param location
     * @return
     */
    private View addViewToAnimLayout(final ViewGroup parent, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }
}
