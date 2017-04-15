package com.kycq.library.picture.picker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

class AlbumTask extends AsyncTask<Void, Void, Void> {
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
	
	/** 内容解析器 */
	private ContentResolver contentResolver;
	/** 相册数据监听 */
	private OnAlbumListener onAlbumListener;
	/** 所有照片相册 */
	private AlbumInfo fullAlbumInfo;
	/** 缓存照片相册 */
	private AlbumInfo cacheAlbumInfo;
	/** 相册列表 */
	private ArrayList<AlbumInfo> albumInfoList;
	
	/**
	 * 构造方法
	 *
	 * @param fullAlbumName   所有照片相册名称
	 * @param cacheAlbumPath  缓存照片相册路径
	 * @param contentResolver 内容解析器
	 * @param onAlbumListener 相册数据监听
	 */
	AlbumTask(String fullAlbumName,
	          String cacheAlbumPath,
	          ContentResolver contentResolver,
	          OnAlbumListener onAlbumListener) {
		this.fullAlbumInfo = AlbumInfo.buildByName(fullAlbumName);
		this.cacheAlbumInfo = AlbumInfo.buildByPath(cacheAlbumPath);
		this.albumInfoList = new ArrayList<>();
		
		this.contentResolver = contentResolver;
		this.onAlbumListener = onAlbumListener;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		String selection =
				MediaStore.MediaColumns.MIME_TYPE + "=? or "
						+ MediaStore.MediaColumns.MIME_TYPE + "=?";
		String selectionArgs[] = new String[]{IMAGE_JPEG, IMAGE_PNG};
		
		// 查询数据库
		Cursor cursor = this.contentResolver.query(IMAGE_URI, IMAGE_PROJECTION, selection, selectionArgs, SORT);
		AlbumInfo fullAlbumInfo = this.fullAlbumInfo;
		ArrayList<AlbumInfo> albumInfoList = this.albumInfoList;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String picturePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
				File pictureFile = new File(picturePath);
				
				PictureInfo pictureInfo = new PictureInfo();
				pictureInfo.pictureUri = Uri.fromFile(pictureFile);
				pictureInfo.picturePath = picturePath;
				pictureInfo.obtainPictureSize();
				if (!pictureInfo.isAvailable()) {
					continue;
				}
				fullAlbumInfo.pictureInfoList.add(pictureInfo);
				addPictureInfo(albumInfoList, pictureInfo);
			}
			cursor.close();
		}
		this.fullAlbumInfo = fullAlbumInfo;
		this.albumInfoList = albumInfoList;
		
		AlbumInfo cacheAlbumInfo = this.cacheAlbumInfo;
		File cachePath = new File(cacheAlbumInfo.albumPath);
		File[] pickerFileArray = cachePath.listFiles();
		Arrays.sort(pickerFileArray, new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				return file1.lastModified() < file2.lastModified() ? -1 : 1;
			}
		});
		for (File pictureFile : pickerFileArray) {
			PictureInfo pictureInfo = new PictureInfo();
			pictureInfo.pictureUri = Uri.fromFile(pictureFile);
			pictureInfo.picturePath = pictureFile.getPath();
			pictureInfo.obtainPictureSize();
			if (!pictureInfo.isAvailable()) {
				continue;
			}
			// int index = fullAlbumInfo.pictureInfoList.indexOf(pictureInfo);
			// if (index != -1) {
			// 	fullAlbumInfo.pictureInfoList.remove(index);
			// }
			// fullAlbumInfo.pictureInfoList.add(0, pictureInfo);
			cacheAlbumInfo.pictureInfoList.add(0, pictureInfo);
		}
		this.cacheAlbumInfo = cacheAlbumInfo;
		
		return null;
	}
	
	/**
	 * 添加图片信息至相册中
	 *
	 * @param albumInfoList 相册信息列表
	 * @param pictureInfo   图片信息
	 */
	private void addPictureInfo(ArrayList<AlbumInfo> albumInfoList, PictureInfo pictureInfo) {
		String albumPath = new File(pictureInfo.picturePath).getParentFile().getAbsolutePath();
		for (AlbumInfo albumInfo : albumInfoList) {
			if (albumInfo.albumPath.equals(albumPath)) {
				albumInfo.pictureInfoList.add(0, pictureInfo);
				return;
			}
		}
		
		AlbumInfo albumInfo = AlbumInfo.buildByPath(albumPath);
		albumInfo.pictureInfoList.add(0, pictureInfo);
		albumInfoList.add(albumInfo);
	}
	
	@Override
	protected void onPostExecute(Void result) {
		this.onAlbumListener.onAlbum(
				this.fullAlbumInfo,
				this.cacheAlbumInfo,
				this.albumInfoList);
	}
	
	/**
	 * 相册读取监听
	 */
	interface OnAlbumListener {
		/**
		 * 相册读取结果
		 *
		 * @param fullAlbumInfo  所有照片相册
		 * @param cacheAlbumInfo 缓存照片相册
		 * @param albumInfoList  相册列表
		 */
		void onAlbum(AlbumInfo fullAlbumInfo,
		             AlbumInfo cacheAlbumInfo,
		             ArrayList<AlbumInfo> albumInfoList);
	}
}
