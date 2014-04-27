package com.vnkmpn.sentencediagrammer;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener {


	protected static final int REQUEST_OK = 1;

	private SpeechRecognizer sr;
	
	private SentenceRecognitionListener listener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.imageButton1).setOnTouchListener(this);

		sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

		listener = new SentenceRecognitionListener(getApplicationContext());

		sr.setRecognitionListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClick(View arg0) {
		sr.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
	}


	public void setSentenceText(String text) {
		((TextView)findViewById(R.id.textView1)).append(text);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN: {
			sr.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
			listener.onBeginningOfSpeech();
			break;
		}

		case MotionEvent.ACTION_UP: {
			listener.onEndOfSpeech();
			sr.stopListening();
			Log.d("Main", "appending data: " + listener.getSentence() + "***");
			((TextView)findViewById(R.id.textView1)).append(listener.getSentence());
			break;
		}
		}
		return false;
	}
}
