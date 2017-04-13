package com.kycq.library.picture.picker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class CompressTask extends AsyncTask<PictureInfo, Void, ArrayList<Uri>> {
	/** jpg图片属性 */
	private final static String IMAGE_JPEG = "image/jpeg";
	/** png图片属性 */
	private final static String IMAGE_PNG = "image/png";
	
	private String compressPath;
	private int pickMaxWidth;
	private int pickMaxHeight;
	private int compressQuality;
	
	private OnCompressListener onCompressListener;
	
	CompressTask(Context context,
	             int pickMaxWidth, int pickMaxHeight,
	             int compressQuality,
	             OnCompressListener onCompressListener) {
		compressPath = initCompressPath(context);
		this.pickMaxWidth = pickMaxWidth;
		this.pickMaxHeight = pickMaxHeight;
		this.compressQuality = compressQuality;
		this.onCompressListener = onCompressListener;
	}
	
	private String initCompressPath(Context context) {
		File compressPath = context.getExternalCacheDir();
		if (compressPath == null || !compressPath.canWrite()) {
			compressPath = context.getCacheDir();
			if (compressPath == null || !compressPath.canWrite()) {
				return null;
			}
		}
		compressPath = new File(compressPath, "compress");
		if (!compressPath.exists() && !compressPath.mkdirs()) {
			return null;
		}
		return compressPath.getPath();
	}
	
	/**
	 * 执行压缩任务
	 */
	void executeCompress(ArrayList<PictureInfo> pictureInfoList) {
		if (this.compressPath != null
				&& (this.pickMaxWidth > 0 || this.pickMaxHeight > 0 || this.compressQuality != 100)) {
			PictureInfo[] pictureInfoArray = new PictureInfo[pictureInfoList.size()];
			execute(pictureInfoList.toArray(pictureInfoArray));
		} else {
			ArrayList<Uri> pictureUriList = new ArrayList<>();
			for (PictureInfo pictureInfo : pictureInfoList) {
				pictureUriList.add(pictureInfo.pictureUri);
			}
			this.onCompressListener.onCompress(pictureUriList);
		}
	}
	
	@Override
	protected ArrayList<Uri> doInBackground(PictureInfo... pictureInfoArray) {
		ArrayList<Uri> pictureUriList = new ArrayList<>();
		
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		for (PictureInfo pictureInfo : pictureInfoArray) {
			if (isCancelled()) {
				break;
			}
			
			boolean success;
			PictureInfo outputPictureInfo = createPictureInfo();
			try {
				inputStream = new FileInputStream(pictureInfo.picturePath);
				outputStream = new FileOutputStream(outputPictureInfo.picturePath);
				
				BitmapFactory.Options options = calculateBitmapOptions(
						pictureInfo.picturePath,
						this.pickMaxWidth, this.pickMaxHeight
				);
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
				if (IMAGE_PNG.equalsIgnoreCase(options.outMimeType)) {
					success = bitmap.compress(Bitmap.CompressFormat.PNG, this.compressQuality, outputStream);
				} else {
					success = bitmap.compress(Bitmap.CompressFormat.JPEG, this.compressQuality, outputStream);
				}
				
				if (success) {
					pictureUriList.add(outputPictureInfo.pictureUri);
				} else {
					outputPictureInfo.delete();
					pictureUriList.add(pictureInfo.pictureUri);
				}
			} catch (Exception error) {
				outputPictureInfo.delete();
				pictureUriList.add(pictureInfo.pictureUri);
			} catch (OutOfMemoryError error) {
				System.gc();
				outputPictureInfo.delete();
				pictureUriList.add(pictureInfo.pictureUri);
			} finally {
				closeSilently(inputStream);
				closeSilently(outputStream);
			}
		}
		return pictureUriList;
	}
	
	/**
	 * 创建压缩照片信息
	 *
	 * @return 照片信息
	 */
	private PictureInfo createPictureInfo() {
		PictureInfo pictureInfo = new PictureInfo();
		pictureInfo.picturePath = this.compressPath + "/IMG_" + System.currentTimeMillis() + ".jpg";
		pictureInfo.pictureUri = Uri.fromFile(new File(pictureInfo.picturePath));
		return pictureInfo;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Uri> pictureUriList) {
		this.onCompressListener.onCompress(pictureUriList);
	}
	
	/**
	 * 计算图片信息
	 *
	 * @param picturePath   图片路径
	 * @param pickMaxWidth  图片最大宽度
	 * @param pickMaxHeight 图片最大高度
	 * @return 图片信息
	 * @throws IOException 图片读取错误信息
	 */
	private BitmapFactory.Options calculateBitmapOptions(String picturePath,
	                                                     int pickMaxWidth,
	                                                     int pickMaxHeight) throws IOException {
		InputStream input = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			input = new FileInputStream(picturePath);
			BitmapFactory.decodeStream(input, null, options);
		} finally {
			closeSilently(input);
		}
		options.inJustDecodeBounds = false;
		
		if ((pickMaxWidth > 0 && pickMaxWidth >= options.outWidth)
				|| (pickMaxHeight > 0 && pickMaxHeight >= options.outHeight)) {
			return options;
		}
		
		int sampleSize = 1;
		while ((pickMaxWidth > 0 && options.outWidth / sampleSize > pickMaxWidth)
				|| (pickMaxHeight > 0 && options.outHeight / sampleSize > pickMaxHeight)) {
			sampleSize = sampleSize << 1;
		}
		options.inSampleSize = sampleSize;
		
		return options;
	}
	
	private void closeSilently(Closeable close) {
		try {
			if (close != null) {
				close.close();
			}
		} catch (IOException ignored) {
		}
	}
	
	/**
	 * 图片压缩监听
	 */
	interface OnCompressListener {
		
		/**
		 * 图片压缩结果
		 *
		 * @param pictureUriList 图片地址列表
		 */
		void onCompress(ArrayList<Uri> pictureUriList);
	}
}
