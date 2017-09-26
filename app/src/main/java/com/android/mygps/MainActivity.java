
package com.android.mygps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static LocationManager mLocationManager;
    private static Geocoder mGeocoder;
    private String provider;
    private static final String TAG = "MY_GPS";
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 3;
    private static boolean fine_permission = false;
    private static boolean coarse_permission = false;
    private static boolean internet_permission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Log.i(TAG, "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    protected void onStart(){
        Log.i(TAG, "MainActivity onStart");

        if (!(checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED) ||
                !(checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        }
        else
        {
            fine_permission = true;
            coarse_permission = true;
        }

        if(fine_permission && coarse_permission)
        {
            //获取当前可用的位置控制器
            List<String> list = mLocationManager.getProviders(true);

            Log.i(TAG, "list size:" + Integer.toString(list.size()));
            for(int i=0; i<list.size(); i++)
            {
                Log.i(TAG, "list " + i + ":" + list.get(i));
            }

            if (list.contains(LocationManager.GPS_PROVIDER)) {
                //是否为GPS位置控制器
                provider = LocationManager.GPS_PROVIDER;
                Log.i(TAG, "location use GPS_PROVIDER");
            }
            else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                //是否为网络位置控制器
                provider = LocationManager.NETWORK_PROVIDER;
                Log.i(TAG, "location use NETWORK_PROVIDER");
            } else {
                Log.i(TAG, "请检查网络或GPS是否打开");
                Toast.makeText(this, "请检查网络或GPS是否打开", Toast.LENGTH_LONG).show();
                return;
            }

            Location location = mLocationManager.getLastKnownLocation(provider);

            mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
            while(location == null)
            {
                mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
                location = mLocationManager.getLastKnownLocation(provider);
            }

            mGeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            if (location != null) {
                //获取当前位置，这里只用到了经纬度
                String string = "纬度为：" + location.getLatitude() + ",经度为："
                        + location.getLongitude() + ", 高度为：" + location.getAltitude();
                Log.i(TAG, string);
                showAddress(location.getLatitude(), location.getLongitude(), location.getAltitude());
            }
            else
            {
                Log.i(TAG, "Can't get location");
                mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
            }
            Log.i(TAG, "MainActivity onStart3");

        }
        else
        {
            Log.i(TAG, "No permission to access GPS");
        }

        super.onStart();
    }

    protected void onResume(){
        Log.i(TAG, "MainActivity onResume start");
        super.onResume();

        Log.i(TAG, "MainActivity onResume end");
    }

    protected void onPause(){
        Log.i(TAG, "MainActivity onPause");
        super.onPause();
    }

    protected void onStop(){
        Log.i(TAG, "MainActivity onStop");

        super.onStop();

    }

    protected void onDestroy(){
        Log.i(TAG, "MainActivity onDestroy");
        mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }

    private void showAddress(double latitude,double longitude, double altitude){

        try{
            List<Address> addrList = mGeocoder.getFromLocation(latitude, longitude, 1);
            if(addrList.size() > 0){
                StringBuilder sb = new StringBuilder();
                String string = "latitude:" + latitude + "\n"
                            + "longitude:" + longitude + "\n"
                            + "altitude:" + altitude + "\n";
                sb.append(string);

                Address addr = addrList.get(0);
                for(int i=0;i<=addr.getMaxAddressLineIndex();i++){
                    sb.append(addr.getAddressLine(i)).append(" ");
                }

                Log.i(TAG, sb.toString());
                TextView tv = (TextView) findViewById(R.id.testview);
                tv.setText(sb.toString());

            }else{
                Log.i(TAG, "address not found!");
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    LocationListener mLocationListener = new LocationListener(){

        public void onLocationChanged(Location location){

            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();
            double altitude = location.getAltitude();

            Log.i(TAG, "Address value:" + "latitude:" + Double.toString(latitude) + "longtitude:" + Double.toString(longtitude));
            showAddress(latitude, longtitude, altitude);
        }

        public void onStatusChanged(String var1, int var2, Bundle var3){

        }

        public void onProviderEnabled(String var1){

        }

        public void onProviderDisabled(String var1){

        }

    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG,"request permission fine location success");
                    fine_permission = true;
                }else {
                    Log.i(TAG,"request permission fine location failed");
                }
                return;
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG,"request permission coarse location success");
                    coarse_permission = true;
                }else {
                    Log.i(TAG,"request permission coarse location failed");
                }
                return;
            case MY_PERMISSIONS_REQUEST_INTERNET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    internet_permission = true;
                }else {
                    Log.i(TAG,"request permission internet failed");
                }
                return;
        }
    }

 }