package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
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
import com.sam_chordas.android.stockhawk.historical.GraphFragment;
import com.sam_chordas.android.stockhawk.historical.HistoricalObject;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
                    historicalObjectList = Utils.historicalJsonToObject(getResponse);
                    if (historicalObjectList.size() != 0) {
                        for (int i = 0; i < historicalObjectList.size(); i++) {
                            historicalDateList.add(historicalObjectList.get(i).getHistoricalDate());
                            historicalPriceList.add(historicalObjectList.get(i).getHistoricalPrice());
//                            Log.v("HISTORICAL OBJECT", historicalObjectList.get(i).getHistoricalDate());
                        }
                    //TODO: Intent to GraphFragment
//                        for (int i = 0; i < historicalObjectList.size(); i++) {
//                            Log.v("HISTORICAL OBJECT", historicalDateList.get(i));
//                        }
                        Bundle args = new Bundle();
                        args.putStringArrayList("DATE", historicalDateList);
                        args.putStringArrayList("PRICE", historicalPriceList);

                        GraphFragment graphFragment = new GraphFragment();
                        graphFragment.setArguments(args);
                    }
//                    }else if(Utils.quoteJsonToContentVals(getResponse).size()==0){
//                        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(mContext);
//                        SharedPreferences.Editor spe = spf.edit();
//                        spe.putString(mContext.getString(R.string.stock_availability_key), mContext.getString(R.string.stock_symbol_empty_msg));
//                        spe.apply();
//                        mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
//                    }
//                    else{
//                        Log.v("ERRORROR", "OPPS! THAT STOCK IS NOT AVAILABLE");
//                        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(mContext);
//                        SharedPreferences.Editor spe = spf.edit();
//                        spe.putString(mContext.getString(R.string.stock_availability_key), mContext.getString(R.string.stock_availability_msg));
//                        spe.apply();
//                        mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
//                        return result;
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //TODO: Add Intent to initiate drawgraph() method in DetailStocksActivity.class
        //with historical Object that is sent back from Utils
        //DetailActivity -> IntentService -> HistoricalTaskService -> Utils (parse JSON and put them in historicalObject) -> Initiate drawgraph with Historical Object

        return result;


    }
}
