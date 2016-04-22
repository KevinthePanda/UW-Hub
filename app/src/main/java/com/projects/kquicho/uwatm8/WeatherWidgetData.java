package com.projects.kquicho.uwatm8;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 4/22/2016.
 */
public class WeatherWidgetData extends UWData implements Parcelable{

    private double mCurrentTemp;
    private double mWindChill;
    private double mMaxTemp;
    private double mMinTemp;
    private double mPrecip;
    private double mHumidity;
    private double mWindSpeed;

    private WeatherWidgetData(double currentTemp, double windChill, double maxTemp,
                              double minTemp, double precip, double humidity, double windSpeed) {
        super(WeatherWidget.TAG);
        finishedLoading();
        mCurrentTemp = currentTemp;
        mWindChill = windChill;
        mMaxTemp = maxTemp;
        mMinTemp = minTemp;
        mPrecip = precip;
        mHumidity = humidity;
        mWindSpeed = windSpeed;
    }

    public WeatherWidgetData(Parcel in){
        super(in);
        mCurrentTemp = in.readDouble();
        mWindChill = in.readDouble();
        mMaxTemp = in.readDouble();
        mMinTemp = in.readDouble();
        mPrecip = in.readDouble();
        mHumidity = in.readDouble();
        mWindSpeed = in.readDouble();
    }

    public double getCurrentTemp() {
        return mCurrentTemp;
    }

    public double getWindChill() {
        return mWindChill;
    }

    public double getMaxTemp() {
        return mMaxTemp;
    }

    public double getMinTemp() {
        return mMinTemp;
    }

    public double getPrecip() {
        return mPrecip;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public static class Builder{
        private double mCurrentTemp = 0;
        private double mWindChill = 0;
        private double mMaxTemp = 0;
        private double mMinTemp = 0;
        private double mPrecip = 0;
        private double mHumidity = 0;
        private double mWindSpeed = 0;

        public Builder currentTemp(double num){
            mCurrentTemp = num;
            return this;
        }

        public Builder windChill(double num){
            mWindChill = num;
            return this;
        }

        public Builder maxTemp(double num){
            mMaxTemp = num;
            return this;
        }

        public Builder minTemp(double num){
            mMinTemp = num;
            return this;
        }

        public Builder precip(double num){
            mPrecip = num;
            return this;
        }

        public Builder humidity(double num){
            mHumidity = num;
            return this;
        }

        public Builder windSpeed(double num){
            mWindSpeed = num;
            return this;
        }

        public WeatherWidgetData createWeatherData(){
            return new WeatherWidgetData(mCurrentTemp, mWindChill, mMaxTemp, mMinTemp, mPrecip,
                    mHumidity, mWindSpeed);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(mCurrentTemp);
        dest.writeDouble(mWindChill);
        dest.writeDouble(mMaxTemp);
        dest.writeDouble(mMinTemp);
        dest.writeDouble(mPrecip);
        dest.writeDouble(mHumidity);
        dest.writeDouble(mWindSpeed);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public WeatherWidgetData createFromParcel(Parcel in) {
            return new WeatherWidgetData(in);
        }

        public WeatherWidgetData[] newArray(int size) {
            return new WeatherWidgetData[size];
        }
    };
}
