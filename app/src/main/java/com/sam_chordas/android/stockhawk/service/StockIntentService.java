package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.historical.DetailStocksActivity;

import org.w3c.dom.Text;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        HistoricalTaskService historicalTaskService = new HistoricalTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")) {
            args.putString("symbol", intent.getStringExtra("symbol"));
            Log.v("ONHANDLEINTENT", "STOCKTASKSERVICE INITIATED");
            stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

        }else if(intent.getStringExtra("tag").equals("historical")){
            args.putString("symbol", intent.getStringExtra("symbol"));
            args.putString("fromdate", intent.getStringExtra("fromdate"));
            args.putString("todate", intent.getStringExtra("todate"));
            Log.v("ONHANDLEINTENT", "HISTORICALTASKSERVICE INITIATED");
            historicalTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));


        }


        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
//        Log.v("ONHANDELINTENT", String.valueOf(k));
    }
}
