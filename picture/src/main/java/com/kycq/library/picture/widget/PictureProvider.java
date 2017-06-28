package com.kycq.library.picture.widget;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class PictureProvider extends FileProvider {
	
	public static Uri getUriForFile(Context context, File file) {
		return getUriForFile(context, context.getPackageName() + ".PictureProvider", file);
	}
}
