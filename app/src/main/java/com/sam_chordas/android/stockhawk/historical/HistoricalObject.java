package com.sam_chordas.android.stockhawk.historical;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jin Yoon on 8/19/2016.
 */
public class HistoricalObject implements Serializable {


    private String historicalDate;
    private String historicalPrice;


    public String getHistoricalDate() {
        return historicalDate;
    }

    public void setHistoricalDate(String historicalDate) {
        this.historicalDate = historicalDate;
    }

    public String getHistoricalPrice() {
        return historicalPrice;
    }

    public void setHistoricalPrice(String historicalPrice) {
        this.historicalPrice = historicalPrice;
    }
}
