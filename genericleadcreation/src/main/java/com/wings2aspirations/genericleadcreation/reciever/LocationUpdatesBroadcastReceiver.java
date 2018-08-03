package com.wings2aspirations.genericleadcreation.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;

/**
 * Created by Rahul on 20-Oct-17.
 */

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action.PROCESS_UPDATES";

    public interface ReceiverCallback {
        void hasLocationAvailability(LocationAvailability locationAvailability);

        void hasLocationResult(LocationResult locationResult);
    }

    private static ReceiverCallback callback;

    public static void setCallback(ReceiverCallback callback1) {
        callback = callback1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action) && callback != null) {
                if (LocationAvailability.hasLocationAvailability(intent)) {
                    callback.hasLocationAvailability(LocationAvailability.extractLocationAvailability(intent));
                }
                if (LocationResult.hasResult(intent)) {
                    callback.hasLocationResult(LocationResult.extractResult(intent));
                }
            }
        }
    }
}
