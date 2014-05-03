package com.vnkmpn.sentencediagrammer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Word {
	private String text = "";
	private ArrayList<String> types = new ArrayList<String>();

	private String key = "INVALID_KEY";
	private Context ctx = null;

	Word(Context context,String word, String dictionaryKey) {
		this.ctx = context;
		this.text = word;
		this.key = dictionaryKey;
		this.types = lookUpWord(word);
	}

	private ArrayList<String> lookUpWord(String word) {
		MWDictionary dict = (MWDictionary) new MWDictionary(key).execute(word);
		ArrayList<String> types = null;
		try {
			types = dict.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return types;
	}

	public String getText() {

		return text.concat(" ");
	}

	@SuppressLint("DefaultLocale")
	public String getType() {
		if (types.size() > 0 ) {
			return types.get(0).toLowerCase();
		}
		return null;
	}

	public String colorize() {
		CharSequence color = "";
		String startFontTag = "<font color='";
		String closeFont = "'>";
		String endFontTag = "</font>";
		String type = this.getType();
		if (type != null) {
			if (type.equals("noun")) {
				color = ctx.getResources().getText(R.string.noun);
			} else if (type.equals("verb")) {
				color = ctx.getResources().getText(R.string.verb);;
			} else if (type.equals("adjective")) {
				color = ctx.getResources().getText(R.string.adjective);;
			} else if (type.contains("article")) {
				color = ctx.getResources().getText(R.string.article);;
			} else if (type.equals("adverb")) {
				color = ctx.getResources().getText(R.string.adverb);;
			} else if (type.equals("pronoun")) {
				color = ctx.getResources().getText(R.string.pronoun);;
			} else if (type.equals("conjunction")) {
				color = ctx.getResources().getText(R.string.conjunction);;
			} else if (type.equals("preposition")) {
				color = ctx.getResources().getText(R.string.preposition);;
			} else {
				Log.d("Main",  this.getText() + " is a " + type);
			}
			return startFontTag + color + closeFont + this.getText() + endFontTag;
		} else {
			return "<b><u>" + this.getText() + "</u></b>";
		}
	}

}
