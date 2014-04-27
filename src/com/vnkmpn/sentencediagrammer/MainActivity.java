package com.vnkmpn.sentencediagrammer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener {


	protected static final int REQUEST_OK = 1;

	private SpeechRecognizer sr;

	private SentenceRecognitionListener listener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf"); 
		((TextView)findViewById(R.id.titleText)).setTypeface(type);
		
		((TextView)findViewById(R.id.sentenceText)).setTypeface(type);

		findViewById(R.id.listenButton).setOnTouchListener(this);

		sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

		listener = new SentenceRecognitionListener(getApplicationContext());

		sr.setRecognitionListener(listener);

		startAnimation();
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
		((TextView)findViewById(R.id.sentenceText)).append(text);
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
			((TextView)findViewById(R.id.sentenceText)).append(listener.getSentence());
			break;
		}
		}
		return false;
	}


	@SuppressLint("NewApi")
	public void startAnimation() {
		long firstStageDuration = 2000;
		long secondStageDuration = 500;
		float firstStageDistance = -500f;
		float secondStageDistance = 100f;
		ImageButton button = (ImageButton) findViewById(R.id.listenButton);
		TextView titleText = (TextView) findViewById(R.id.titleText);

		ObjectAnimator buttonDropIn = ObjectAnimator.ofFloat(button,
				"translationY", firstStageDistance, secondStageDistance);
		buttonDropIn.setDuration(firstStageDuration);
		ObjectAnimator buttonFadeIn = ObjectAnimator.ofFloat(button, "alpha",
				0f, 1f);
		buttonFadeIn.setDuration(firstStageDuration);
		
		ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(titleText, "alpha",
				0f, 1f);
		titleFadeIn.setDuration(firstStageDuration);
		
		ObjectAnimator bounceUp = ObjectAnimator.ofFloat(button,  "translationY", secondStageDistance, 0f);
		bounceUp.setDuration(secondStageDuration);

		AnimatorSet stageOne = new AnimatorSet();

		stageOne.play(buttonDropIn).with(buttonFadeIn).with(titleFadeIn);

		
		ObjectAnimator titleFadeOut = ObjectAnimator.ofFloat(titleText, "alpha",  1f, 0f);
		titleFadeOut.setDuration(secondStageDuration);
		
		AnimatorSet stageTwo = new AnimatorSet();

		stageTwo.play(bounceUp).with(titleFadeOut).after(stageOne);
		stageTwo.start();

	}
}
