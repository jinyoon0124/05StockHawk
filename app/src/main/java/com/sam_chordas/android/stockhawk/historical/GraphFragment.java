package com.sam_chordas.android.stockhawk.historical;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

/**
 * Created by Jin Yoon on 8/19/2016.
 */
public class GraphFragment extends Fragment {
    private String LOG_TAG = GraphFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.graph_fragment, container, false);
//        ImageView test = (ImageView) rootView.findViewById(R.id.image_test);
//        test.setImageResource(R.drawable.common_full_open_on_phone);
//

//        ArrayList<String> dateList = getArguments().getStringArrayList("HISTORICALDATELIST");
//        if (dateList != null) {
//            for(int i=0; i<dateList.size(); i++){
//                Log.v("DATE IN FRAG", dateList.get(i));
//            }
//            Log.v("DATE IN FRAG", "NULL");
//        }
        Log.v(LOG_TAG, "FRAGMENT ACTIVATED");

        return rootView;
    }
}
