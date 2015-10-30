package com.example.okubo.onsenkensaku;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

/**
 * Created by Okubo on 9/30/2015 030.
 */
public class CurrentLocationFinder {
    private static final float MIN_DISTANCE = 0;
    private static final int MIN_INTERVAL = 10000;

    private Context mContext;
    private LocationManager mLocationManager;
//    private String mBestProvider;
    List<String> mProviders;
    private Location mCurrentMyLocation;
    private boolean mLocationSearchFlag = false;

    public CurrentLocationFinder(Context context) {
        mContext = context;
    }

    public boolean hasCurrentLocationData(){
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getString(R.string.preferenceName), Context.MODE_PRIVATE);
        if(preferences.getString(mContext.getString(R.string.preferenceKeyLatitude), "").equals("") || preferences.getString(mContext.getString(R.string.preferenceKeyLongitude), "").equals("")){
            return false;
        }
        mCurrentMyLocation = new Location("");
        mCurrentMyLocation.setLatitude(Double.parseDouble(preferences.getString(mContext.getString(R.string.preferenceKeyLatitude), "")));
        mCurrentMyLocation.setLongitude(Double.parseDouble(preferences.getString(mContext.getString(R.string.preferenceKeyLongitude), "")));

        return true;
    }

    public boolean isLocationSearching(){
        return  mLocationSearchFlag;
    }

    public void initializeLocationSearching(){
        mLocationSearchFlag = false;
    }

    public boolean checkGPSSetting(){
        mLocationManager = (LocationManager)mContext.getSystemService(mContext.LOCATION_SERVICE);
        mProviders =  mLocationManager.getAllProviders();

        if(mLocationManager == null || mProviders.size() == 0){
            return false;
        }else{
            if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                return false;
            }
            return true;
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocationSearchFlag = true;
            mCurrentMyLocation = location;
            removeLocationListener();
            SharedPreferences preference = mContext.getSharedPreferences(mContext.getString(R.string.preferenceName),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString(mContext.getString(R.string.preferenceKeyLatitude), String.valueOf(location.getLatitude()));
            editor.putString(mContext.getString(R.string.preferenceKeyLongitude), String.valueOf(location.getLongitude()));
            editor.commit();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void getCurrentLocation(){
        for(int i = 0 ; i < mProviders.size() ; i++){
            mLocationManager.requestLocationUpdates(mProviders.get(i), MIN_INTERVAL, MIN_DISTANCE, mLocationListener);
        }
    }

    public double getCurrentLatitude(){
        return mCurrentMyLocation.getLatitude();
    }

    public double getCurrentLongitude(){
        return mCurrentMyLocation.getLongitude();
    }

    public void removeLocationListener(){
        if(mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
