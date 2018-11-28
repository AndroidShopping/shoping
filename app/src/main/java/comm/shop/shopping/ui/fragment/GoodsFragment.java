package comm.shop.shopping.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.blankj.rxbus.RxBus;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import cn.droidlover.xdroidmvp.event.BusProvider;
import cn.droidlover.xdroidmvp.mvp.XLazyFragment;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.shopping.R;
import comm.shop.shopping.adapter.adapter.GoodAdapter;
import comm.shop.shopping.adapter.adapter.RecycleGoodsCategoryListAdapter;
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
    public static final String TAG = "GoodsFragment";
    @BindView(R.id.goods_category_list)
    RecyclerView mGoodsCateGoryList;
    private RecycleGoodsCategoryListAdapter mGoodsCategoryListAdapter;
    @BindView(R.id.goods_recycleView)
    RecyclerView recyclerView;
    private GoodAdapter goodAdapter;
    private StickyHeaderGridLayoutManager gridLayoutManager;
    public static final int DEFAULT_ITEM_INTVEL = 10;
    private ShopResult result;

    public ShopResult getResult() {
        return result;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void bindEvent() {
        super.bindEvent();
        BusProvider.getBus().subscribe(this, new RxBus.Callback<ShopResult>() {
            @Override
            public void onEvent(ShopResult absEvent) {
                mGoodsCategoryListAdapter.notifyDataSetChanged();
                goodAdapter.notifyDataSetChanged();
            }
        });

    }

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
                Log.d(TAG, "onItemClick:position= "+position);
            }
        });
        gridLayoutManager = new StickyHeaderGridLayoutManager(2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerItemDecoration(DensityUtil.dp2px(getContext(), DEFAULT_ITEM_INTVEL), 2));
        goodAdapter = new GoodAdapter(getActivity(), shopResult);
        goodAdapter.setmActivity(getActivity());
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
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
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
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"6奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"5奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"4奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"3奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"2奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
                    "          \"picPath\": \"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543242177152&di=4f794bbf5417d119e6e33a3cbfd26b82&imgtype=0&src=http%3A%2F%2Fpic2.16pic.com%2F00%2F15%2F31%2F16pic_1531045_b.jpg\",\n" +
                    "          \"isShelf\": 1,\n" +
                    "          \"unit\": \"被\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"price\": 123,\n" +
                    "          \"name\": \"1奶茶1\",\n" +
                    "          \"description\": \"奶茶1\",\n" +
                    "          \"number\": 122,\n" +
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
