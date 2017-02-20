package com.kycq.picture;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

public class BasicApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Fresco.initialize(this,
				ImagePipelineConfig.newBuilder(this)
						.setDownsampleEnabled(true)
						.build());
	}
}
