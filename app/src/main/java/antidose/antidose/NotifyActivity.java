package antidose.antidose;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class NotifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        TextView distance = (TextView) findViewById(R.id.textViewDistance);
        TextView direction = (TextView) findViewById(R.id.textViewDirection);
        TextView time = (TextView) findViewById(R.id.textViewTime);

        updateDistance(distance);
        updateDirection(direction);
        updateTime(time);

    }

    public void canGo(View view) {
        String hasKit = view.getTag().toString();
        if (hasKit == "true") {
            //haskit
        } else {
            //no kit
        }
        //do stuff
        //get map
        //idk this is hard
    }

    public void cannotGo(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void updateDistance(TextView text) {

        //get the distance from server
        int KM = 10;
        text.setText(KM + " KM");
    }

    public void updateDirection(TextView text) {

        //get cardinality from server
        String dir = "NORTH";
        text.setText(dir);

    }

    public void updateTime(TextView text) {
        //get time to drive from server
        String time = "10:25";  //might be a time object or something
        text.setText(time + " DRIVE");
    }
}

