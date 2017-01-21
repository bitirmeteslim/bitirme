package com.android.park_sistemi.Modules;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

/**
 * Created by User on 20.11.2016.
 */

public class GetCurrentLocation extends Service {
    private LocationListener listener;
    private LocationManager locationManager;
    private Context context;

    @Override
    public void onCreate() {
        //LocationListener nesnemiz ile konum bilgisi almaya çalısıyoruz.
        listener = new LocationListener() {
            //Konumda herhangi bir değişiklik olunca tetiklenecek metod
            @Override
            public void onLocationChanged(Location location) {
                //konum bilgileri alındığında intent oluşturulup broadcastReceiver ile latitude ve longitude
                //parametreleri gönderiliyor.
                Intent intent = new Intent("location_update");
                intent.putExtra("latitude",location.getLatitude());
                intent.putExtra("longitude",location.getLongitude());
                //broadcastReceiver ile intent başlatılıyor.
                sendBroadcast(intent);
            }

            //LocationListener'ın statüsünde herhangi bir değişiklik olunca tetiklenecek metod.
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            //Provider kullanılabilir duruma geldiğinde tetiklenecek metod.
            @Override
            public void onProviderEnabled(String provider) {

            }

            //Provider kullanılamaz duruma geldiğinde tetiklenecek metod
            @Override
            public void onProviderDisabled(String provider) {
                //Provider'ın durumu disable durumuna geldiğinde Intent Flag Activity ile
                //Yeni görev bayrağı aktif hale getirilerek provider'ın durumu güncelleniyor.
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates("gps",3000,0,listener);
    }

    //Activity kapanmadan önce en son tetiklenecek metod.
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //tüm konum güncellemeleri siliniyor.
            locationManager.removeUpdates(listener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
