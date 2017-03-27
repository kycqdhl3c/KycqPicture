package com.kycq.library.picture.picker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

class AlbumTask extends AsyncTask<Void, Void, ArrayList<AlbumInfo>> {
	/** jpg图片属性 */
	private final static String IMAGE_JPEG = "image/jpeg";
	/** png图片属性 */
	private final static String IMAGE_PNG = "image/png";
	/** 数据排序 */
	private final static String SORT = MediaStore.Images.Media.DATE_ADDED + " ASC";
	/** 图片数据库地址 */
	private final static Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	/** 图片数据库字段数组 */
	private static final String[] IMAGE_PROJECTION = {
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.DATE_ADDED
	};
	
	/** 选择参数信息 */
	private KPPicker kpPicker;
	/** 内容解析器 */
	private ContentResolver contentResolver;
	/** 相册数据监听 */
	private OnAlbumListener onAlbumListener;
	
	/**
	 * 构造方法
	 *
	 * @param kpPicker        选择参数信息
	 * @param contentResolver 内容解析器
	 * @param onAlbumListener 相册数据监听
	 */
	AlbumTask(KPPicker kpPicker,
	          ContentResolver contentResolver,
	          OnAlbumListener onAlbumListener) {
		this.kpPicker = kpPicker;
		this.contentResolver = contentResolver;
		this.onAlbumListener = onAlbumListener;
	}
	
	@Override
	protected ArrayList<AlbumInfo> doInBackground(Void... params) {
		String selection = MediaStore.MediaColumns.MIME_TYPE
				+ "=? or "
				+ MediaStore.MediaColumns.MIME_TYPE + "=?";
		String selectionArgs[] = new String[]{IMAGE_JPEG, IMAGE_PNG};
		
		// 查询数据库
		Cursor cursor = this.contentResolver.query(IMAGE_URI, IMAGE_PROJECTION, selection, selectionArgs, SORT);
		ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
		System.out.println(this.kpPicker.allPictureAlbumName);
		albumInfoList.add(AlbumInfo.buildByName(this.kpPicker.allPictureAlbumName));
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String picturePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
				File pictureFile = new File(picturePath);
				
				PictureInfo pictureInfo = new PictureInfo();
				pictureInfo.pictureUri = Uri.fromFile(pictureFile);
				pictureInfo.picturePath = picturePath;
				KPPicker.addPictureInfo(albumInfoList, pictureInfo);
			}
			cursor.close();
		}
		return albumInfoList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<AlbumInfo> albumInfoList) {
		this.onAlbumListener.onAlbum(albumInfoList);
	}
	
	/**
	 * 相册信息列表数据监听
	 */
	interface OnAlbumListener {
		/**
		 * 相册信息列表数据
		 *
		 * @param albumInfoList 相册信息列表
		 */
		void onAlbum(ArrayList<AlbumInfo> albumInfoList);
	}
}
