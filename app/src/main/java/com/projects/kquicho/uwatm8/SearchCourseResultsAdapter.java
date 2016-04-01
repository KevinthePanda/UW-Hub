package com.projects.kquicho.uwatm8;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Kevin Quicho on 3/29/2016.
 */
public class SearchCourseResultsAdapter extends android.support.v4.widget.SimpleCursorAdapter {
    private static final String TAG = SearchCourseResultsAdapter.class.getName();
    private Context mContext = null;

    public SearchCourseResultsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        this.mContext=context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView catalogNumber =(TextView)view.findViewById(R.id.course_name);
        TextView title =(TextView)view.findViewById(R.id.title);

        catalogNumber.setText(cursor.getString(1));
        title.setText(cursor.getString(2));
    }

}
