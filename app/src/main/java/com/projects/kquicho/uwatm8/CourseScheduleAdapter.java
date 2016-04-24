package com.projects.kquicho.uwatm8;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseScheduleAdapter
        extends AbstractExpandableItemAdapter<CourseScheduleAdapter.MyGroupViewHolder, CourseScheduleAdapter.MyBaseViewHolder>{
    public static final String TAG = "CourseScheduleAdapter";
    private final int CLASS = 0, ENROLLMENT = 1, FOOTER = 2;
    public static final int ADD_EVENT = 0, DIRECTION = 1, VIEW_EVENT = 2, ADD_WATCH = 3, REMOVE_WATCH = 4;

    private CourseScheduleData mData;
    private int mDensity;

    onButtonClickListener mOnButtonClickListener;
    Drawable mEnrollmentStatusOpen;
    Drawable mEnrollmentStatusFull;
    Drawable mViewCalendarDrawable;
    Drawable mAddCalendarDrawable;
    Drawable mEyeClosedDrawable;
    Drawable mEyeOpenDrawable;

    // NOTE: Make accessible with short name
    private interface Expandable extends ExpandableItemConstants {
    }

    public static abstract class MyBaseViewHolder extends AbstractExpandableItemViewHolder {
        public View mContainer;

        public MyBaseViewHolder(View v) {
            super(v);
            mContainer =  v.findViewById(R.id.container);
        }
    }

    public static class MyGroupViewHolder extends MyBaseViewHolder {
        public ExpandableItemIndicator mIndicator;
        public TextView mSection;
        public TextView mInstructor;
        public TextView mTime;
        public TextView mLocation;
        public ImageView mEnrollmentStatus;

        public MyGroupViewHolder(View v) {
            super(v);
            mIndicator = (ExpandableItemIndicator) v.findViewById(R.id.indicator);
            mSection = (TextView) v.findViewById(R.id.section);
            mInstructor= (TextView) v.findViewById(R.id.instructor);
            mTime = (TextView) v.findViewById(R.id.time);
            mLocation = (TextView) v.findViewById(R.id.location);
            mEnrollmentStatus = (ImageView) v.findViewById(R.id.enrollment_status);

        }
    }

    public static class MyChildClassViewHolder extends MyBaseViewHolder {
        public TextView mTime;
        public TextView mLocation;

        public MyChildClassViewHolder(View v) {
            super(v);
            mTime = (TextView) v.findViewById(R.id.time);
            mLocation = (TextView) v.findViewById(R.id.location);
        }

    }

    public static class MyChildEnrollmentViewHolder extends MyBaseViewHolder  {
        public TextView mEnrollment;
        public TextView mEnrollmentGroup;
        public ProgressBar mProgressBar;

        public MyChildEnrollmentViewHolder(View v) {
            super(v);
            mEnrollment = (TextView) v.findViewById(R.id.enrollment);
            mEnrollmentGroup = (TextView) v.findViewById(R.id.enrollment_label);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public static class MyChildFooterViewHolder extends MyBaseViewHolder  {
        public TextView mClassNumber;
        public ImageView mGoogleCalendarBTN;
        public ImageView mGoogleMapsDirectionsBTN;
        public ImageView mCourseWatchBTN;

        public MyChildFooterViewHolder(View v) {
            super(v);
            mClassNumber = (TextView) v.findViewById(R.id.class_number);
            mGoogleCalendarBTN = (ImageView) v.findViewById(R.id.google_calendar);
            mGoogleMapsDirectionsBTN = (ImageView) v.findViewById(R.id.google_maps_directions);
            mCourseWatchBTN = (ImageView) v.findViewById(R.id.watch_course_section);
        }
    }


    public CourseScheduleAdapter(CourseScheduleData data, Activity activity,
                                 onButtonClickListener onButtonClickListener) {
        mData = data;
        if (activity != null) {
            mEnrollmentStatusOpen = ContextCompat.getDrawable(activity, R.drawable.enrollment_status_open);
            mEnrollmentStatusFull = ContextCompat.getDrawable(activity, R.drawable.enrollment_status_full);
            mViewCalendarDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_calendar);
            mAddCalendarDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_calendar_plus);
            mEyeClosedDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_eye_off);
            mEyeOpenDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_eye);
            mDensity = (int) activity.getResources().getDisplayMetrics().density;
        }
        mOnButtonClickListener = onButtonClickListener;


        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mData.getGroupCount();
    }

    @Override
    public int getChildCount(int i) {
        return  mData.getChildCount(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int j) {
        return j;
    }

    @Override
    public int getGroupItemViewType(int i) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int i, int j) {
        if(mData.getChildItem(i,j) instanceof  CourseSectionClassData){
            return CLASS;
        }else if(mData.getChildItem(i,j) instanceof CourseEnrollmentData){
            return ENROLLMENT;
        }else{
            return FOOTER;
        }
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(R.layout.section_row, viewGroup, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyBaseViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int i) {
        MyBaseViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch (i) {
            case CLASS:
                view = inflater.inflate(R.layout.section_class_row, viewGroup, false);
                viewHolder = new MyChildClassViewHolder(view);
                break;
            case ENROLLMENT:
                view = inflater.inflate(R.layout.section_enrollment_row, viewGroup, false);
                viewHolder = new MyChildEnrollmentViewHolder(view);
                break;
            case FOOTER:
                view = inflater.inflate(R.layout.section_footer, viewGroup, false);
                viewHolder = new MyChildFooterViewHolder(view);
                break;
            default:
                view = inflater.inflate(R.layout.section_class_row, viewGroup, false);
                viewHolder = new MyChildClassViewHolder(view);
                break;
        }
        return viewHolder;
}

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder myGroupViewHolder, int i, int j) {
        final CourseSectionData courseSectionData = (CourseSectionData)mData.getGroupItem(i);
        myGroupViewHolder.mSection.setText(courseSectionData.getSection());
        myGroupViewHolder.mInstructor.setText(courseSectionData.getInstructor());


        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CANADA) ;
        DateFormat newFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);
        DateFormat newFormatWithoutSuffix = new SimpleDateFormat("h:mm", Locale.CANADA);
        String time = "";
        try{
            Date startTime = dateFormat.parse(courseSectionData.getStartTime());
            Date endTime = dateFormat.parse(courseSectionData.getEndTime());
            String sStartTime = newFormat.format(startTime);
            String sEndTime = newFormat.format(endTime);

            if(sStartTime.charAt(sStartTime.length() -2) == sEndTime.charAt(sEndTime.length() -2)){
                time = newFormatWithoutSuffix.format(startTime) + " - " + sEndTime;
            }else{
                time = sStartTime + " - " + sEndTime;
            }

        }catch (ParseException ex){
            Log.e(TAG, "createClass ParseException: " + ex.getMessage());
        }

        String finalTimeText = "TBA";
        if(!courseSectionData.getWeekdays().equals("null")){
            finalTimeText = time + " " + courseSectionData.getWeekdays();
        }

        String location = "TBA - ";
        if(!courseSectionData.getBuilding().equals("null") && !courseSectionData.getRoom().equals("null")){
            location = courseSectionData.getBuilding() + " " + courseSectionData.getRoom()  + " - ";
        }else if(courseSectionData.getCampus().contains("ONLINE")){
            location = "";
            finalTimeText = "N/A";
        }
        myGroupViewHolder.mTime.setText(finalTimeText);
        myGroupViewHolder.mLocation.setText(location + courseSectionData.getCampus());


        Drawable status = courseSectionData.getEnrollmentTotal() < courseSectionData.getEnrollmentCapacity() ?
                mEnrollmentStatusOpen : mEnrollmentStatusFull;
        myGroupViewHolder.mEnrollmentStatus.setImageDrawable(status);

        // mark as clickable
        myGroupViewHolder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = myGroupViewHolder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            boolean isExpanded;
            boolean animateIndicator = ((expandState & Expandable.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);

            if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                isExpanded = true;
            } else {
                isExpanded = false;
                for(int k = mData.getChildCount(i) -1; k >= 0; k-- ){
                    AbstractExpandableData.ChildData data = mData.getChildItem(i,k);
                    if(data instanceof CourseEnrollmentData ){
                        ((CourseEnrollmentData)data).setHasAnimationRan(false);
                    }else{
                        break;
                    }
                }
            }

            myGroupViewHolder.mIndicator.setExpandedState(isExpanded, animateIndicator);
        }

    }

    @Override
    public void onBindChildViewHolder(MyBaseViewHolder myBaseViewHolder, final int groupPosition, final int childPosition, int viewType) {
        switch (viewType){
            case CLASS:
                CourseSectionClassData classData = (CourseSectionClassData)mData.getChildItem(groupPosition,childPosition);
                MyChildClassViewHolder classHolder = (MyChildClassViewHolder)myBaseViewHolder;
                DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CANADA) ;
                DateFormat newFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);
                DateFormat newFormatWithoutSuffix = new SimpleDateFormat("h:mm", Locale.CANADA);
                String time = "";
                try{
                    Date startTime = dateFormat.parse(classData.getStartTime());
                    Date endTime = dateFormat.parse(classData.getEndTime());
                    String sStartTime = newFormat.format(startTime);
                    String sEndTime = newFormat.format(endTime);

                    if(sStartTime.charAt(sStartTime.length() -2) == sEndTime.charAt(sEndTime.length() -2)){
                        time = newFormatWithoutSuffix.format(startTime) + " - " + sEndTime;
                    }else{
                        time = sStartTime + " - " + sEndTime;
                    }

                }catch (ParseException ex){
                    Log.e(TAG, "createClass ParseException: " + ex.getMessage());
                }
                String finalTimeText = "TBA";
                if(!classData.getWeekdays().equals("null")){
                    finalTimeText = time + " " + classData.getWeekdays();
                }
                classHolder.mTime.setText(finalTimeText);

                String location = "TBA";
                if(!classData.getBuilding().equals("null") && !classData.getRoom().equals("null")){
                    location = classData.getBuilding() + " " + classData.getRoom() ;
                }
                classHolder.mLocation.setText(location + " - " + classData.getCampus());

                break;
            case ENROLLMENT:
                final CourseEnrollmentData enrollmentData = (CourseEnrollmentData)mData.getChildItem(groupPosition,childPosition);
                final MyChildEnrollmentViewHolder enrollmentHolder = (MyChildEnrollmentViewHolder)myBaseViewHolder;

                String enrollmentGroup = enrollmentData.getGroup();
                if(enrollmentGroup.equals("")){
                    enrollmentHolder.mEnrollmentGroup.setText("Enrollment");
                }else{
                    enrollmentHolder.mEnrollmentGroup.setText(enrollmentGroup);
                }

                final ProgressBar progressBar = enrollmentHolder.mProgressBar;
                progressBar.setMax(enrollmentData.getEnrollmentCapacity());
                final String enrollmentCapacity = String.valueOf(enrollmentData.getEnrollmentCapacity());

                if(enrollmentData.hasAnimationRan()){
                    enrollmentHolder.mEnrollment.setText(enrollmentData.getEnrollmentTotal() + "/" + enrollmentCapacity);
                    progressBar.setProgress(enrollmentData.getEnrollmentTotal());
                }else {
                    enrollmentHolder.mEnrollment.setText("0/" + enrollmentCapacity);
                    progressBar.setProgress(0);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, enrollmentData.getEnrollmentTotal()); // see this max value coming back here, we animale towards that value
                    animation.setDuration(500); //in milliseconds
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Integer current = (Integer) animation.getAnimatedValue();
                            enrollmentHolder.mEnrollment.setText(current.toString() + "/" + enrollmentCapacity);
                        }
                    });
                    animation.setStartDelay(300);
                    animation.start();
                    enrollmentData.setHasAnimationRan(true);
                }

                break;
            case FOOTER:
                final CourseSectionFooterData footerData = (CourseSectionFooterData)mData.getChildItem(groupPosition,childPosition);
                final MyChildFooterViewHolder footerHolder = (MyChildFooterViewHolder)myBaseViewHolder;

               if(footerData.getEventID() != null) {
                    footerHolder.mGoogleCalendarBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnButtonClickListener.onButtonClick(groupPosition, childPosition, VIEW_EVENT);
                        }
                    });
                    footerHolder.mGoogleCalendarBTN.setImageDrawable(mViewCalendarDrawable);
                }else{
                    footerHolder.mGoogleCalendarBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnButtonClickListener.onButtonClick(groupPosition, childPosition, ADD_EVENT);
                        }
                    });
                    footerHolder.mGoogleCalendarBTN.setImageDrawable(mAddCalendarDrawable);
                }

                if(footerData.isBeingWatched()){
                    footerHolder.mCourseWatchBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnButtonClickListener.onButtonClick(groupPosition, childPosition, REMOVE_WATCH);
                        }
                    });
                    footerHolder.mCourseWatchBTN.setImageDrawable(mEyeOpenDrawable);
                }else{
                    footerHolder.mCourseWatchBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnButtonClickListener.onButtonClick(groupPosition, childPosition, ADD_WATCH);
                        }
                    });
                    footerHolder.mCourseWatchBTN.setImageDrawable(mEyeClosedDrawable);
                }

                    footerHolder.mGoogleMapsDirectionsBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnButtonClickListener.onButtonClick(groupPosition, childPosition, DIRECTION);
                    }
                });




                footerHolder.mClassNumber.setText(String.valueOf(footerData.getClassNumber()));
                break;

        }
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder myGroupViewHolder, int i, int i1, int i2, boolean b) {
        if (!(myGroupViewHolder.itemView.isEnabled() && myGroupViewHolder.itemView.isClickable())) {
            return false;
        }

        return true;
    }


    public interface onButtonClickListener{
        void onButtonClick(int groupPos, int childPos, int type);
    }

}
