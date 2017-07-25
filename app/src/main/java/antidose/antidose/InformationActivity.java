package antidose.antidose;

import android.annotation.TargetApi;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class InformationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    private boolean onSilence = false;
    private Button btnSpeak;
    private Button btnNext;
    private Button btnPrev;
    private String[] steps = {"First Stimulate.  Rub your knuckles hard on the victims sternum.  Try to wake them. Call 911 if they are unresponsive.",
            "Ensure that the airway of the victim is clear and unobstructed.",
            "If the airway is clear, plug their nose, tilt their head back, and breathe for the victim.  Compressions are not necessary, simply provide air.  One breath every 5 seconds.",
            "Take a moment and evaluate the situation, is the person coming around?  Are they breathing alone?  They may need Naloxone.",
            "If naloxone is required, inject 1 milli liter into a large muscle like the leg or butt.  And continue to breathe for them.",
            "Evaluate again, is the person breathing?  If after 5 minutes they have not woken, another dose is needed."};
    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        tts = new TextToSpeech(this, this);

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

        btnSpeak = (Button) findViewById(R.id.readSteps);
        btnSpeak.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(tts.isSpeaking()) {
                    btnSpeak.setText("Read Steps");
                    tts.stop();
                }
                else {
                    btnSpeak.setText("Stop");
                    startSpeech(currentStep = 0);
                }
            }
        });

        btnNext = (Button) findViewById(R.id.nextStep);
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
        btnPrev = (Button) findViewById(R.id.prevStep);
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

    @TargetApi(21)
    private void startSpeech(int firstStep){
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            tts.speak("", TextToSpeech.QUEUE_FLUSH, null, null);
            for (int theStep = firstStep; theStep < steps.length; theStep++) {
                speak(steps[theStep], Integer.toString(theStep));
            }
        }
    }

    @TargetApi(21)
    public void speak(String text, String id){
        if(android.os.Build.VERSION.SDK_INT >= 21){
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, id);
            tts.playSilentUtterance(2000, TextToSpeech.QUEUE_ADD, id+"silence");
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
