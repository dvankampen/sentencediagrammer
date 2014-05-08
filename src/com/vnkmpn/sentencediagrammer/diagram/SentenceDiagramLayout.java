package com.vnkmpn.sentencediagrammer.diagram;

import com.vnkmpn.sentencediagrammer.language.Sentence;
import com.vnkmpn.sentencediagrammer.language.SpeechType;
import com.vnkmpn.sentencediagrammer.language.Word;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

public class SentenceDiagramLayout extends TableLayout {

	private TableRow mainLine;

	private TableRow modifiers;

	private Sentence mSentence;
	private Context mCtx;

	public SentenceDiagramLayout(Context context) {
		super(context);
		this.mCtx = context;
		this.mSentence = new Sentence(this.mCtx);

		this.mainLine = new TableRow(this.mCtx);
		this.mainLine.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		this.addView(mainLine);

		this.modifiers = new TableRow(this.mCtx);
		this.modifiers.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		this.addView(modifiers);
	}

	public void addWord(Word word) {
		mSentence.addWord(word);

		this.removeAllViews();
		DiagramWordTextView wtv = new DiagramWordTextView(this.mCtx, word);
		switch (word.getType()) {
		case ADJECTIVE:
		case ARTICLE:
			this.modifiers.addView(wtv);
			break;
		default:
			this.mainLine.addView(wtv);
			break;
		}
		this.redraw();
	}

	private void redraw() {
		this.removeView(mainLine);
		this.addView(mainLine);

		this.removeView(modifiers);
		this.addView(this.modifiers);
	}

	public void clear() {
		mSentence = new Sentence(this.mCtx);
		this.removeAllViews();
	}

	public int getWordCount() {
		return mSentence.getWordCount();
	}

}
