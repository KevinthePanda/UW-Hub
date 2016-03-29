package com.projects.kquicho.uwatm8;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.projects.kquicho.uw_api_client.Codes.Subject;


public class GroupSubjectAdapter
        extends AbstractExpandableItemAdapter<GroupSubjectAdapter.MyGroupViewHolder, GroupSubjectAdapter.MyChildViewHolder>{
    public static final String TAG = "GroupSubjectAdapter";
    private static onSubjectClickListener mSubjectClickListener;

    // NOTE: Make accessible with short name
    private interface Expandable extends ExpandableItemConstants {
    }

    private GroupSubjectData mData;


    public static abstract class MyBaseViewHolder extends AbstractExpandableItemViewHolder {
        public View mContainer;

        public MyBaseViewHolder(View v) {
            super(v);
            mContainer =  v.findViewById(R.id.container);
        }
    }

    public static class MyGroupViewHolder extends MyBaseViewHolder {
        public ExpandableItemIndicator mIndicator;
        public TextView mFaculty;

        public MyGroupViewHolder(View v) {
            super(v);
            mIndicator = (ExpandableItemIndicator) v.findViewById(R.id.indicator);
            mFaculty = (TextView) v.findViewById(R.id.faculty);

        }
    }

    public static class MyChildViewHolder extends MyBaseViewHolder implements View.OnClickListener {
        public TextView mSubject;
        public TextView mDesc;

        public MyChildViewHolder(View v) {
            super(v);
            mSubject = (TextView) v.findViewById(R.id.subject);
            mDesc = (TextView) v.findViewById(R.id.desc);
            mContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mSubjectClickListener.onSubjectClick(mSubject.getText().toString());
        }
    }

    public GroupSubjectAdapter(GroupSubjectData data, onSubjectClickListener subjectClickListener) {
        mData = data;
        mSubjectClickListener = subjectClickListener;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mData.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return  mData.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return  groupPosition + childPosition;
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.group_subject_row, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.subject_row, parent, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
        // child item
        final String item = mData.getGroupItem(groupPosition);

        // set text
        holder.mFaculty.setText(item);

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;
            boolean isExpanded;
            boolean animateIndicator = ((expandState & Expandable.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);

            if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                bgResId = R.color.theme_primary;
                isExpanded = true;
            } else {
                bgResId = R.color.bg_item_normal_state;
                isExpanded = false;
            }

            holder.mContainer.setBackgroundResource(bgResId);
            holder.mIndicator.setExpandedState(isExpanded, animateIndicator);
        }
    }

    @Override
    public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
        // group item
        final Subject item = mData.getChildItem(groupPosition, childPosition);

        // set text
        holder.mSubject.setText(item.getSubject());
        holder.mDesc.setText(item.getDescription());
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        return true;
    }

    public interface onSubjectClickListener {
        void onSubjectClick(String subject);
    }

}