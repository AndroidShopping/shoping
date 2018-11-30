package comm.shop.shopping.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.droidlover.xdroidmvp.event.BusProvider;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.model.ShopResult;
import comm.shop.shopping.ui.fragment.GoodsFragment;
import comm.shop.shopping.utils.TextUtils;

public class MainActivity extends BaseAcivity {


    //    实现提交订单的页面。内容包括
//1.页面UI布局，字体和间距与设计稿一致
//2.实现标题栏的样式和功能，与设计稿一致
//3.填充购物商品数据
//4.实现向服务器模拟提交订单的功能。并对服务器返回错误进行处理
//5.实现点击去购彩按钮的背景图片
//
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
    private GoodsFragment goodsFragment;
    ShopPopupFragment myFragment;

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        goodsFragment.refresh();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        setStatusBar();
        myFragment = new ShopPopupFragment();
        goodsFragment = (GoodsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFragment.setShopResult(goodsFragment.getResult());
                myFragment.showNow(getSupportFragmentManager(), "dialog_show");
            }
        });
        BusProvider.getBus().subscribe(this, new RxBus.Callback<ShopResult>() {
            @Override
            public void onEvent(ShopResult event) {
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
        goCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitActivity.start(MainActivity.this, goodsFragment.getResult());
            }
        });
    }

    private void initView() {
        shopCartMain = findViewById(R.id.shopCartMain);
        shopCartNum = findViewById(R.id.shopCartNum);
        totalPrice = findViewById(R.id.totalPrice);
        noShop = findViewById(R.id.noShop);
        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goodsFragment.initData(null);
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


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void initData(Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public Object newP() {
        return null;
    }
}
