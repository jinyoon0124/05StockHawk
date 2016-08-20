package com.sam_chordas.android.stockhawk.historical;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

/**
 * Created by Jin Yoon on 8/20/2016.
 */
public class DateAxisValueFormatter implements AxisValueFormatter {

    private String[] mValues;

    public DateAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mValues[(int)value];
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
