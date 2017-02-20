package com.kycq.library.picture.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

class AlbumTask extends AsyncTask<Void, Void, ArrayList<PictureInfo>> {
	/** jpg图片属性 */
	private final static String IMAGE_JPEG = "image/jpeg";
	/** png图片属性 */
	final static String IMAGE_PNG = "image/png";
	/** 数据排序 */
	private final static String SORT = MediaStore.Images.Media.DATE_ADDED + " DESC";
	/** 图片数据库地址 */
	private final static Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	/** 图片数据库字段数组 */
	private static final String[] IMAGE_PROJECTION = {
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.DATE_ADDED,
	};

	/** 设备上下文环境 */
	private Context mContext;
	/** 相册数据监听 */
	private OnAlbumListener mOnAlbumListener;

	/**
	 * 构造方法
	 *
	 * @param context  设备上下文环境
	 * @param listener 相册数据监听
	 */
	AlbumTask(Context context, OnAlbumListener listener) {
		mContext = context;
		mOnAlbumListener = listener;
	}

	@Override
	protected ArrayList<PictureInfo> doInBackground(Void... voids) {
		ContentResolver resolver = mContext.getContentResolver();

		String selection = MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=?";
		String selectionArgs[] = new String[]{IMAGE_JPEG, IMAGE_PNG};

		// 查询数据库
		Cursor cursor = resolver.query(IMAGE_URI, IMAGE_PROJECTION, selection, selectionArgs, SORT);
		ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				// int pictureId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
				// String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_ID));
				// String name = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
				String picturePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));

				PictureInfo pictureInfo = new PictureInfo();
				pictureInfo.setPictureUri(Uri.fromFile(new File(picturePath)));
				pictureInfo.setPicturePath(picturePath);
				pictureInfo.obtainPictureSize();

				// 仅展示可用图片
				if (pictureInfo.isAvailable()) {
					pictureInfoList.add(pictureInfo);
				}
			}
			cursor.close();
		}
		return pictureInfoList;
	}

	@Override
	protected void onPostExecute(ArrayList<PictureInfo> pictureInfoList) {
		if (isCancelled()) {
			return;
		}
		mOnAlbumListener.onAlbum(pictureInfoList);
	}

	/**
	 * 相册数据监听
	 */
	interface OnAlbumListener {
		/**
		 * 相册数据
		 *
		 * @param pictureInfoList 图片信息列表
		 */
		void onAlbum(ArrayList<PictureInfo> pictureInfoList);
	}
}
