package antidose.antidose;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.support.v4.content.ContextCompat;

public class SettingActivity extends AppCompatActivity implements DeleteConfirmFragment.DeleteDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //Active/Inactive Status Button
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        final TextView active = (TextView) findViewById(R.id.textViewActive);
        final TextView inactive = (TextView) findViewById(R.id.textViewInactive);
        setStatus(toggle, active, inactive);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    buttonView.setBackgroundResource(R.color.colorSuccess);
                    inactive.setVisibility(View.INVISIBLE);
                    active.setVisibility(View.VISIBLE);
                    //make a DB call to save state

                }
                else {
                    buttonView.setBackgroundResource(R.color.colorPrimary);
                    active.setVisibility(View.INVISIBLE);
                    inactive.setVisibility(View.VISIBLE);
                    //make a DB call to save state

                }
            }
        });

    }


    public void setStatus(ToggleButton btn, TextView on, TextView off){

        //make a DB call to get if the user is set to inactive or active
        //set the boolean and the rest of the code should be guuci
        boolean isActive = true;
        if(isActive){
            btn.setChecked(true);
            btn.setBackgroundResource(R.color.colorSuccess);
            off.setVisibility(View.INVISIBLE);
            on.setVisibility(View.VISIBLE);


        }else{
            btn.setChecked(false);
            btn.setBackgroundResource(R.color.colorPrimary);
            on.setVisibility(View.INVISIBLE);
            off.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showDeleteDialog(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DeleteConfirmFragment();
        dialog.show(getSupportFragmentManager(), "DeleteConfirmFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        //Do the delete
        //deleteAccount();
        //return to main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Cancel
    }


    public void deleteAccount(){
        //do server stuff here

    }

    }

