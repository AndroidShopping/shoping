package com.shop.shopping.boothprint.printutil;

import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.shop.shopping.model.ShopCategory;
import com.shop.shopping.model.ShopItem;
import com.shop.shopping.model.ShopResult;
import com.shop.shopping.ui.PrintOrderActivity;
import com.shop.shopping.utils.TextUtils;


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
            printer.print("欢迎光临***餐厅");
            printer.printLineFeed();
            printer.setEmphasizedOff();
            printer.printLineFeed();

            printer.printLineFeed();
            printer.setFontSize(0);
            printer.setAlignCenter();
            printer.print("订单号：" + intent.getStringExtra(PrintOrderActivity.NUMBER_ID));
            printer.printLineFeed();

            printer.setAlignCenter();
            printer.print(new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis())));
            printer.printLineFeed();
            printer.printLine();

            printer.printLineFeed();
            printer.setAlignLeft();
            printer.print("订单状态: " + "已下单");
            printer.printLineFeed();

            printer.setAlignCenter();
            printer.print("购物信息");
            printer.printLineFeed();
            printer.setAlignCenter();
            printer.printInOneLine("产品", "数量*单价", "金额", 0);
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
            printer.print("购买件数:" + result.getAllBuyedGoodCount());

            printer.print("应付金额:" + TextUtils.getPriceText(result.getAllSelectPrice()));
            printer.printLine();
            printer.setAlignLeft();
            printer.print("实收金额:" + 100);

            printer.print("找零:" + 100);
            printer.printLine();
            printer.printLineFeed();
            printer.printLine();
            printer.setAlignCenter();
            printer.print("谢谢惠顾，欢迎再次光临！");
            printer.printLineFeed();
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
