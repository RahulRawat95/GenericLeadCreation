package com.wings2aspirations.genericleadcreation.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GpsStatusListener extends BroadcastReceiver {
    public interface GpsStatusCallback {
        void isConnected(boolean isConnected);
    }

    private GpsStatusCallback callback;

    public GpsStatusListener() {
    }

    public GpsStatusListener(GpsStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (callback != null) {
            try {
                callback.isConnected(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            } catch (Exception e) {
            }
        }
    }
}
