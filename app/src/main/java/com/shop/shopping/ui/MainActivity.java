package com.shop.shopping.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.classic.common.MultipleStatusView;
import com.shop.shopping.adapter.adapter.GoodAdapter;
import com.shop.shopping.adapter.adapter.RecycleGoodsCategoryListAdapter;
import com.shop.shopping.boothprint.BluetoothController;
import com.shop.shopping.boothprint.base.AppInfo;
import com.shop.shopping.boothprint.bt.BtInterface;
import com.shop.shopping.boothprint.bt.BtUtil;
import com.shop.shopping.boothprint.print.PrintMsgEvent;
import com.shop.shopping.boothprint.print.PrinterMsgType;
import com.shop.shopping.boothprint.util.ToastUtil;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.present.PShopPresenter;
import com.shop.shopping.stickyheadergrid.StickyHeaderGridLayoutManager;
import com.shop.shopping.utils.DensityUtil;
import com.shop.shopping.utils.TextUtils;
import com.shop.shopping.utils.ToastUtils;
import com.shop.shopping.widget.MyDividerItemDecoration;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;

public class MainActivity extends BaseAcivity<PShopPresenter> implements BtInterface {
    int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    public BluetoothAdapter mAdapter;
    public boolean mBtEnable;
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
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    private ViewGroup anim_mask_layout;
    ShopPopupFragment myFragment;

    @BindView(R.id.goods_category_list)
    RecyclerView mGoodsCateGoryList;
    private RecycleGoodsCategoryListAdapter mGoodsCategoryListAdapter;
    @BindView(R.id.goods_recycleView)
    RecyclerView recyclerView;


    @BindView(R.id.refresh_multiple_status_view)
    MultipleStatusView statusView;


    private GoodAdapter goodAdapter;
    private StickyHeaderGridLayoutManager gridLayoutManager;
    public static final int DEFAULT_ITEM_INTVEL = 10;
    private ShopResult result;
    private int mIndex;
    private boolean move;
    private LinearLayoutManager linearLayoutManager;

    /**
     * blue tooth broadcast receiver
     */
    protected BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            String action = intent.getAction();
            if (android.text.TextUtils.isEmpty(action)) {
                return;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btStartDiscovery(intent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btFinishDiscovery(intent);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                btStatusChanged(intent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                btFoundDevice(intent);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                btBondStatusChange(intent);
            } else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                btPairingRequest(intent);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        BtUtil.registerBluetoothReceiver(mBtReceiver, this);
        BluetoothController.init(this);
        if (mAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            mAdapter.enable();
            ToastUtil.showToast(MainActivity.this, R.string.ble_state_cloes);
            return;
        }
        if (android.text.TextUtils.isEmpty(AppInfo.btAddress)) {
            startActivity(new Intent(MainActivity.this, SearchBluetoothActivity.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BtUtil.unregisterBluetoothReceiver(mBtReceiver, this);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        initView();
        initData();
        myFragment = new ShopPopupFragment(this);
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
                if (mAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    mAdapter.enable();
                    ToastUtil.showToast(MainActivity.this, R.string.ble_state_cloes);
                }
                if (android.text.TextUtils.isEmpty(AppInfo.btAddress)) {
                    startActivity(new Intent(MainActivity.this, SearchBluetoothActivity.class));
                    return;
                }

                SubmitActivity.start(MainActivity.this, result);
            }
        });
        getP().getShopProductList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        titlebar.setOnClickListener(new View.OnClickListener() {
            final int COUNTS = 5;//点击次数
            final long DURATION = 3 * 1000;//规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    SettingActivity.start(MainActivity.this);
                }
            }
        });
        EventBus.getDefault().register(this);
    }

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
                goCal.setEnabled(true);
                image.setClickable(true);
            } else {
                shopCartNum.setVisibility(View.GONE);
                totalPrice.setVisibility(View.GONE);
                noShop.setVisibility(View.VISIBLE);
                goCal.setEnabled(false);
                image.setClickable(false);
            }
            totalPrice.setText(TextUtils.getPriceText(event.getAllSelectPrice()));

        }
    }

    private void initData() {
        mGoodsCategoryListAdapter = new RecycleGoodsCategoryListAdapter(null, this);
        linearLayoutManager = new LinearLayoutManager(this);
        mGoodsCateGoryList.setLayoutManager(linearLayoutManager);
        mGoodsCateGoryList.setAdapter(mGoodsCategoryListAdapter);
        mGoodsCateGoryList.addItemDecoration(new MyDividerItemDecoration(this));
        mGoodsCateGoryList.setItemAnimator(new DefaultItemAnimator());
        mGoodsCategoryListAdapter.setOnItemClickListener(new RecycleGoodsCategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setChecked(position, true);
            }
        });
        gridLayoutManager = new StickyHeaderGridLayoutManager(2);
        gridLayoutManager.setHeaderStateChangeListener(new StickyHeaderGridLayoutManager.HeaderStateChangeListener() {
            @Override
            public void onHeaderStateChanged(int section, View headerView, StickyHeaderGridLayoutManager.HeaderState state, int pushOffset) {
                if (state == StickyHeaderGridLayoutManager.HeaderState.STICKY) {

                    setChecked(section, false);
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerItemDecoration(DensityUtil.dp2px(this, DEFAULT_ITEM_INTVEL), 2));
        goodAdapter = new GoodAdapter(this, null);
        recyclerView.setAdapter(goodAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (move) {
                    move = false;
                    int n = mIndex - gridLayoutManager.getFirstVisibleHeaderPosition(false);
                    if (0 <= n && n < recyclerView.getChildCount()) {
                        int top = recyclerView.getChildAt(n).getTop();
                        recyclerView.scrollBy(0, top);
                    }
                }


            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (move && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    move = false;
                    int n = mIndex - gridLayoutManager.getFirstVisibleHeaderPosition(false);
                    if (0 <= n && n < recyclerView.getChildCount()) {
                        int top = recyclerView.getChildAt(n).getTop();
                        recyclerView.smoothScrollBy(0, top);
                    }

                }
            }
        });


    }


    private void setChecked(int position, boolean isLeft) {
        int counts = 0;
        for (int i = 0; i < position; i++) {
            counts += result.getData().get(i).getShopItem().size();
        }

        if (isLeft) {
            mGoodsCategoryListAdapter.setCheckPosition(position);
            counts += position;
            smoothMoveToPosition(counts);
            moveToCenter(position);
        } else {
            mGoodsCategoryListAdapter.setCheckPosition(position);
            moveToCenter(position);
        }


    }

    //将当前选中的item居中
    private void moveToCenter(int position) {
        //将点击的position转换为当前屏幕上可见的item的位置以便于计算距离顶部的高度，从而进行移动居中
        int itemPosition = position - linearLayoutManager.findFirstVisibleItemPosition();
        /*
        当往上滑动太快，会出现itemPosition为-1的情况。做下判断
         */
        if (0 < itemPosition && itemPosition < linearLayoutManager.getChildCount()) {
            View childAt = mGoodsCateGoryList.getChildAt(position - linearLayoutManager.findFirstVisibleItemPosition());
            int y = (childAt.getTop() - linearLayoutManager.getHeight() / 2);
            mGoodsCateGoryList.smoothScrollBy(0, y);
        }


    }


    private void smoothMoveToPosition(int n) {
        mIndex = n;
        recyclerView.stopScroll();
        int firstItem = gridLayoutManager.getFirstVisibleItemPosition(true);
        int lastItem = gridLayoutManager.getLastVisibleItemPosition();

        if (n <= firstItem) {
            recyclerView.scrollToPosition(n);
        } else if (n <= lastItem) {
            int top = recyclerView.getChildAt(n - firstItem).getTop();
            recyclerView.scrollBy(0, top);
        } else {
            recyclerView.scrollToPosition(n);
            move = true;
        }
    }


    public void onStartLoading() {
        statusView.showLoading();

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
        if (shopResult == null) {
            statusView.showEmpty();
            return;
        }

        if (shopResult.isBizError()) {
            ToastUtils.show(shopResult.getMessage());
            statusView.showError();
            return;
        }
        statusView.showContent();
        this.result = shopResult;
        shopCartNum.setVisibility(View.GONE);
        totalPrice.setVisibility(View.GONE);
        noShop.setVisibility(View.VISIBLE);
        goCal.setEnabled(false);
        image.setClickable(false);
        mGoodsCategoryListAdapter.updateShopResult(shopResult);
        goodAdapter.updateShopResult(shopResult);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void btStartDiscovery(Intent intent) {

    }

    @Override
    public void btFinishDiscovery(Intent intent) {

    }

    @Override
    public void btStatusChanged(Intent intent) {

    }

    @Override
    public void btFoundDevice(Intent intent) {

    }

    @Override
    public void btBondStatusChange(Intent intent) {

    }

    @Override
    public void btPairingRequest(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
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
            switch (error.getType()) {
                case NetError.ParseError:
                    statusView.showError();
                    break;

                case NetError.AuthError:
                    statusView.showError();
                    break;

                case NetError.BusinessError:
                    statusView.showError();
                    break;

                case NetError.NoConnectError:
                    statusView.showNoNetwork();
                    break;

                case NetError.NoDataError:
                    statusView.showEmpty();
                    break;

                case NetError.OtherError:
                    statusView.showError();
                default:
            }
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refresh();

    }


    private void initView() {
        shopCartNum = findViewById(R.id.shopCartNum);
        totalPrice = findViewById(R.id.totalPrice);
        noShop = findViewById(R.id.noShop);
        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
//        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SettingActivity.start(v.getContext());
//            }
//        });
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

    /**
     * handle printer message
     *
     * @param event print msg event
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PrintMsgEvent event) {
        if (event.type == PrinterMsgType.MESSAGE_TOAST) {
            ToastUtil.showToast(MainActivity.this, event.msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(MainActivity.this);
    }
}
