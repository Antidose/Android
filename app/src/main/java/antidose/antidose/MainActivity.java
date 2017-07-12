package antidose.antidose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;

public class MainActivity extends AppCompatActivity {

    AnimationDrawable alertAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //animation
        ImageButton alertImage = (ImageButton) findViewById(R.id.imageButton);
        alertImage.setImageResource(R.drawable.animatedalert);
        alertImage.setScaleType(ScaleType.FIT_CENTER);
        alertAnimation = (AnimationDrawable) alertImage.getDrawable();
        alertAnimation.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }


    public void goNavigate(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);

    }

    public void register(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }


}
