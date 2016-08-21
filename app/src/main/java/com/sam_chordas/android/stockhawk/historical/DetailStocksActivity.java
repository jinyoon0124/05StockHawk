package com.sam_chordas.android.stockhawk.historical;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.HistoricalTaskService;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.squareup.okhttp.Callback;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailStocksActivity extends AppCompatActivity implements View.OnClickListener{
    private String LOG_TAG = DetailStocksActivity.class.getSimpleName();

    private LineChart mChart;
    private EditText fromDateEtxt, toDateEtxt;
    private String mFromDate, mToDate;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private Intent mServiceIntent;
    private String mStockSymbol;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private ArrayList<String> dateList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        mContext= this;
//        Toast.makeText(DetailStocksActivity.this, "Detail Activity Activated", Toast.LENGTH_SHORT).show();
        TextView stockSymbolView = (TextView) findViewById(R.id.graph_stock_symbol);
        fromDateEtxt = (EditText) findViewById(R.id.etxt_fromdate);
        toDateEtxt = (EditText) findViewById(R.id.etxt_todate);
        mChart = (LineChart) findViewById(R.id.lineChart);

        mChart.setNoDataTextDescription(getString(R.string.graph_empty_msg_string));

        Intent intent = getIntent();
        mStockSymbol = intent.getStringExtra("SYMBOL");

        mServiceIntent = new Intent(this, StockIntentService.class);

        //Set Date Picker
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fromDateEtxt.setInputType(InputType.TYPE_NULL);
        fromDateEtxt.requestFocus();
        toDateEtxt.setInputType(InputType.TYPE_NULL);
        setDateTimeField();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                drawGraph(intent);
            }
        };

        stockSymbolView.setText(mStockSymbol);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void drawGraph(Intent intent) {

        Bundle args = intent.getBundleExtra("HISTORICAL_DATA");
        dateList = args.getStringArrayList("HISTORICALDATELIST");
        ArrayList<String> priceList = args.getStringArrayList("HISTORICALPRICELIST");

        List<Entry> entryValues = new ArrayList<>();

        for(int i = 0; i<dateList.size();i++){
//            Log.v(LOG_TAG + " DATE : ", String.valueOf(Utils.getFloatDate(dateList.get(i))));
//            Log.v(LOG_TAG + " PRICE : ", String.valueOf(Utils.getFloatBidPrice(priceList.get(i))));
            Entry item = new Entry((float)i, Utils.getFloatBidPrice(priceList.get(i)));
            Log.v(LOG_TAG + " INDEX : ", String.valueOf((float)i));
            Log.v(LOG_TAG + " DATE : ", dateList.get((int)(float)i));
            entryValues.add(item);

        }

        LineDataSet setEntryValues = new LineDataSet(entryValues, mStockSymbol);
        LineData lineStockData = new LineData(setEntryValues);
        mChart.setData(lineStockData);
        XAxis label = mChart.getXAxis();
        label.setTextColor(getColor(R.color.white));
        label.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Log.v(LOG_TAG + " FORMAT VALUE : ", String.valueOf(value));
                if(value<dateList.size()){
                    return dateList.get((int)value);
                }else{
                    return null;
                }
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        label.setGranularityEnabled(true);

        if(entryValues.size()<10){
            setEntryValues.setValueTextSize(10f);
            setEntryValues.setValueTextColor(getColor(R.color.white));
            label.setGranularity(1);

        }else{
//            setEntryValues.setDrawValues(false);
//            label.setGranularity(Math.round(entryValues.size()/5));
            setEntryValues.setDrawValues(true);
//            mChart.setMaxVisibleValueCount(5);
            label.setLabelCount(5);


        }


        mChart.getAxisLeft().setTextColor(getColor(R.color.white));
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");

        mChart.invalidate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(HistoricalTaskService.HISTORICAL_DATA_UPDATED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setDateTimeField() {
        fromDateEtxt.setOnClickListener(this);
        toDateEtxt.setOnClickListener(this);

        Calendar ca = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                fromDateEtxt.setText(dateFormatter.format(newDate.getTime()));

                historicalTaskServiceInitiator();

            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());


        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                toDateEtxt.setText(dateFormatter.format(newDate.getTime()));

                historicalTaskServiceInitiator();
            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());



        //        toDatePickerDialog.getDatePicker().setMinDate(fromDatePickerDialog.getDatePicker().getMinDate());

    }

    private void historicalTaskServiceInitiator() {

        mFromDate = fromDateEtxt.getText().toString();
        mToDate = toDateEtxt.getText().toString();
        try {

            if(!mFromDate.equals("") && !mToDate.equals("")){
                long fromTime = dateFormatter.parse(mFromDate).getTime();
                long toTime = dateFormatter.parse(mToDate).getTime();

                if(toTime<fromTime){
                    Toast.makeText(mContext, getString(R.string.date_selector_to_smaller_than_from), Toast.LENGTH_SHORT).show();
                }else{
    //                Toast.makeText(DetailStocksActivity.this, "TASK SERVICE INITIATED", Toast.LENGTH_SHORT).show();
                    mServiceIntent.putExtra("tag", "historical");
                    mServiceIntent.putExtra("symbol", mStockSymbol);
                    mServiceIntent.putExtra("fromdate", mFromDate);
                    mServiceIntent.putExtra("todate", mToDate);
                    startService(mServiceIntent);
                }
            }else if(mFromDate.equals("") && mToDate.equals("")){
                Toast.makeText(mContext, getString(R.string.date_selector_to_and_from_empty), Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }



//
//        if(fromDateEtxt.getText().toString().equals("")
//                || toDateEtxt.getText().toString().equals("")){
//            Toast.makeText(DetailStocksActivity.this, "SELECT TO DATES", Toast.LENGTH_SHORT).show();
//        }else{
//            try {
//                if(!fromDateEtxt.getText().toString().equals("")) {
//                    mFromDate = fromDateEtxt.getText().toString();
//                    mToDate = toDateEtxt.getText().toString();
//                    long fromTime = dateFormatter.parse(mFromDate).getTime();
//                    long toTime = dateFormatter.parse(mToDate).getTime();
//
//                    if(toTime<fromTime){
//                        Toast.makeText(DetailStocksActivity.this, "TO SMALLER THAN FROM", Toast.LENGTH_SHORT).show();
//                    }else{
//                        // INITIATE TASK SERVICE HERE
//                        Toast.makeText(DetailStocksActivity.this, "TASK SERVICE INITIATED", Toast.LENGTH_SHORT).show();
//                        mServiceIntent.putExtra("tag", "historical");
//                        mServiceIntent.putExtra("symbol", mStockSymbol);
//                        mServiceIntent.putExtra("fromdate", mFromDate);
//                        mServiceIntent.putExtra("todate", mToDate);
//                        startService(mServiceIntent);
//                    }
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        if(v == fromDateEtxt){
            fromDatePickerDialog.show();
        }else if (v==toDateEtxt){
            toDatePickerDialog.show();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }




}
