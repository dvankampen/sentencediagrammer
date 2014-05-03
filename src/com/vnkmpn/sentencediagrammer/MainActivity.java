package com.vnkmpn.sentencediagrammer;

import java.util.ArrayList;


import com.vnkmpn.sentencediagrammer.language.Sentence;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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

	private boolean liveDiagramming = true;

	private TextView sentenceView;

	private SpeechRecognizer sr;

	private Sentence sentence;

	private ImageButton button;
	
	SharedPreferences sharedPrefs;

	@SuppressLint("HandlerLeak")
	private final Handler updateSentenceViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			appendHypertextToSentence(msg.obj.toString());
			updateSentenceViewHandler.postDelayed(updateSentenceView,125);
		}
	};

	private final Runnable updateSentenceView = new Runnable() {

		@SuppressLint("DefaultLocale")
		@Override
		public void run() {

			if (sentence.getRemainingWordCount() == 0 ) {
				updateSentenceViewHandler.removeCallbacks(this);
				return;
			}

			Message msg = updateSentenceViewHandler.obtainMessage();
			msg.obj = sentence.removeWordAsHtml();

			updateSentenceViewHandler.sendMessage(msg);
		}
	};

	private Intent createRecognizerIntent() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

		return intent;
	}

	protected void appendHypertextToSentence(String word) {
		sentenceView.append(Html.fromHtml(word));
		sentenceView.invalidate();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button = (ImageButton) findViewById(R.id.listenButton);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf"); 

		sentenceView = (TextView)findViewById(R.id.sentenceText);
		sentenceView.setTypeface(type);

		sentenceView.setTypeface(type);

		findViewById(R.id.listenButton).setOnClickListener(this);

		sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
		
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
			Intent intent = new Intent(MainActivity.this,
					SettingsActivity.class);
			startActivity(intent);
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

		startListening(sr);
	}

	private void startListening(final SpeechRecognizer sr) {
		
		liveDiagramming = sharedPrefs.getBoolean(SettingsFragment.KEY_PREF_LIVE_DIAG, false);
		
		Log.i("Main", "live diagramming is " + liveDiagramming);

		/* clear the text view */
		sentenceView.setText("");

		sentence = new Sentence(this);

		Intent intent = createRecognizerIntent();

		final Runnable stopListening = new Runnable() {
			@Override
			public void run() {
				sr.stopListening();
			}
		};
		final Handler handler = new Handler();

		sr.setRecognitionListener( new RecognitionListener() {

			private String bestSentence = "";

			private int wordCount = 0;

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
				handler.removeCallbacks(stopListening);
			}

			@Override
			public void onError(int error) {
				Log.d("Speech", "onError");

				signifyWarning();
				handler.removeCallbacks(stopListening);

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
				if (liveDiagramming) {

					ArrayList<String> results = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					String partials = results.get(0);
					if (!partials.equals("")) {
						Log.i("Main", "partial results: " + partials + ".");
						String[] words = partials.split(" ");
						if (words.length > wordCount) {
							if (words[wordCount] != null) {
								Log.i("Main",  "adding word " + words[wordCount]);
								sentence.addWord(words[wordCount]);
								appendHypertextToSentence(sentence.removeWordAsHtml());
								wordCount++;
							}
						}
					}
				}
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

				if (!liveDiagramming) {

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

					diagram(bestSentence);
				}
			}

			@Override
			public void onRmsChanged(float rmsdB) {
			}
		});
		sr.startListening(intent);
	}

	@SuppressLint("NewApi")
	public void startAnimation() {
		long firstStageDuration = 1500;
		long secondStageDuration = 500;
		float firstStageDistance = -500f;
		float secondStageDistance = 100f;
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

		//diagram("Egads the evil teacher assigns us work daily and expects it on his desk by eight the next morning");
	}

	private void recording(boolean enabled) 
	{
		long duration = 100;
		float distance = -50f;
		float origin;
		float destination;
		final int nextColor;

		button.setEnabled(false);

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
				button.setEnabled(true);
			}
		}, (2*duration));
	}

	public void diagram(String bestSentence) {
		sentence.addWords(bestSentence);

		// start the UI update handler
		updateSentenceViewHandler.post(updateSentenceView);
	}

	public void signifyWarning() {
		long halfStageDuration = 50;
		long fullStageDuration = 100;
		float leftOffset = -25f;
		float rightOffset = 25f;
		float origin = 0;

		button.setEnabled(false);

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
				button.setEnabled(true);
			}
		}, (4*fullStageDuration));

		wobble.playSequentially(halfRightMove, fullLeftMove, fullRightMove, fullLeftMove2, halfRightMove2);

		wobble.start();

	}

	
}
