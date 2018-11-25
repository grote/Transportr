/*
 * Copyright 2015 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.grobox.transportr.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import de.grobox.transportr.R;

public class LceAnimator {

	private final static int DURATION = 750;

	private LceAnimator() {
	}

	/**
	 * Show the loading view. No animations, because sometimes loading things is pretty fast (i.e.
	 * retrieve data from memory cache).
	 */
	public static void showLoading(@NonNull View loadingView, @NonNull View contentView,
	                               @Nullable View errorView) {

		contentView.setVisibility(View.GONE);
		if (errorView != null) errorView.setVisibility(View.GONE);
		loadingView.setVisibility(View.VISIBLE);
	}

	/**
	 * Shows the error view instead of the loading view
	 */
	public static void showErrorView(@NonNull final View loadingView, @NonNull final View contentView, @NonNull final View errorView) {
		contentView.setVisibility(View.GONE);

		// Not visible yet, so animate the view in
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator in = ObjectAnimator.ofFloat(errorView, View.ALPHA, 1f);
		ObjectAnimator loadingOut = ObjectAnimator.ofFloat(loadingView, View.ALPHA, 0f);

		set.playTogether(in, loadingOut);
		set.setDuration(DURATION);

		set.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				errorView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				loadingView.setVisibility(View.GONE);
				loadingView.setAlpha(1f); // For future showLoading calls
			}
		});

		set.start();
	}

	/**
	 * Display the content instead of the loadingView
	 */
	public static void showContent(@NonNull final View loadingView, @NonNull final View contentView,
	                               @Nullable final View errorView) {

		if (contentView.getVisibility() == View.VISIBLE) {
			// No Changing needed, because contentView is already visible
			if (errorView != null) errorView.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
		} else {
			if (errorView != null) errorView.setVisibility(View.GONE);

			final Resources resources = loadingView.getResources();
			final int translateInPixels = resources.getDimensionPixelSize(R.dimen.lce_content_view_animation_translate_y);
			// Not visible yet, so animate the view in
			AnimatorSet set = new AnimatorSet();
			ObjectAnimator contentFadeIn = ObjectAnimator.ofFloat(contentView, View.ALPHA, 0f, 1f);
			ObjectAnimator contentTranslateIn = ObjectAnimator.ofFloat(contentView, View.TRANSLATION_Y,
					translateInPixels, 0);

			ObjectAnimator loadingFadeOut = ObjectAnimator.ofFloat(loadingView, View.ALPHA, 1f, 0f);
			ObjectAnimator loadingTranslateOut = ObjectAnimator.ofFloat(loadingView, View.TRANSLATION_Y, 0,
					-translateInPixels);

			set.playTogether(contentFadeIn, contentTranslateIn, loadingFadeOut, loadingTranslateOut);
			set.setDuration(DURATION);

			set.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					contentView.setTranslationY(0);
					loadingView.setTranslationY(0);
					contentView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					loadingView.setVisibility(View.GONE);
					loadingView.setAlpha(1f); // For future showLoading calls
					contentView.setTranslationY(0);
					loadingView.setTranslationY(0);
				}
			});

			set.start();
		}
	}
}
