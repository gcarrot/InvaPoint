package si.urban.invapoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    // GPSTracker class
    GPSTracker gps;

    private String Phone1, Phone2, SMSContent;
    private boolean GPSEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        gps = new GPSTracker(Settings.this);
        gps.stopUsingGPS();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String SettingsTitle = getResources().getString(R.string.SettingsTitle_sl);
        getSupportActionBar().setTitle(SettingsTitle);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        SharedPreferences settings = getSharedPreferences("SMSValues", 0);
        Phone1 = settings.getString("phoneNumer1", "");
        Phone2 = settings.getString("phoneNumer2", "");
        SMSContent = settings.getString("smsContent", "");
        GPSEnabled = settings.getBoolean("gpsEnabled", true);


        EditText etPhone1 = (EditText) findViewById(R.id.etPhone1);
        EditText etPhone2 = (EditText) findViewById(R.id.etPhone2);

        EditText etSMS = (EditText) findViewById(R.id.etSMS);
        Switch swGPS = (Switch) findViewById(R.id.swGPS);

        if(Phone1.trim() != ""){
            etPhone1.setText(Phone1);
        }
        if(Phone2.trim() != ""){
            etPhone2.setText(Phone2);
        }
        if(SMSContent.trim() != ""){
            etSMS.setText(SMSContent);
        }

        swGPS.setChecked(GPSEnabled);

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etPhone1 = (EditText) findViewById(R.id.etPhone1);
                EditText etPhone2 = (EditText) findViewById(R.id.etPhone2);

                EditText etSMS = (EditText) findViewById(R.id.etSMS);
                Switch swGPS = (Switch) findViewById(R.id.swGPS);

                SharedPreferences sp = getSharedPreferences("SMSValues", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("phoneNumer1", etPhone1.getText().toString());
                editor.putString("phoneNumer2", etPhone2.getText().toString());
                editor.putString("smsContent", etSMS.getText().toString());
                editor.putBoolean("gpsEnabled", swGPS.isChecked());
                editor.commit();


                Toast.makeText(getApplicationContext(),"Settings updated",Toast.LENGTH_LONG).show();
            }
        });
    }




}