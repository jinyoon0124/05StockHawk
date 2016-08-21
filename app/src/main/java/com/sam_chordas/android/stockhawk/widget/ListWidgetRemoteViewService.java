package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by Jin Yoon on 8/16/2016.
 */
public class ListWidgetRemoteViewService extends RemoteViewsService{
    private static final String[] STOCK_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_BIDPRICE = 2;
    static final int INDEX_STOCK_PERCENT_CHANGE = 3;
    static final int INDEX_STOCK_CHANGE = 4;
    static final int INDEX_STOCK_ISUP = 5;



    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                Uri stockListUri = QuoteProvider.Quotes.CONTENT_URI;
                data = getContentResolver().query(stockListUri,
                        STOCK_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if(data!=null){
                    data.close();
                    data=null;
                }
            }

            @Override
            public int getCount() {
                return data==null? 0: data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)){
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_stocklist_item);
                int stockId = data.getInt(INDEX_STOCK_ID);
                String stockSymbol = data.getString(INDEX_STOCK_SYMBOL);
                String stockBidPrice = Utils.truncateBidPrice(data.getString(INDEX_STOCK_BIDPRICE));
                String stockPercentChange = Utils.truncateChange(data.getString(INDEX_STOCK_PERCENT_CHANGE), true);
                //String stockChange = Utils.truncateChange(data.getString(INDEX_STOCK_CHANGE), false);
                String stockIsup = data.getString(INDEX_STOCK_ISUP);

                if(stockIsup.equals("1")){
                    views.setTextColor(R.id.widget_change, getResources().getColor(R.color.material_green_700));
                }else{
                    views.setTextColor(R.id.widget_change, getResources().getColor(R.color.material_red_700));
                }

                views.setTextViewText(R.id.widget_stock_symbol, stockSymbol);
                views.setTextViewText(R.id.widget_bid_price, stockBidPrice);
                views.setTextViewText(R.id.widget_change, stockPercentChange);

                final Intent fillInIntent = new Intent();
//                Uri stockUri = QuoteProvider.Quotes.withSymbol(stockSymbol);
//                fillInIntent.setData(stockUri);
                fillInIntent.putExtra("SYMBOL", stockSymbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_stocklist_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
