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

public class ScaleTask extends AsyncTask<PictureInfo, Void, ArrayList<Uri>> {
	/** png图片属性 */
	private final static String IMAGE_PNG = "image/png";
	
	private KPPicker kpPicker;
	private OnScaleListener onScaleListener;
	
	public ScaleTask(KPPicker kpPicker, OnScaleListener onScaleListener) {
		this.kpPicker = kpPicker;
		this.onScaleListener = onScaleListener;
	}
	
	/**
	 * 执行缩放图片任务
	 *
	 * @param pictureInfoList 图片信息列表
	 */
	void executeScale(ArrayList<PictureInfo> pictureInfoList) {
		if (this.kpPicker.pickMaxWidth <= 0 && this.kpPicker.pickMaxHeight <= 0) {
			ArrayList<Uri> pictureUriList = new ArrayList<>();
			for (PictureInfo pictureInfo : pictureInfoList) {
				pictureUriList.add(pictureInfo.pictureUri);
			}
			this.onScaleListener.onScale(pictureUriList);
		} else {
			execute((PictureInfo[]) pictureInfoList.toArray());
		}
	}
	
	@Override
	protected ArrayList<Uri> doInBackground(PictureInfo... pictureInfoArray) {
		ArrayList<Uri> pictureUriList = new ArrayList<>();
		
		FileInputStream input = null;
		FileOutputStream output = null;
		for (PictureInfo pictureInfo : pictureInfoArray) {
			if (isCancelled()) {
				break;
			}
			
			PictureInfo outputPictureInfo = this.kpPicker.createPictureInfo();
			try {
				input = new FileInputStream(pictureInfo.picturePath);
				output = new FileOutputStream(outputPictureInfo.picturePath);
				
				BitmapFactory.Options options = calculateBitmapOptions(pictureInfo.picturePath);
				options.inJustDecodeBounds = false;
				Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
				if (IMAGE_PNG.equalsIgnoreCase(options.outMimeType)) {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
				} else {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
				}
				
				// pictureInfo.removePictureInfo();
				pictureUriList.add(outputPictureInfo.pictureUri);
			} catch (Exception error) {
				this.kpPicker.removePictureInfo(outputPictureInfo);
				pictureUriList.add(pictureInfo.pictureUri);
			} catch (OutOfMemoryError error) {
				System.gc();
				this.kpPicker.removePictureInfo(outputPictureInfo);
				pictureUriList.add(pictureInfo.pictureUri);
			} finally {
				closeSilently(input);
				closeSilently(output);
			}
		}
		return pictureUriList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Uri> pictureUriList) {
		this.onScaleListener.onScale(pictureUriList);
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
		while ((this.kpPicker.pickMaxWidth > 0 && options.outWidth / sampleSize > this.kpPicker.pickMaxWidth)
				|| (this.kpPicker.pickMaxHeight > 0 && options.outHeight / sampleSize > this.kpPicker.pickMaxHeight)) {
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
		 * 缩放列表数据
		 *
		 * @param pictureUriList 图片地址列表
		 */
		void onScale(ArrayList<Uri> pictureUriList);
	}
}
