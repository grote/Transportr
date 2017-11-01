package de.grobox.transportr.ui;

import android.content.Context;
import android.os.Build;
import android.view.ContextThemeWrapper;

import de.cketti.library.changelog.ChangeLog;
import de.grobox.transportr.R;

public class TransportrChangeLog extends ChangeLog {

	public final static String TAG = TransportrChangeLog.class.getName();

	public TransportrChangeLog(Context context, boolean dark) {
		super(new ContextThemeWrapper(context, getDialogTheme(dark)), theme(dark));
	}

	private static int getDialogTheme(boolean dark) {
		if (dark) {
			return R.style.DialogTheme;
		} else {
			return R.style.DialogTheme_Light;
		}
	}

	private static String theme(boolean dark) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			if (dark) {
				// holo dark
				return "body { color: #e7e3e7; font-size: 0.9em; background-color: #292829; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			} else {
				// holo light
				return "body { color: #212421; font-size: 0.9em; background-color: #f7f7f7; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			}
		} else {
			if (dark) {
				// material dark
				return "body { color: #f3f3f3; font-size: 0.9em; background-color: #424242; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			} else {
				// material light
				return "body { color: #202020; font-size: 0.9em; background-color: transparent; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			}
		}
	}

}
