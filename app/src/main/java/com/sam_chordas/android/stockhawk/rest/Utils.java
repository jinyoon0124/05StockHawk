package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.historical.DetailStocksActivity;
import com.sam_chordas.android.stockhawk.historical.HistoricalObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static ArrayList<String> getDateFromJson(String JSON){
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
//        ArrayList<HistoricalObject> historicalObjectsList = new ArrayList<>();
        ArrayList<String> dateList = new ArrayList<>();

        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = resultsArray.length(); i >0 ; i--) {
                        HistoricalObject historicalObject = new HistoricalObject();

                        jsonObject = resultsArray.getJSONObject(i-1);
                        String date = jsonObject.getString("Date");
//                        String price = truncateBidPrice(jsonObject.getString("Close"));

                        dateList.add(date);
                    }
                }
            }
            } catch (JSONException e) {
            e.printStackTrace();
        }


        return dateList;
    }

    public static ArrayList<String> getPriceFromJson(String JSON){
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
//        ArrayList<HistoricalObject> historicalObjectsList = new ArrayList<>();
        ArrayList<String> priceList = new ArrayList<>();

        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = resultsArray.length(); i >0 ; i--) {

                        jsonObject = resultsArray.getJSONObject(i-1);
//                        String date = jsonObject.getString("Date");
                        String price = truncateBidPrice(jsonObject.getString("Close"));

                        priceList.add(price);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return priceList;
    }


    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        //
        try {
            String change = jsonObject.getString("Change");
            String symbol = jsonObject.getString("symbol");
            String bid = jsonObject.getString("Bid");
            String changePercent = jsonObject.getString("ChangeinPercent");
            if(bid!="null" && changePercent!="null" && change !="null"){
                builder.withValue(QuoteColumns.SYMBOL, symbol);
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bid));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        changePercent, true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
            }else{
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }




//        try {
//            String change = jsonObject.getString("Change");
//            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
//            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
//            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
//                    jsonObject.getString("ChangeinPercent"), true));
//            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
//            builder.withValue(QuoteColumns.ISCURRENT, 1);
//            if (change.charAt(0) == '-') {
//                builder.withValue(QuoteColumns.ISUP, 0);
//            } else {
//                builder.withValue(QuoteColumns.ISUP, 1);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        return builder.build();
    }

    public static float getFloatBidPrice(String bidPrice){
        return Float.parseFloat(bidPrice);
    }

    public static long getFloatDate(String date){
        String dateNewFormat = date + " 15:00:00";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        try {
            Date interDate = dateFormatter.parse(dateNewFormat);
            return interDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

}
