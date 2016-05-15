package com.projects.kquicho.network.Resources;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoSession implements Parcelable{

    private int id;
    private String employer = null;
    private String date = null;
    private String start_time = null;
    private String end_time = null;
    private String display_time_range = null;
    private String building_code = null;
    private String building_room = null;
    private String building_map_url = null;
    private String website = null;
    private String audience = null;
    private String programs = null;
    private String description = null;
    private double latitude =  0.0;
    private double longitude =  0.0;
    private String link =  null;
    private boolean isCancelled = false;
    private long time = 0;

    public void setTime(long time){
        this.time = time;
    }

    public long getTime(){
        return time;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDisplay_time_range() {
        return display_time_range;
    }

    public void setDisplay_time_range(String display_time_range) {
        this.display_time_range = display_time_range;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getBuildingCode() {
        return building_code;
    }

    public void setBuildingRoom(String building_room) {
        this.building_room = building_room;
    }

    public String getBuildingRoom() {
        return building_room;
    }

    public void setBuildingMapUrl(String building_map_url) {
        this.building_map_url = building_map_url;
    }

    public String getBuildingMapUrl() {
        return building_map_url;
    }

    public void setBuildingCode(String building_code) {
        this.building_code = building_code;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getPrograms() {
        return programs;
    }

    public void setPrograms(String programs) {
        this.programs = programs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InfoSession(){
    }

    public InfoSession(Parcel in){
        id = in.readInt();
        employer = in.readString();
        date = in.readString();
        start_time = in.readString();
        end_time = in.readString();
        display_time_range = in.readString();
        building_code = in.readString();
        building_room = in.readString();
        building_map_url = in.readString();
        website = in.readString();
        audience = in.readString();
        programs = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        link = in.readString();
        isCancelled = in.readByte() != 0;
    }


    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public InfoSession createFromParcel(Parcel in) {
            return new InfoSession(in);
        }

        public InfoSession[] newArray(int size) {
            return new InfoSession[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(employer);
        dest.writeString(date);
        dest.writeString(start_time);
        dest.writeString(end_time);
        dest.writeString(display_time_range);
        dest.writeString(building_code);
        dest.writeString(building_room);
        dest.writeString(building_map_url);
        dest.writeString(website);
        dest.writeString(audience);
        dest.writeString(programs);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(link);
        dest.writeByte((byte) (isCancelled ? 1 : 0));

    }
}
