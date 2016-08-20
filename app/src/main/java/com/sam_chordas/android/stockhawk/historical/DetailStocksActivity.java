package com.sam_chordas.android.stockhawk.historical;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.squareup.okhttp.Callback;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DetailStocksActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText fromDateEtxt, toDateEtxt;
    private String mFromDate, mToDate;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private Intent mServiceIntent;
    private String mStockSymbol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

//        Toast.makeText(DetailStocksActivity.this, "Detail Activity Activated", Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();
        mStockSymbol = intent.getStringExtra("KK");

        TextView stockSymbolView = (TextView) findViewById(R.id.graph_stock_symbol);
        mServiceIntent = new Intent(this, StockIntentService.class);

        fromDateEtxt = (EditText) findViewById(R.id.etxt_fromdate);
        fromDateEtxt.setInputType(InputType.TYPE_NULL);
        fromDateEtxt.requestFocus();

        toDateEtxt = (EditText) findViewById(R.id.etxt_todate);
        toDateEtxt.setInputType(InputType.TYPE_NULL);

        setDateTimeField();

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


        stockSymbolView.setText(mStockSymbol);

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

            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());


        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                toDateEtxt.setText(dateFormatter.format(newDate.getTime()));


                if(fromDateEtxt.getText().toString().equals("")
                        || toDateEtxt.getText().toString().equals("")){
                    Toast.makeText(DetailStocksActivity.this, "PLEASE SELECT DATES", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        if(!fromDateEtxt.getText().toString().equals("")) {
                            mFromDate = fromDateEtxt.getText().toString();
                            mToDate = toDateEtxt.getText().toString();
                            long fromTime = dateFormatter.parse(mFromDate).getTime();
                            long toTime = dateFormatter.parse(mToDate).getTime();

                            if(toTime<fromTime){
                                Toast.makeText(DetailStocksActivity.this, "TO SMALLER THAN FROM", Toast.LENGTH_SHORT).show();
                            }else{
                                //TODO: INITIATE TASK SERVICE HERE
                                Toast.makeText(DetailStocksActivity.this, "TASK SERVICE INITIATED", Toast.LENGTH_SHORT).show();
                                mServiceIntent.putExtra("tag", "historical");
                                mServiceIntent.putExtra("symbol", mStockSymbol);
                                mServiceIntent.putExtra("fromdate", mFromDate);
                                mServiceIntent.putExtra("todate", mToDate);
                                startService(mServiceIntent);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }



            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());



        //        toDatePickerDialog.getDatePicker().setMinDate(fromDatePickerDialog.getDatePicker().getMinDate());

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



//    public static void drawgraph(ArrayList<>){
//        TextView test = (TextView) DetailStocksActivity.this.findViewById()
//    }
}
