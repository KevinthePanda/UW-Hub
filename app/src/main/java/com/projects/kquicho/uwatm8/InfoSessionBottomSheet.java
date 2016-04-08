package com.projects.kquicho.uwatm8;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Kevin Quicho on 4/7/2016.
 */
public class InfoSessionBottomSheet extends BottomSheetDialogFragment {

    public static InfoSessionBottomSheet newInstance(){
        return new InfoSessionBottomSheet();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(
                R.layout.bottom_sheet_info_session, container, false);

        return v;
    }

}
