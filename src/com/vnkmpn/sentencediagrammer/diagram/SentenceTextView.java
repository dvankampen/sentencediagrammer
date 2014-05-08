package com.vnkmpn.sentencediagrammer.diagram;

import com.vnkmpn.sentencediagrammer.R;
import com.vnkmpn.sentencediagrammer.language.Sentence;
import com.vnkmpn.sentencediagrammer.language.SpeechType;
import com.vnkmpn.sentencediagrammer.language.Word;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class SentenceTextView extends TextView {
	
	private Sentence mSentence;
	
	private Context mCtx;

	public SentenceTextView(Context context) {
		super(context);
		this.mCtx = context;
		Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/RobotoCondensed-Regular.ttf");
		this.setTypeface(type);
		this.setTextSize(24);
		this.setAlpha(0f);
		mSentence = new Sentence(this.mCtx);
	}

	public void clear() {
		mSentence = new Sentence(this.mCtx);
		this.setText("");
	}
	
	public void addWord(Word word) {
		mSentence.addWord(word);
		String wordHtml = getHtml(word);
		this.append(Html.fromHtml(wordHtml));
		
		this.invalidate();
	}

	public int getWordCount() {
		return mSentence.getWordCount();
	}
	
	public String getHtml(Word word) {
		CharSequence color = "";
		String startFontTag = "<font color='";
		String closeFont = "'>";
		String endFontTag = "</font>";
		SpeechType type = word.getType();
		if (type != SpeechType.INVALID) {
			switch (type) {
			case NOUN:
				color = mCtx.getResources().getText(R.string.noun);
				break;
			case VERB:
				color = mCtx.getResources().getText(R.string.verb);
				break;
			case ADJECTIVE:
			case ARTICLE:
				color = mCtx.getResources().getText(R.string.adjective);
				break;
			case ADVERB:
				color = mCtx.getResources().getText(R.string.adverb);
				break;
			case PRONOUN:
				color = mCtx.getResources().getText(R.string.pronoun);
				break;
			case CONJUNCTION:
				color = mCtx.getResources().getText(R.string.conjunction);
				break;
			case PREPOSITION:
				color = mCtx.getResources().getText(R.string.preposition);
				break;
			case ABBREVIATION:
				color = mCtx.getResources().getText(R.string.abbreviation);
				break;
			default:
				Log.d("Main",  word.getPlaintext() + " is a " + type.toString());
				break;
			}
			return startFontTag + color + closeFont + word.getPlaintext() + endFontTag;
		} else {
			return "<b><u>" + word.getPlaintext() + "</u></b>";
		}
	}

}
