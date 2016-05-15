package com.projects.kquicho.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.kquicho.network.Course.Course;
import com.projects.kquicho.uwhub.R;


import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {
    public static final String TAG = "CoursesAdapter";
    private static onCourseClickListener mCourseClickListener;

    ArrayList<Course> mData;

    public static class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public View mContainer;
        public TextView mCourseName;
        public TextView mTitle;
       // public TextView mDescription;

        public CourseViewHolder(View v){
            super(v);
            mContainer = v.findViewById(R.id.container);
            mCourseName = (TextView) v.findViewById(R.id.course_name);
            mTitle = (TextView) v.findViewById(R.id.title);
          //  mDescription = (TextView) v.findViewById(R.id.description);
            mContainer.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mCourseClickListener.onCourseClick(mCourseName.getText().toString().split(" ")[1], mTitle.getText().toString());
        }
    }

    public CoursesAdapter(ArrayList<Course> data, onCourseClickListener courseClickListener){
        mData = data;
        mCourseClickListener = courseClickListener;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.catalog_number_row, parent, false);

        return  new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder viewHolder, int position) {
        Course course = mData.get(position);
        viewHolder.mCourseName.setText(course.getSubject() + " " + course.getCatalogNumber());
        viewHolder.mTitle.setText(course.getTitle());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public interface onCourseClickListener {
        void onCourseClick(String catalogNumber, String title);
    }

}
