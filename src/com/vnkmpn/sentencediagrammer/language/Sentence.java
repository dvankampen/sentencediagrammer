package com.vnkmpn.sentencediagrammer.language;

import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;

public class Sentence {

	private LinkedList<Word> mWords;
	private Context mCtx;

	public Sentence(Context ctx) {
		this.mCtx = ctx;
		mWords = new LinkedList<Word>();
	}

	public Sentence(Context ctx, String text) {
		this.mCtx = ctx;
		mWords = new LinkedList<Word>();
		this.addWords(text);
	}

	public void addWords(String text) {
		String[] wordArray = text.split(" ");
		for (int i = 0; i < wordArray.length; i++) {
			Word word = new Word(mCtx, wordArray[i]);
			mWords.addLast(word);
		}
	}

	public String removeWordAsPlaintext() {
		return mWords.removeFirst().getPlaintext();
	}
	
	public void removeWord() {
		mWords.removeLast();
	}
	
	public void removeWord(int id) {
		mWords.remove(id);
	}

	public int getWordCount() {
		return mWords.size();
	}
	
	public Word getSubject() {
		return this.findFirstWordByType(SpeechType.NOUN);
	}
	
	public Word getObject() {
		return null;
	}
	
	public Word getAction() {
		return this.findFirstWordByType(SpeechType.VERB);
	}
	
	public SpeechType getWordType(int id) {
		return mWords.get(id-1).getType();
	}

	public int addWord(Word word) {
		this.mWords.addLast(word);
		return mWords.size();
	}
	
	private Word findFirstWordByType(SpeechType type) {
		if (mWords.isEmpty())
	    {
	        return null;
	    }

	    Word word;
	    ListIterator<Word> iterator = mWords.listIterator();
	    while(iterator.hasNext())
	    {
	    	word = iterator.next();
	    	
	        if(word.getType() == type)
	        {
	            return word;
	        }
	    }
	    return null;
	}
}
