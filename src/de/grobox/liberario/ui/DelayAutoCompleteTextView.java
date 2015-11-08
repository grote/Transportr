package de.grobox.liberario.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

public class DelayAutoCompleteTextView extends AutoCompleteTextView {

	private static final int MESSAGE_TEXT_CHANGED = 100;
	private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

	private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
	private ProgressBar mLoadingIndicator;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
		}
	};

	public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// don't let the system try to save the state of this view, we do it ourselves
		setSaveEnabled(false);
	}

	public void setLoadingIndicator(ProgressBar progressBar) {
		mLoadingIndicator = progressBar;
	}

	public void setAutoCompleteDelay(int autoCompleteDelay) {
		mAutoCompleteDelay = autoCompleteDelay;
	}

	public void cancelFiltering() {
		mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
		if(mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.GONE);
		}
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		if(mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.VISIBLE);
		}
		mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
	}

	@Override
	public void onFilterComplete(int count) {
		if(mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.GONE);
		}
		super.onFilterComplete(count);
	}

	@Override
	protected void replaceText(CharSequence text) {
		// do nothing so that the text stays the same, because we will set it ourselves later
	}
}
