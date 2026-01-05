package com.example.texttospeech;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button mButtonSpeak;
    private RadioButton radio_en;
    private RadioButton radio_fr;
    private TextToSpeech mTTS;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private AudioManager audioManager = null;
    RadioGroup radioGroup;
    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);
        initControls();
        mButtonSpeak = findViewById(R.id.button_speak);
        RadioButton radio_en = findViewById(R.id.radio_en);
        RadioButton radio_fr = findViewById(R.id.radio_fr);
        radioGroup =  findViewById(R.id.radioGroup);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){

                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(MainActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }else {
                        mButtonSpeak.setEnabled(true);
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mEditText =  findViewById(R.id.edit_text);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                printOutSupportedLanguages();
            setTextToSpeechLanguage();
            }
        });

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
    }


    private void printOutSupportedLanguages() {
        // Supported Languages
        Set<Locale> supportedLanguages = mTTS.getAvailableLanguages();
        if (supportedLanguages != null) {
            for (Locale lang : supportedLanguages) {
                Log.e("TTS", "Supported Language: " + lang);


            }
        }
    }



    private void speak() {
        if (!ready) {
            Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        //text to speak

        String text = mEditText.getText().toString();

        String utteranceId = UUID.randomUUID().toString();
        float pitch = (float) mSeekBarPitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (float) mSeekBarSpeed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);


        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null,utteranceId);
    }
    @Override
    public void onPause() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onPause();
    }

    private Locale getUserSelectedLanguage() {
        int checkedRadioId = this.radioGroup.getCheckedRadioButtonId();
        if (checkedRadioId == R.id.radio_en) {
            return Locale.ENGLISH;
        } else if (checkedRadioId == R.id.radio_fr) {
            return Locale.FRANCE;
        }
        return null;
    }

    private void setTextToSpeechLanguage() {
        Locale language = this.getUserSelectedLanguage();
        if (language == null) {
            this.ready = false;
            Toast.makeText(this, "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = mTTS.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();

        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();

        } else {
            this.ready = true;
            Locale currentLanguage = mTTS.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }


    private void initControls() {
        try {
            SeekBar volumeSeekbar = findViewById(R.id.seek_bar_volume);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
