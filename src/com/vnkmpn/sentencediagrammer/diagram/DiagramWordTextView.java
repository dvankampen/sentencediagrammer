package com.vnkmpn.sentencediagrammer.diagram;

import com.vnkmpn.sentencediagrammer.language.Word;

import android.R.color;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DiagramWordTextView extends TextView {

	public DiagramWordTextView(Context context) {
		super(context);
		this.setText("sample");
		Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/RobotoCondensed-Regular.ttf");
		this.setTypeface(type);
	}

	public DiagramWordTextView(Context context, String word) {
		super(context);
		this.setText(Html.fromHtml("<u>" + word + "</u>"));
		this.setPadding(0, 0, 5, 0);
		Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/RobotoCondensed-Regular.ttf");
		this.setTypeface(type);
	}

	public DiagramWordTextView(Context context, Word word) {
		super(context);
		this.setText(Html.fromHtml("<u>" + word.getPlaintext() + "</u>"));
		this.setPadding(0, 0, 5, 0);
		Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/RobotoCondensed-Regular.ttf");
		this.setTextSize(18);
		this.setTypeface(type);
		switch (word.getType()) {
		case ABBREVIATION:
			break;
		case ADJECTIVE:
		case ARTICLE:
			this.setTextColor(context.getResources().getColor(color.holo_orange_light));
			this.setRotation(45);
			break;
		case ADVERB:
			break;
		case CONJUNCTION:
			break;
		case CONTRACTION:
			break;
		case NOUN:
			this.setTextColor(context.getResources().getColor(color.holo_green_light));
			break;
		case PREPOSITION:
			this.setTextColor(context.getResources().getColor(color.darker_gray));
			break;
		case PRONOUN:
			break;
		case VERB:
			this.setTextColor(context.getResources().getColor(color.holo_red_light));
			break;
		case INVALID:
		default:
			break;
		}
	    //this.setPadding(20, 20, 20, 20);
		this.setGravity(Gravity.CENTER);
		TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1.0f);

		paramsExample.setMargins(20, 20, 20, 20);
		this.setLayoutParams(paramsExample);
	}

}
