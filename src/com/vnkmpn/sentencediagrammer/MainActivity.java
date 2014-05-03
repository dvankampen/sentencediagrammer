package com.vnkmpn.sentencediagrammer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements OnClickListener {

	static TextView sentenceView;

	private SpeechRecognizer sr;	

	private SentenceRecognitionListener listener;

	private static String[] words;
	private static int wordIndex = 0;
	private String key = "INVALID";
	@SuppressLint("HandlerLeak")
	private final Handler updateSentenceViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			sentenceView.append(Html.fromHtml(msg.obj.toString()));
			sentenceView.invalidate();
			updateSentenceViewHandler.postDelayed(updateSentenceView,250);
		}
	};

	private final Runnable updateSentenceView = new Runnable() {

		@SuppressLint("DefaultLocale")
		@Override
		public void run() {

			if (wordIndex == (words.length) ) {
				updateSentenceViewHandler.removeCallbacks(this);
				return;
			}

			Message msg = updateSentenceViewHandler.obtainMessage();
			msg.obj = new Word(getApplicationContext(), words[wordIndex], key).colorize();

			updateSentenceViewHandler.sendMessage(msg);
			wordIndex++;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf"); 

		sentenceView = (TextView)findViewById(R.id.sentenceText);
		sentenceView.setTypeface(type);

		sentenceView.setTypeface(type);

		findViewById(R.id.listenButton).setOnClickListener(this);

		sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

		listener = new SentenceRecognitionListener();

		sr.setRecognitionListener(listener);

		try {
			InputStream inputStream = getAssets().open("dictionary.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			Log.d("Main","dictionary properties are loaded");
			key = properties.getProperty("KEY");
		} catch (IOException e) {
			e.printStackTrace();
		}
		startAnimation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			((TextView)findViewById(R.id.sentenceText)).setText("");
			break;
		case R.id.menu_save:
			showMsg("Save");
			break;
		case R.id.menu_preferences:
			showMsg("preferences!");
			break;
		case R.id.menu_about:
			LayoutInflater layoutInflater 
			= (LayoutInflater)getBaseContext()
			.getSystemService(LAYOUT_INFLATER_SERVICE);  
			View popupView = layoutInflater.inflate(R.layout.about_popup, null);  
			final PopupWindow popupWindow = new PopupWindow(
					popupView, 
					LayoutParams.WRAP_CONTENT,  
					LayoutParams.WRAP_CONTENT); 

			Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
			btnDismiss.setOnClickListener(new Button.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					popupWindow.dismiss();
				}});

			popupWindow.showAtLocation(this.findViewById(R.id.listenButton), Gravity.CENTER, 0, 0);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showMsg(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
		toast.show();
	}

	public void onClick(View arg0) {
		recording(true);
		sr.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
	}


	public void setSentenceText(String text) {
		((TextView)findViewById(R.id.sentenceText)).append(text);
	}

	@SuppressLint("NewApi")
	public void startAnimation() {
		long firstStageDuration = 1500;
		long secondStageDuration = 500;
		float firstStageDistance = -500f;
		float secondStageDistance = 100f;
		final ImageButton button = (ImageButton) findViewById(R.id.listenButton);
		TextView titleText = (TextView) findViewById(R.id.titleText);

		button.setEnabled(false);

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

		button.postDelayed(new Runnable() {
			@Override
			public void run() {
				button.setBackgroundResource(R.drawable.greenroundcorners);
				button.setEnabled(true);
			}
		}, firstStageDuration + secondStageDuration);

		//diagramSentence("Egads the evil teacher assigns us work daily and expects it on his desk by eight the next morning");
	}

	/**
	 * 
	 * @author dave
	 *
	 */

	class SentenceRecognitionListener implements RecognitionListener{

		private String bestSentence = "";

		@Override
		public void onBeginningOfSpeech() {
			Log.d("Speech", "onBeginningOfSpeech");
			bestSentence = "";
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			Log.d("Speech", "onBufferReceived");
		}

		@Override
		public void onEndOfSpeech() {
			Log.d("Speech", "onEndOfSpeech");
		}

		@Override
		public void onError(int error) {
			Log.d("Speech", "onError");

			signifyWarning();

			//recording(false);
			switch (error)
			{
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				Log.d("Speech", "insufficient permissions");
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				Log.d("Speech", "recognizer busy");
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				Log.d("Speech", "timeout");
				break;
			default:
				Log.d("Speech", "error: " + error);
				break;
			}
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.d("Speech", "onEvent");
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			Log.d("Speech", "onPartialResults");
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			Log.d("Speech", "onReadyForSpeech");
		}


		@SuppressLint("DefaultLocale")
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public void onResults(Bundle results) {
			Log.d("Speech", "onResults");
			recording(false);

			/* clear the text view */
			sentenceView.setText("");
			wordIndex = 0;

			float[] confidenceList = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			int bestGuessIndex = 0;
			float highestConfidence = 0;
			for (int i = 0; i < confidenceList.length; i++) {
				if (confidenceList[i] >= highestConfidence) {
					highestConfidence = confidenceList[i];
					bestGuessIndex = i;
				}
			}
			ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			if (highestConfidence > .6) {
				Log.d("Speech", "resultant string=" + strlist.get(bestGuessIndex) + ", with confidence of " + highestConfidence);
				bestSentence = strlist.get(bestGuessIndex);
			} else {
				Log.d("Speech", "low confidence");
				showMsg( "Please say that again...");
				return;
			}
			diagramSentence(bestSentence);
		}


		@Override
		public void onRmsChanged(float rmsdB) {
		}
	}

	private void recording(boolean enabled) 
	{
		long duration = 100;
		float distance = -50f;
		float origin;
		float destination;
		final int nextColor;

		final ImageButton button = (ImageButton) findViewById(R.id.listenButton);

		origin = 0;
		destination = distance;

		if (enabled) {
			nextColor = R.drawable.redroundcorners;
		} else {
			nextColor = R.drawable.greenroundcorners;
		}
		ObjectAnimator buttonMove = ObjectAnimator.ofFloat(button,
				"translationY", origin, destination);
		buttonMove.setDuration(duration);

		ObjectAnimator buttonMove2 = ObjectAnimator.ofFloat(button,
				"translationY", destination, origin);
		buttonMove2.setDuration(duration);

		AnimatorSet bounce = new AnimatorSet();

		bounce.playSequentially(buttonMove, buttonMove2);
		bounce.start();

		button.postDelayed(new Runnable() {
			@Override
			public void run() {
				button.setBackgroundResource(nextColor);
			}
		}, (2*duration));
	}

	public void diagramSentence(String bestSentence) {
		words = bestSentence.split( " " );

		updateSentenceViewHandler.post(updateSentenceView);
	}

	public void signifyWarning() {
		long halfStageDuration = 50;
		long fullStageDuration = 100;
		float leftOffset = -25f;
		float rightOffset = 25f;
		float origin = 0;

		final ImageButton button = (ImageButton) findViewById(R.id.listenButton);

		button.setBackgroundResource(R.drawable.yellowroundcorners);

		ObjectAnimator halfRightMove = ObjectAnimator.ofFloat(button,
				"translationX", origin, rightOffset);
		halfRightMove.setDuration(halfStageDuration);

		ObjectAnimator halfLeftMove = ObjectAnimator.ofFloat(button,
				"translationX", rightOffset, origin);
		halfLeftMove.setDuration(halfStageDuration);

		ObjectAnimator fullLeftMove = ObjectAnimator.ofFloat(button,
				"translationX", rightOffset, leftOffset);
		fullLeftMove.setDuration(fullStageDuration);

		ObjectAnimator fullRightMove = ObjectAnimator.ofFloat(button,
				"translationX", leftOffset, rightOffset);
		fullRightMove.setDuration(fullStageDuration);

		ObjectAnimator fullLeftMove2 = fullLeftMove.clone();
		fullLeftMove2.setDuration(fullStageDuration);

		ObjectAnimator halfRightMove2 = ObjectAnimator.ofFloat(button,
				"translationX", leftOffset, origin);
		halfRightMove2.setDuration(halfStageDuration);

		AnimatorSet wobble = new AnimatorSet();

		button.postDelayed(new Runnable() {
			@Override
			public void run() {
				button.setBackgroundResource(R.drawable.greenroundcorners);
			}
		}, (4*fullStageDuration));

		wobble.playSequentially(halfRightMove, fullLeftMove, fullRightMove, fullLeftMove2, halfRightMove2);

		wobble.start();

	}
}
