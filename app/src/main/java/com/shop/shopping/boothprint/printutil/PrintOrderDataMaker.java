package com.shop.shopping.boothprint.printutil;

import android.content.Context;
import android.content.Intent;

import com.shop.shopping.model.ShopCategory;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.ui.PrintOrderActivity;
import com.shop.shopping.utils.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.droidlover.xdroidmvp.shopping.R;

import static com.shop.shopping.ui.PrintOrderActivity.RECEIVE;
import static com.shop.shopping.ui.PrintOrderActivity.REPAY;


/**
 * 测试数据生成器
 * Created by liuguirong on 8/1/17.
 */

public class PrintOrderDataMaker implements PrintDataMaker {


    private String qr;
    private int width;
    private int height;
    Context btService;
    Intent intent;


    public PrintOrderDataMaker(Context btService, String qr, int width, int height, Intent intent) {
        this.qr = qr;
        this.width = width;
        this.height = height;
        this.btService = btService;
        this.intent = intent;
    }


    @Override
    public List<byte[]> getPrintData(int type) {
        ArrayList<byte[]> data = new ArrayList<>();

        try {
            PrinterWriter printer;
            printer = type == PrinterWriter58mm.TYPE_58 ? new PrinterWriter58mm(height, width) : new PrinterWriter80mm(height, width);
            printer.setAlignCenter();
            data.add(printer.getDataAndReset());


            printer.setAlignLeft();
            printer.printLine();
            printer.printLineFeed();

            printer.printLineFeed();
            printer.setAlignCenter();
            printer.setEmphasizedOn();
            printer.setFontSize(1);
            printer.print(TextUtils.getString(R.string.welcome_to_this));
            printer.printLineFeed();
            printer.setEmphasizedOff();
            printer.printLineFeed();

            printer.printLineFeed();
            printer.setFontSize(0);
            printer.setAlignCenter();
            printer.print(TextUtils.getString(R.string.order_id_data) + intent.getStringExtra(PrintOrderActivity.NUMBER_ID));
            printer.printLineFeed();

            printer.setAlignCenter();
            printer.print(new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis())));
            printer.printLineFeed();
            printer.printLine();

            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print(TextUtils.getString(R.string.order_state));
            printer.printLineFeed();

            printer.setAlignCenter();
            printer.printLineFeed();
            printer.printLineFeed();
            printer.setAlignCenter();
            printer.printInOneLine(TextUtils.getString(R.string.prodcut),
                    TextUtils.getString(R.string.product_dan_jia), TextUtils.getString(R.string.count), 0);
            printer.printLineFeed();
            printer.printLine();
            printer.printLineFeed();
            ShopResult result = intent.getParcelableExtra(PrintOrderActivity.RESULT);
            List<ShopCategory> data1 = result.getData();
            for (ShopCategory shopCategory : data1) {
                List<ShopItem> shopItem = shopCategory.getShopItem();
                for (ShopItem item : shopItem) {
                    if (item.getBuyCount() != 0) {
                        printer.printInOneLine(item.getName(), item.getBuyCount() + "X" +
                                TextUtils.getPriceText(item.getPrice()), TextUtils.getPriceText(item.getAllBuyPrice()), 0);
                        printer.printLineFeed();
                    }
                }
            }

            printer.printLineFeed();
            printer.printLine();
            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print(TextUtils.getString(R.string.jian_shu) + result.getAllBuyedGoodCount());
            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print(TextUtils.getString(R.string.ying_fu) + TextUtils.getPriceText(result.getAllSelectPrice()));
            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print(TextUtils.getString(R.string.shi_shou_jin_e) +
                    TextUtils.getPriceText(intent.getIntExtra(RECEIVE, 0)).replace(TextUtils.getString(R.string.mark), ""));
            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print(TextUtils.getString(R.string.zhao_ling) + TextUtils.getPriceText(intent.getIntExtra(REPAY, 0)).replace(TextUtils.getString(R.string.mark), ""));

            printer.printLineFeed();
            printer.printLineFeed();
            printer.printLine();
            printer.setAlignCenter();
            printer.print(TextUtils.getString(R.string.xie_xie_again));
            printer.printLineFeed();
            printer.printLineFeed();
            printer.feedPaperCut();
            data.add(printer.getDataAndClose());
            return data;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


}
