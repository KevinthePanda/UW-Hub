package com.projects.kquicho.uwatm8;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Kevin Quicho on 3/29/2016.
 */
public class SearchCourseResultsAdapter extends android.support.v4.widget.SimpleCursorAdapter {
    private static final String TAG = SearchCourseResultsAdapter.class.getName();
    private Context mContext = null;
    final private onSuggestionClickListener mOnSuggestionClick;

    public SearchCourseResultsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags,
                                      onSuggestionClickListener onClickListener) {
        super(context, layout, c, from, to, flags);

        mContext=context;
        mOnSuggestionClick = onClickListener;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView catalogNumber =(TextView)view.findViewById(R.id.course_name);
        TextView title =(TextView)view.findViewById(R.id.title);

        final String catalogNumberS = cursor.getString(1).split(" ")[1];
        final String titleS = cursor.getString(2);
        catalogNumber.setText(cursor.getString(1));
        title.setText(cursor.getString(2));

        view.findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("test", "onClick");
                mOnSuggestionClick.onSuggestionClick(catalogNumberS,titleS );
            }
        });
    }


    public interface onSuggestionClickListener{
        void onSuggestionClick(String catalogNumber, String title);
    }
}
