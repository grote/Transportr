package de.grobox.liberario.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class NoTextChangeAutoCompleteTextView extends AutoCompleteTextView {

	public NoTextChangeAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// don't let the system try to save the state of this view, we do it ourselves
		setSaveEnabled(false);
	}

	@Override
	protected void replaceText(CharSequence text) {
		// do nothing so that the text stays the same, because we will set it ourselves later
	}
}
