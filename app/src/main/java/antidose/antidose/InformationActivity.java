package antidose.antidose;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Locale;

public class InformationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    private boolean onSilence = false;
    private Button btnSpeak;
    private Button btnNext;
    private Button btnPrev;
    private String[] steps = {"First Stimulate.  Rub your knuckles hard on the victims sternum.  Try to wake them. Call nine one one if they are unresponsive.",
            "Check that the airway of the victim is clear and unobstructed.",
            "If the airway is clear, plug their nose, tilt their head back, and breathe for the victim.  Compressions are not necessary, simply provide air.  One breath every 5 seconds.",
            "Take a moment and evaluate the situation, is the person coming around?  Are they breathing alone?  They may need Naloxone.",
            "If naloxone is required, inject 1 milli liter into a large muscle like the leg or butt.  And continue to breathe for them.",
            "Evaluate again, is the person breathing?  If after 5 minutes they have not woken, another dose is needed."};
    private int currentStep = 0;

    @Override
    protected void onNewIntent(Intent savedIntent)
    {
        super.onNewIntent(savedIntent);
        onActive();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onActive();
    }

    protected void onActive()
    {
        setContentView(R.layout.activity_information);

        tts = new TextToSpeech(this, this);
        btnSpeak = (Button) findViewById(R.id.readSteps);
        btnNext = (Button) findViewById(R.id.nextStep);
        btnPrev = (Button) findViewById(R.id.prevStep);
        btnNext.setVisibility(View.GONE);
        btnPrev.setVisibility(View.GONE);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if(utteranceId.contains("silence"))
                {
                    onSilence = false;
                    currentStep += 1;
                }
                else{
                    onSilence = true;

                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });

        btnSpeak.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(tts.isSpeaking()) {
                    btnSpeak.setText("Read Steps");
                    btnNext.setVisibility(View.GONE);
                    btnPrev.setVisibility(View.GONE);
                    tts.stop();
                }
                else {
                    btnSpeak.setText("Stop");
                    btnNext.setVisibility(View.VISIBLE);
                    btnPrev.setVisibility(View.VISIBLE);
                    startSpeech(currentStep = 0);
                }
            }
        });


        btnNext.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currentStep + 1 < steps.length) {
                    onSilence = false;
                    startSpeech(currentStep += 1);
                }
                else{
                    tts.stop();
                }
            }
        });
        btnPrev.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currentStep - 1 >= 0) {
                    if (onSilence == true) {
                        onSilence = false;
                        startSpeech(currentStep);
                    }
                    else {
                        startSpeech(currentStep -= 1);
                    }
                }
                else{
                    startSpeech(currentStep = 0);
                }
            }
        });
    }

    private void startSpeech(int firstStep){
        speak("", TextToSpeech.QUEUE_FLUSH, null);
        for (int theStep = firstStep; theStep < steps.length; theStep++) {
            speak(steps[theStep], TextToSpeech.QUEUE_ADD, Integer.toString(theStep));
            speakSilent(10000,Integer.toString(theStep) + "silence");
        }
    }

    @SuppressWarnings("deprecation")
    public void speak(String text, int queueMode, String id){
        if(android.os.Build.VERSION.SDK_INT >= 21){
            tts.speak(text, queueMode, null, id);
        }
        else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, queueMode, map);
        }
    }

    @SuppressWarnings("deprecation")
    public void speakSilent(int length, String id){
        if(android.os.Build.VERSION.SDK_INT >= 21){
            tts.playSilentUtterance(length, TextToSpeech.QUEUE_ADD, id);
        }
        else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.playSilence(length, TextToSpeech.QUEUE_ADD, map);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            tts.setSpeechRate((float)0.9);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }



    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
