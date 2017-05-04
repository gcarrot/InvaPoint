package si.urban.invapoint;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class MainActivity2 extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private String BatteryLevel = "";
    private String Phone1, Phone2, SMSContent;
    private boolean GPSEnabled;

    double latitude, longitude;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(Html.fromHtml("<big>InvaPOINT</big></div>"));
        final ImageButton btnShowLocation = (ImageButton) findViewById(R.id.btnHelp);
        Button btnSettings = (Button) findViewById(R.id.btnSettings);


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences settings = getSharedPreferences("SMSValues", 0);
        Phone1 = settings.getString("phoneNumer1", "");
        Phone2 = settings.getString("phoneNumer2", "");
        SMSContent = settings.getString("smsContent", "");
        GPSEnabled = settings.getBoolean("gpsEnabled", true);

        if (SMSContent == "") {
            SMSContent = "Nekaj se je zgodilo. Potrebujem pomoc.";
        }

        if (Phone1 == "" && Phone2 == "") {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Prosim vnesite vsaj eno mobilno številko.", Toast.LENGTH_LONG).show();
        }

        TextView tvText = (TextView) findViewById(R.id.tvText);
        //tvText.setText("");
        tvText.setVisibility(View.VISIBLE);

        final RelativeLayout loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.GONE);

        final TextView tvLoading = (TextView) findViewById(R.id.tvTextLoading);
        tvLoading.setText("Vaša lokacija se še nalaga prosimo počakajte");
        tvLoading.setVisibility(View.GONE);

        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
            // Do something for Kitkat and less versions
            btnSettings.setVisibility(View.VISIBLE);
        }

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
            }
        });


        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(this, "GPS is  not Enabled in your devide", Toast.LENGTH_SHORT).show();
            //settingsrequest();
        }

        /*if (checkGooglePlayServices()) {
            buildGoogleApiClient();
            //prepare connection request
            createLocationRequest();
        }*/


        batteryLevel();

        //final RelativeLayout loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        //loadingPanel.setVisibility(View.GONE);
        //final TextView tvLoading = (TextView) findViewById(R.id.tvTextLoading);

        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getLocation();

                loadingPanel.setVisibility(View.VISIBLE);
                tvLoading.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.i("Urban", "test 123");
                        checkIfOkSendSms();
                    }
                }, 500);


            }
        });
    }


    public void checkIfOkSendSms(){
        RelativeLayout loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        //loadingPanel.setVisibility(View.GONE);
        TextView tvLoading = (TextView) findViewById(R.id.tvTextLoading);

        //loadingPanel.setVisibility(View.VISIBLE);
        //tvLoading.setVisibility(View.VISIBLE);
        int value = 0;
        boolean GoForward = true;
        while(longitude == 0.0 && latitude == 0.0){
            // Show loading screen;
            Log.e("Urban", "Message: iscem lokacijo");
            if(value == 1) {

            }

            if ( value > 10000 ) {
                // I GPS not found then break while loop
                GoForward = false;
                Toast.makeText(this, "GPS ni najden poskusite ponovno!", Toast.LENGTH_SHORT).show();

                tvLoading.setVisibility(View.GONE);
                loadingPanel.setVisibility(View.GONE);
                break;
            }
            getLocation();

            value +=1;
        }

        // IF no error while loading GPS then continue.
        if(GoForward){
            tvLoading.setVisibility(View.GONE);
            loadingPanel.setVisibility(View.GONE);

            Log.i("Urban", "GPS začetek");
            String message = SMSContent + " Baterije: " + BatteryLevel + ". ";


            message += "Moja lokacija:  http://maps.google.com/?daddr=" + latitude + "," + longitude + " ";
            //message +=  " My location is https://www.google.com/maps?q="+latitude+","+longitude+"";


            Log.i("Urban", "Message: " + message);

            if (latitude != 0.0 && longitude != 0.0)  {
                sendSMSMessage(message);
            }
        }

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //process your onClick here
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about_app) {
            //process your onClick here
            Intent intent = new Intent(getApplicationContext(), AboutApp.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void batteryLevel() {

        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int level = -1;
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                BatteryLevel = +level + "%";
                //batterLevel.setText("Battery Level Remaining: " + level + "%");
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

    }

    private boolean checkGooglePlayServices() {
        Log.i("Urban", "Test checkGooglePlayServices");


        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);

        Log.i("Urban", "Test c: " + checkGooglePlayServices);
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
              /*
               * google play services is missing or update is required
               *  return code could be
               * SUCCESS,
               * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
               * SERVICE_DISABLED, SERVICE_INVALID.
               */
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e("Urban e", "Error: " +requestCode);
        if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES) {

            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Google Play Services must be installed.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public  GoogleApiClient getInstance(){
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(MainActivity2.this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }

    public void settingsrequest()
    {
        Log.e("settingsrequest","Comes");


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();


                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                             Log.e("Application","Button Clicked");

                            //checkIfOkSendSms();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                             Log.e("Application","Button Clicked1");

                            //checkIfOkSendSms();
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity2.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                                Log.e("Applicationsett",e.toString());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Log.e("Application","Button Clicked2");
                            Toast.makeText(MainActivity2.this, "Location is Enabled", Toast.LENGTH_SHORT).show();
                            break;
                    }

            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());


    }


    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


        if (mGoogleApiClient.isConnected()) {
            Log.i("Urban", "Google_Api_Client: It was connected on (onConnected) function, working as it should.");
        } else {
            Log.i("Urban", "Google_Api_Client: It was NOT connected on (onConnected) function, It is definetly bugged.");
        }


        if (mLastLocation != null) {

            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();


            //Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();

        }

        startLocationUpdates();

    }


    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            //Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /* Second part*/

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();

        //Toast.makeText(this, "Update -> Latitude:" + mLastLocation.getLatitude()+", Longitude:"+mLastLocation.getLongitude(),Toast.LENGTH_LONG).show();

    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }


    }


    private void sendSMSMessage(String message) {
        Log.i("Send SMS", "");
        String phoneNo = "";
        String phoneNo2 = "";

        if (Phone1.trim() != "") {
            phoneNo = Phone1.substring(0, 3) + "-" + Phone1.substring(3, Phone1.length());
        }
        if (Phone2.trim() != "") {
            phoneNo2 = Phone2.substring(0, 3) + "-" + Phone2.substring(3, Phone1.length());
        }

        Log.i("Urban", "Phone num 1: " + phoneNo);
        Log.i("Urban", "Phone num 2: " + phoneNo2);


        Log.i("Urban", "Message send: " + message);

        final TextView tvText = (TextView) findViewById(R.id.tvText);

        try {

            if (Phone1 != "") {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);
                Log.i("Urban", "Phone num 1 send: " + phoneNo);
            }

            if (Phone2.trim() != "") {
                SmsManager smsManager2 = SmsManager.getDefault();
                smsManager2.sendTextMessage(phoneNo2, null, message, null, null);
                Log.i("Urban", "Phone num 2 send: " + phoneNo2);
            }
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();


            final ImageButton btnShowLocation = (ImageButton) findViewById(R.id.btnHelp);
            btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.gumb_zelen));


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    final ImageButton btnShowLocation = (ImageButton) findViewById(R.id.btnHelp);
                    btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.gumb_rdec));

                    TextView tvLoading = (TextView) findViewById(R.id.tvTextLoading);
                    tvLoading.setVisibility(View.GONE);
                }
            }, 15000);

            tvText.setText("SMS: " + SMSContent + "\nBATERIJA: " + BatteryLevel + "\nLOKACIJA: " + latitude + "," + longitude + "\nPOTRDITEV: SMS POSLAN / PREJETO");
            tvText.setVisibility(View.VISIBLE);

            //gps.stopUsingGPS();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}