package com.kycq.library.picture.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

class PictureInfo implements Serializable, Parcelable {
	/** 日期格式 */
	private static SimpleDateFormat mDateFormat;

	static {
		mDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
	}

	/** 图片地址 */
	private Uri pictureUri;
	/** 图片路径 */
	private String picturePath;
	/** 图片宽度 */
	private int pictureWidth;
	/** 图片高度 */
	private int pictureHeight;

	/**
	 * 构造方法
	 */
	PictureInfo() {
	}

	private PictureInfo(Parcel in) {
		pictureUri = in.readParcelable(Uri.class.getClassLoader());
		picturePath = in.readString();
		pictureWidth = in.readInt();
		pictureHeight = in.readInt();
	}

	/**
	 * 创建相机照片信息
	 *
	 * @return 图片信息
	 */
	static PictureInfo createPictureInfo() {
		// 相机照片目录
		File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (!cameraDirectory.exists()) {
			cameraDirectory.mkdir();
		}

		PictureInfo pictureInfo = new PictureInfo();
		pictureInfo.picturePath = cameraDirectory + "/IMG_" + mDateFormat.format(System.currentTimeMillis()) + ".jpg";
		pictureInfo.pictureUri = Uri.fromFile(new File(pictureInfo.picturePath));
		return pictureInfo;
	}

	/**
	 * 获取图片地址
	 *
	 * @return 图片地址
	 */
	Uri getPictureUri() {
		return pictureUri;
	}

	/**
	 * 设置图片地址
	 *
	 * @param pictureUri 图片地址
	 */
	void setPictureUri(Uri pictureUri) {
		this.pictureUri = pictureUri;
	}

	/**
	 * 获取图片路径
	 *
	 * @return 图片路径
	 */
	String getPicturePath() {
		return picturePath;
	}

	/**
	 * 设置图片路径
	 *
	 * @param picturePath 图片路径
	 */
	void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}

	/**
	 * 转换成调用第三方应用时使用的地址
	 *
	 * @param context 设备上下文环境
	 * @return 图片地址
	 */
	Uri getContentUri(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return FileProvider.getUriForFile(context, context.getPackageName() + ".PictureProvider", new File(picturePath));
		}
		return pictureUri;
	}

	/**
	 * 获取图片大小信息
	 */
	void obtainPictureSize() {
		if (pictureWidth > 0 && pictureHeight > 0) {
			return;
		}

		if (!new File(picturePath).exists()) {
			return;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);
		pictureWidth = options.outWidth;
		pictureHeight = options.outHeight;
	}

	/**
	 * 获取图片宽度
	 *
	 * @return 图片宽度
	 */
	public int getPictureWidth() {
		return pictureWidth;
	}

	/**
	 * 获取图片高度
	 *
	 * @return 图片高度
	 */
	public int getPictureHeight() {
		return pictureHeight;
	}

	/**
	 * 图片是否可用
	 *
	 * @return true可用
	 */
	boolean isAvailable() {
		return pictureWidth > 0 && pictureHeight > 0;
	}

	/**
	 * 删除图片文件
	 *
	 * @return true删除成功
	 */
	boolean removePictureInfo() {
		File pictureFile = new File(pictureUri.getPath());
		return pictureFile.delete();
	}

	/**
	 * 删除图片数据库信息
	 *
	 * @param context 设备上下文环境
	 * @return true删除成功
	 */
	public int removePicture(Context context) {
		ContentResolver resolver = context.getContentResolver();
		return resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media.DATA + "=?",
				new String[]{picturePath});
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PictureInfo) {
			PictureInfo other = (PictureInfo) obj;
			return other.pictureUri.equals(pictureUri) && other.picturePath.equals(picturePath);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format(Locale.CHINA,
				"pictureUri = %s, picturePath = %s, pictureWidth = %d, pictureHeight = %d",
				pictureUri, picturePath, pictureWidth, pictureHeight);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(pictureUri, flags);
		dest.writeString(picturePath);
		dest.writeInt(pictureWidth);
		dest.writeInt(pictureHeight);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PictureInfo> CREATOR = new Creator<PictureInfo>() {
		@Override
		public PictureInfo createFromParcel(Parcel in) {
			return new PictureInfo(in);
		}

		@Override
		public PictureInfo[] newArray(int size) {
			return new PictureInfo[size];
		}
	};
}
