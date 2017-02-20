package com.kycq.library.picture.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;

import com.facebook.common.internal.Preconditions;

public class AnimatedZoomableController extends AbstractAnimatedZoomableController {

	private final ValueAnimator mValueAnimator;

	public AnimatedZoomableController(TransformGestureDetector transformGestureDetector) {
		super(transformGestureDetector);
		mValueAnimator = ValueAnimator.ofFloat(0, 1);
		mValueAnimator.setInterpolator(new DecelerateInterpolator());
	}

	public static AnimatedZoomableController newInstance() {
		return new AnimatedZoomableController(TransformGestureDetector.newInstance());
	}

	@Override
	public void setTransformAnimated(
			final Matrix newTransform,
			long durationMs,
			@Nullable final Runnable onAnimationComplete) {
		stopAnimation();
		Preconditions.checkArgument(durationMs > 0);
		Preconditions.checkState(!isAnimating());
		setAnimating(true);
		mValueAnimator.setDuration(durationMs);
		getTransform().getValues(getStartValues());
		newTransform.getValues(getStopValues());
		mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				calculateInterpolation(getWorkingTransform(), (float) valueAnimator.getAnimatedValue());
				AnimatedZoomableController.super.setTransform(getWorkingTransform());
			}
		});
		mValueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				onAnimationStopped();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				onAnimationStopped();
			}

			private void onAnimationStopped() {
				if (onAnimationComplete != null) {
					onAnimationComplete.run();
				}
				setAnimating(false);
				getDetector().restartGesture();
			}
		});
		mValueAnimator.start();
	}

	@Override
	public void stopAnimation() {
		if (!isAnimating()) {
			return;
		}
		mValueAnimator.cancel();
		mValueAnimator.removeAllUpdateListeners();
		mValueAnimator.removeAllListeners();
	}

}