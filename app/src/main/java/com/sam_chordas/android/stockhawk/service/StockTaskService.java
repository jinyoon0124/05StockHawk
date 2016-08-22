package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public static final String ACTION_DATA_UPDATED = "com.example.sam_chordas.stockhawk.ACTION_DATA_UPDATED";

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
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
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
// Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        if (params.getTag().equals("iit") || params.getTag().equals("periodic")) {

        if (params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
//            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
//// Init task. Populates DB with quotes for the symbols seen below
//                try {
//                    urlStringBuilder.append(
//                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            } else
            if (initQueryCursor != null) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            updateWidgets();
        } else if (params.getTag().equals("add")) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            //
//            if(stockInput==null){
//                mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
//            }

            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);
                    }
                    //Updated data applied to content resolver

                    Log.v("GETRESPONSE", getResponse);

                    //Successful download
                    if(Utils.quoteJsonToContentVals(getResponse).size()!=0 && Utils.quoteJsonToContentVals(getResponse).get(0)!=null){
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                Utils.quoteJsonToContentVals(getResponse));
                        //Log.v("PACKAGENAME : ", mContext.getPackageName());
                        updateWidgets();

                    //no input handling
                    }else if(Utils.quoteJsonToContentVals(getResponse).size()==0){
                        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor spe = spf.edit();
                        spe.putString(mContext.getString(R.string.stock_availability_key), mContext.getString(R.string.stock_symbol_empty_msg));
                        spe.apply();
                        mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
                    }

                    //Unavailable input handling
                    else{
                        Log.v("ERRORROR", "OPPS! THAT STOCK IS NOT AVAILABLE");
                        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor spe = spf.edit();
                        spe.putString(mContext.getString(R.string.stock_availability_key), mContext.getString(R.string.stock_availability_msg));
                        spe.apply();
                        mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
                        return result;
                    }

                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void updateWidgets() {
        Intent dataUpdatedIntent = new Intent();
        dataUpdatedIntent.setAction(ACTION_DATA_UPDATED);
        mContext.sendBroadcast(dataUpdatedIntent);
        Log.v(LOG_TAG + " WIDGET : ", "updateWidget is called hereherehrehrehre");
    }

}
