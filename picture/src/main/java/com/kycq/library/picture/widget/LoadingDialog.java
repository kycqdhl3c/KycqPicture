package com.kycq.library.picture.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class LoadingDialog extends Dialog {
	private LoadingDrawable mLoadingDrawable;

	public LoadingDialog(Context context) {
		super(context);
		
		// noinspection ConstantConditions
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ImageView ivLoading = new ImageView(getContext());
		setContentView(ivLoading);

		mLoadingDrawable = new LoadingDrawable(getContext(), ivLoading);
		mLoadingDrawable.updateSizes(LoadingDrawable.LARGE);
		mLoadingDrawable.setBackgroundColor(0xFFFAFAFA);
		ivLoading.setImageDrawable(mLoadingDrawable);
	}

	@Override
	public void show() {
		super.show();

		mLoadingDrawable.setProgressRotation(0.8f);
		mLoadingDrawable.setStartEndTrim(0f, 0.5f);
		mLoadingDrawable.setAlpha(255);
		mLoadingDrawable.start();
	}

	@Override
	public void dismiss() {
		super.dismiss();

		mLoadingDrawable.stop();
	}
}
