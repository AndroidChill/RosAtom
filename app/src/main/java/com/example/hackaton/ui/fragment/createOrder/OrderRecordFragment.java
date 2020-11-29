package com.example.hackaton.ui.fragment.createOrder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.hackaton.R;
import com.example.hackaton.databinding.FragmentOrderRecordBinding;
import com.example.hackaton.ui.activity.CreateOrderActivity;
import com.example.hackaton.ui.activity.MainActivity;
import com.icaksama.rapidsphinx.RapidCompletionListener;
import com.icaksama.rapidsphinx.RapidPreparationListener;
import com.icaksama.rapidsphinx.RapidSphinx;
import com.icaksama.rapidsphinx.RapidSphinxListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import edu.cmu.pocketsphinx.Config;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Language;
import ru.yandex.speechkit.OnlineModel;
import ru.yandex.speechkit.OnlineRecognizer;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.speechkit.Track;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class OrderRecordFragment extends Fragment  {

    private FragmentOrderRecordBinding binding;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private RapidSphinx rapidSphinx;

    private boolean isRecording = false;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;


    public static OrderRecordFragment newInstance() {

        Bundle args = new Bundle();

        OrderRecordFragment fragment = new OrderRecordFragment();
        fragment.setArguments(args);
        return fragment;
    }
private boolean active = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_record, container, false);

//        binding.main.setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        speechRecognizer.startListening(speechRecognizerIntent);
//                        Toast.makeText(getContext(), "Down", Toast.LENGTH_SHORT).show();
//                        keeper = "";
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        speechRecognizer.stopListening();
//                        Toast.makeText(getContext(), "Up", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//                return true;
//            }});


        timer = binding.recordTimer;

        onClick();

        checkVoiceCommandPermission();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matchesFound = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matchesFound != null){
                    Toast.makeText(getContext(), String.valueOf(matchesFound.size()), Toast.LENGTH_SHORT).show();
                    keeper = matchesFound.get(0);
                    Toast.makeText(getContext(), keeper, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        binding.recordBtn.setOnClickListener(v -> {
            if(active){
                Toast.makeText(getContext(), "stop", Toast.LENGTH_SHORT).show();
                speechRecognizer.stopListening();
            } else {
                Toast.makeText(getContext(), "start", Toast.LENGTH_SHORT).show();
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            active = !active;
        });

        return binding.getRoot();
    }

    private void checkVoiceCommandPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!(ContextCompat.checkSelfPermission(getContext(), RECORD_AUDIO) == PERMISSION_GRANTED)){
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
                startActivity(intent);
//                getActivity().finish();
            }
        }
    }


    public void onClick() {
//        binding.recordBtn.setOnClickListener(v -> {
//            try{
//                if(isRecording) {
//                    //Stop Recording
//                    stopRecording();
//                    // Change button image and set Recording state to false
//                    binding.recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
//                    isRecording = false;
//                } else {
//                    //Check permission to record audio
//                    if(checkPermissions()) {
//                        //Start Recording
//                        speechRecognizer.startListening(speechRecognizerIntent);
//                        startRecording();
//
//                        // Change button image and set Recording state to false
//                        binding.recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
//                        isRecording = true;
//                    }
//                }
//            } catch (Exception e){
//                Log.d("tag", "onClick: " + e.getMessage());
//            }
//
//        });

    }


    private void stopRecording() {
        //Stop Timer, very obvious
        timer.stop();
        timer.setBase(SystemClock.elapsedRealtime());

        //Change text on page to file saved
        binding.recordFilename.setText("Recording Stopped, File Saved : " + recordFile);

        //Stop media recorder and set it to null for further use to record new audio
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        speechRecognizer.stopListening();
        keeper = "";
    }

    private void startRecording() {
        //Start timer from 0
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".wav";

        binding.recordFilename.setText("Recording, File Name : " + recordFile);

        Log.d("tag", "startRecording: " + recordPath);

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
    }

}
