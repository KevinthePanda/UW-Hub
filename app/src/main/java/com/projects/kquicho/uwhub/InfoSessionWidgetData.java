package com.projects.kquicho.uwhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 4/22/2016.
 */
public class InfoSessionWidgetData extends UWData implements Parcelable{

    private ArrayList<InfoSession> mInfoSessions;
    private ArrayList<InfoSessionDBModel> mSavedInfoSessions;

    public InfoSessionWidgetData(ArrayList<InfoSession> infoSessions, ArrayList<InfoSessionDBModel> savedInfoSessions){
        super(InfoSessionWidget.TAG);
        finishedLoading();
        mInfoSessions = infoSessions;
        mSavedInfoSessions = savedInfoSessions;
    }

    public InfoSessionWidgetData(Parcel in){
        super(in);
        in.readTypedList(mInfoSessions, InfoSession.CREATOR);
        in.readTypedList(mSavedInfoSessions, InfoSessionDBModel.CREATOR);
    }

    public ArrayList<InfoSession> getInfoSessions() {
        return mInfoSessions;
    }

    public ArrayList<InfoSessionDBModel> getSavedInfoSessions() {
        return mSavedInfoSessions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(mInfoSessions);
        dest.writeTypedList(mSavedInfoSessions);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public InfoSessionWidgetData createFromParcel(Parcel in) {
            return new InfoSessionWidgetData(in);
        }

        public InfoSessionWidgetData[] newArray(int size) {
            return new InfoSessionWidgetData[size];
        }
    };

}
