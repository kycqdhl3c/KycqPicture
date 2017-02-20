package com.kycq.library.picture.picker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class ScaleTask extends AsyncTask<PictureInfo, Void, ArrayList<Uri>> {
	/** 输出图片最大宽度 */
	private int mMaxWidth;
	/** 输出图片最大高度 */
	private int mMaxHeight;

	/** 图片缩放监听 */
	private OnScaleListener mOnScaleListener;

	/**
	 * 构造方法
	 *
	 * @param maxWidth  输出图片最大宽度
	 * @param maxHeight 输出图片最大高度
	 * @param listener  图片缩放监听
	 */
	ScaleTask(int maxWidth, int maxHeight, OnScaleListener listener) {
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
		mOnScaleListener = listener;
	}

	/**
	 * 执行缩放图片任务
	 *
	 * @param pictureInfoArray 图片信息数组
	 */
	void executeScale(PictureInfo... pictureInfoArray) {
		if (mMaxWidth <= 0 && mMaxHeight <= 0) {
			ArrayList<Uri> pictureUriList = new ArrayList<>();
			for (PictureInfo pictureInfo : pictureInfoArray) {
				pictureUriList.add(pictureInfo.getPictureUri());
			}
			mOnScaleListener.scaleResult(pictureUriList);
		} else {
			execute(pictureInfoArray);
		}
	}

	@Override
	protected void onPreExecute() {
		mOnScaleListener.scaleStart();
	}

	@Override
	protected ArrayList<Uri> doInBackground(PictureInfo... pictureInfoArray) {
		ArrayList<Uri> pictureUriList = new ArrayList<>();

		FileInputStream input = null;
		FileOutputStream output = null;
		for (PictureInfo pictureInfo : pictureInfoArray) {
			PictureInfo outputPictureInfo = PictureInfo.createPictureInfo();
			try {
				input = new FileInputStream(pictureInfo.getPicturePath());
				output = new FileOutputStream(outputPictureInfo.getPicturePath());

				BitmapFactory.Options options = calculateBitmapOptions(pictureInfo.getPicturePath());
				options.inJustDecodeBounds = false;
				Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
				if (AlbumTask.IMAGE_PNG.equalsIgnoreCase(options.outMimeType)) {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
				} else {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
				}

				// pictureInfo.removePictureInfo();
				pictureUriList.add(outputPictureInfo.getPictureUri());
			} catch (Exception error) {
				outputPictureInfo.removePictureInfo();
				pictureUriList.add(pictureInfo.getPictureUri());
			} catch (OutOfMemoryError error) {
				System.gc();
				outputPictureInfo.removePictureInfo();
				pictureUriList.add(pictureInfo.getPictureUri());
			} finally {
				closeSilently(input);
				closeSilently(output);
			}
		}

		return pictureUriList;
	}

	@Override
	protected void onPostExecute(ArrayList<Uri> pictureList) {
		if (isCancelled()) {
			return;
		}
		mOnScaleListener.scaleResult(pictureList);
	}

	/**
	 * 计算图片信息
	 *
	 * @param picturePath 图片路径
	 * @return 图片信息
	 * @throws IOException 图片读取错误信息
	 */
	private BitmapFactory.Options calculateBitmapOptions(String picturePath) throws IOException {
		InputStream input = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			input = new FileInputStream(picturePath);
			BitmapFactory.decodeStream(input, null, options);
		} finally {
			closeSilently(input);
		}

		int sampleSize = 1;
		while ((mMaxWidth > 0 && options.outWidth / sampleSize > mMaxWidth)
				|| (mMaxHeight > 0 && options.outHeight / sampleSize > mMaxHeight)) {
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
	 * 图片缩放监听
	 */
	interface OnScaleListener {

		/**
		 * 开始缩放
		 */
		void scaleStart();

		/**
		 * 缩放结果
		 *
		 * @param pictureUriList 图片地址列表
		 */
		void scaleResult(ArrayList<Uri> pictureUriList);
	}
}
