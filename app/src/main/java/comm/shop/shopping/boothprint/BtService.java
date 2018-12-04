package comm.shop.shopping.boothprint;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import comm.shop.shopping.boothprint.print.PrintQueue;
import comm.shop.shopping.boothprint.print.PrintUtil;
import comm.shop.shopping.boothprint.printutil.PrintOrderDataMaker;
import comm.shop.shopping.boothprint.printutil.PrinterWriter;
import comm.shop.shopping.boothprint.printutil.PrinterWriter58mm;

/**
 * Created by liuguirong on 8/1/17.
 * <p/>
 * print ticket service
 */
public class BtService extends IntentService {

    public BtService() {
        super("BtService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BtService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TICKET)) {
            PrintOrderDataMaker printOrderDataMaker = new PrintOrderDataMaker(this, "", PrinterWriter58mm.TYPE_58,
                    PrinterWriter.HEIGHT_PARTING_DEFAULT, intent);
            ArrayList<byte[]> printData = (ArrayList<byte[]>) printOrderDataMaker.getPrintData(PrinterWriter58mm.TYPE_58);
            PrintQueue.getQueue(getApplicationContext()).add(printData);
        }

    }


}