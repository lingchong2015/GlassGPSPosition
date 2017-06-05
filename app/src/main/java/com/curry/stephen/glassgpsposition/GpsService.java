package com.curry.stephen.glassgpsposition;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lingchong on 17/5/18.
 */

public class GpsService extends Service implements Handler.Callback  {

    private LocationManager mLocationManager;

    private HandlerThread mHandlerThread;

    private Handler mHandler;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "location output: " + location.toString());
            Intent intent = new Intent(MainActivity.RECEIVE_GPS_INFO);
            intent.putExtra(getString(R.string.latitude), location.getLatitude());
            intent.putExtra(getString(R.string.longitude), location.getLongitude());
            sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private static final long MILLIS_BETWEEN_LOCATIONS = TimeUnit.SECONDS.toMillis(0);

    private static final long METERS_BETWEEN_LOCATIONS = 0;

    public static final int MESSAGE_FOR_HANDLER = 1;

    private static final String TAG = GpsService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendMessage2HandlerThread();

        return START_STICKY;
    }

    private void sendMessage2HandlerThread() {
        Message message = new Message();
        message.what = MESSAGE_FOR_HANDLER;
        mHandler.sendMessage(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_FOR_HANDLER: {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);

                List<String> providers =
                        mLocationManager.getProviders(criteria, true);
                for (String provider : providers) {
                    if (PackageManager.PERMISSION_GRANTED == checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") &&
                            PackageManager.PERMISSION_GRANTED == checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") ) {
                        mLocationManager.requestLocationUpdates(provider, 5000, METERS_BETWEEN_LOCATIONS, mLocationListener,
                                mHandlerThread.getLooper());
                    }
                }

                Log.i(TAG, "sendMessage2HandlerThread");
                break;
            }
        }

        return true;
    }
}
