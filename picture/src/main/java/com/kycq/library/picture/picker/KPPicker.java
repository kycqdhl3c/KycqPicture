package com.kycq.library.picture.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.kycq.library.picture.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class KPPicker implements Parcelable {
	/** 选择参数 */
	static final String PICKER = "picker";
	/** 输出图片列表 */
	static final String PICKER_LIST = "pickerList";
	
	/** 日期格式 */
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
	
	String allPictureAlbumName;
	int pickCount;
	int pickAspectX;
	int pickAspectY;
	int pickMaxWidth;
	int pickMaxHeight;
	
	private KPPicker() {
	}
	
	private KPPicker(Parcel in) {
		allPictureAlbumName = in.readString();
		pickCount = in.readInt();
		pickAspectX = in.readInt();
		pickAspectY = in.readInt();
		pickMaxWidth = in.readInt();
		pickMaxHeight = in.readInt();
	}
	
	/**
	 * 创建相机照片信息
	 *
	 * @return 图片信息
	 */
	PictureInfo createPictureInfo() {
		// 相机照片目录
		File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (!cameraDirectory.exists()) {
			// noinspection ResultOfMethodCallIgnored
			cameraDirectory.mkdir();
		}
		
		PictureInfo pictureInfo = new PictureInfo();
		pictureInfo.picturePath = cameraDirectory + "/IMG_" + DATE_FORMAT.format(System.currentTimeMillis()) + ".jpg";
		pictureInfo.pictureUri = Uri.fromFile(new File(pictureInfo.picturePath));
		return pictureInfo;
	}
	
	boolean removePictureInfo(PictureInfo pictureInfo) {
		File pictureFile = new File(pictureInfo.pictureUri.getPath());
		return pictureFile.delete();
	}
	
	/**
	 * 转换成调用第三方应用时使用的地址
	 *
	 * @param context     设备上下文环境
	 * @param pictureInfo 图片信息
	 * @return 图片地址
	 */
	Uri getPictureContentUri(Context context, PictureInfo pictureInfo) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return FileProvider.getUriForFile(
					context,
					context.getPackageName() + ".PictureProvider",
					new File(pictureInfo.picturePath)
			);
		}
		return pictureInfo.pictureUri;
	}
	
	/**
	 * 添加图片信息至相册中
	 *
	 * @param albumInfoList 相册信息列表
	 * @param pictureInfo   图片信息
	 * @return true添加成功
	 */
	static boolean addPictureInfo(ArrayList<AlbumInfo> albumInfoList, PictureInfo pictureInfo) {
		if (albumInfoList == null) {
			return false;
		}
		
		obtainPictureSize(pictureInfo);
		if (!pictureInfo.isAvailable()) {
			return false;
		}
		
		String albumPath = new File(pictureInfo.picturePath).getParentFile().getAbsolutePath();
		for (AlbumInfo albumInfo : albumInfoList) {
			if (albumInfo.isFullAlbum()) {
				albumInfo.pictureInfoList.add(0, pictureInfo);
			} else if (albumInfo.albumPath.equals(albumPath)) {
				albumInfo.pictureInfoList.add(0, pictureInfo);
				return true;
			}
		}
		
		AlbumInfo albumInfo = AlbumInfo.buildByPath(albumPath);
		albumInfo.pictureInfoList.add(0, pictureInfo);
		albumInfoList.add(albumInfo);
		
		return true;
	}
	
	/**
	 * 获取图片大小信息
	 */
	static void obtainPictureSize(PictureInfo pictureInfo) {
		if (pictureInfo.pictureWidth > 0 && pictureInfo.pictureHeight > 0) {
			return;
		}
		
		if (!new File(pictureInfo.picturePath).exists()) {
			return;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pictureInfo.picturePath, options);
		pictureInfo.pictureWidth = options.outWidth;
		pictureInfo.pictureHeight = options.outHeight;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(allPictureAlbumName);
		dest.writeInt(pickCount);
		dest.writeInt(pickAspectX);
		dest.writeInt(pickAspectY);
		dest.writeInt(pickMaxWidth);
		dest.writeInt(pickMaxHeight);
	}
	
	public static final Creator<KPPicker> CREATOR = new Creator<KPPicker>() {
		@Override
		public KPPicker createFromParcel(Parcel in) {
			return new KPPicker(in);
		}
		
		@Override
		public KPPicker[] newArray(int size) {
			return new KPPicker[size];
		}
	};
	
	public static Uri pickPictureUri(Intent data) {
		return pickPictureUri(data, 0);
	}
	
	public static Uri pickPictureUri(Intent data, int position) {
		ArrayList<Uri> pictureUriList = pickPictureUriList(data);
		if (pictureUriList == null || pictureUriList.size() < position) {
			return null;
		}
		return pictureUriList.get(position);
	}
	
	public static ArrayList<Uri> pickPictureUriList(Intent data) {
		return data.getParcelableArrayListExtra(PICKER_LIST);
	}
	
	public static class Builder {
		int pickCount;
		int pickAspectX;
		int pickAspectY;
		int pickMaxWidth;
		int pickMaxHeight;
		
		public Builder pickCount(int pickCount) {
			this.pickCount = pickCount;
			return this;
		}
		
		public Builder pickAspect(int pickAspectX, int pickAspectY) {
			this.pickAspectX = pickAspectX;
			this.pickAspectY = pickAspectY;
			return this;
		}
		
		public Builder pickMaxSize(int pickMaxWidth, int pickMaxHeight) {
			this.pickMaxWidth = pickMaxWidth;
			this.pickMaxHeight = pickMaxHeight;
			return this;
		}
		
		public void pick(Activity activity, int requestCode) {
			Intent intent = new Intent(activity, KPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(activity));
			activity.startActivityForResult(intent, requestCode);
		}
		
		public void pick(Fragment fragment, int requestCode) {
			Intent intent = new Intent(fragment.getContext(), KPPicturePickerActivity.class);
			intent.putExtra(PICKER, buildPicker(fragment.getContext()));
			fragment.startActivityForResult(intent, requestCode);
		}
		
		KPPicker buildPicker(Context context) {
			KPPicker kpPicker = new KPPicker();
			kpPicker.allPictureAlbumName = context.getString(R.string.kp_all_picture);
			kpPicker.pickCount = this.pickCount;
			kpPicker.pickAspectX = this.pickAspectX;
			kpPicker.pickAspectY = this.pickAspectY;
			kpPicker.pickMaxWidth = this.pickMaxWidth;
			kpPicker.pickMaxHeight = this.pickMaxHeight;
			return kpPicker;
		}
	}
}
