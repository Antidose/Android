package antidose.antidose;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ContactEMSActivity extends AppCompatActivity {
    Button smsButton;
    final private int MY_PERMISSIONS_REQUEST_SEND_SMS = 123;

    protected boolean checkSMSPermissions(){
        int checkSMSPermission = ContextCompat.checkSelfPermission(ContactEMSActivity.this, Manifest.permission.SEND_SMS);
        if (checkSMSPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactEMSActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            return false;
        }
        else {
            // Permission Granted
            return true;
        }
    }

    protected void hideSMSButton(){
        smsButton = (Button) findViewById(R.id.smsEMS);
        smsButton.setVisibility(View.GONE);
    }

    protected void sendSMS(){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("2505077525", null, "This is a test`", null, null);
        Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSMS();
                    hideSMSButton();
                }
                else {
                    // permission denied, do nothing
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_ems);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Button dialerButton = (Button) findViewById(R.id.openDialer);
        dialerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:250-507-7525"));
                startActivity(callIntent);
            }
        });
        Button smsButton = (Button) findViewById(R.id.smsEMS);
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(checkSMSPermissions()) {
                    sendSMS();
                    hideSMSButton();
                }
                else {
                    // Do nothing
                }
            }


        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);

    }
}
