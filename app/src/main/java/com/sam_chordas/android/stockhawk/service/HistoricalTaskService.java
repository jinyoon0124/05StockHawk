package com.sam_chordas.android.stockhawk.service;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.historical.DetailStocksActivity;
import com.sam_chordas.android.stockhawk.historical.GraphFragment;
import com.sam_chordas.android.stockhawk.historical.HistoricalObject;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Jin Yoon on 8/18/2016.
 */
public class HistoricalTaskService extends GcmTaskService {

    private String LOG_TAG = HistoricalTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();

    public static final String HISTORICAL_DATA_UPDATED = "com.example.sam_chordas.stockhawk.HISTORICAL_DATA_UPDATED";


    public HistoricalTaskService() {
    }

    public HistoricalTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    @Override
    public int onRunTask(TaskParams params) {

        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        if (params.getTag().equals("historical")) {

            try {
                String stockInput = params.getExtras().getString("symbol");
                String startDate = params.getExtras().getString("fromdate");
                String endDate = params.getExtras().getString("todate");

                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                        + "= ", "UTF-8"));

                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\" and startDate = ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\"" + startDate + "\" and endDate = ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\"" + endDate + "\"", "UTF-8"));

                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.

        String urlString;
        String getResponse;

        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    // Need to handle JSON
                    ArrayList<HistoricalObject> historicalObjectList;
                    ArrayList<String> historicalDateList = new ArrayList<>();
                    ArrayList<String> historicalPriceList = new ArrayList<>();
//                    historicalObjectList = Utils.historicalJsonToObject(getResponse);
                    historicalDateList= Utils.getDateFromJson(getResponse);
                    historicalPriceList= Utils.getPriceFromJson(getResponse);
                    Bundle args = new Bundle();
                    args.putStringArrayList("HISTORICALDATELIST", historicalDateList);
                    args.putStringArrayList("HISTORICALPRICELIST", historicalPriceList);

                    Intent intentForGraph = new Intent();
                    intentForGraph.setAction(HISTORICAL_DATA_UPDATED);
                    intentForGraph.putExtra("HISTORICAL_DATA", args);
                    mContext.sendBroadcast(intentForGraph);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return result;


    }
}
