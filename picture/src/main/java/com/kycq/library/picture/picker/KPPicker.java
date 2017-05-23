package com.kycq.library.picture.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.kycq.library.picture.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class KPPicker implements Parcelable {
	/** 暗色主题 */
	public static final int DARK = 1;
	/** 亮色主题 */
	public static final int LIGHT = 2;
	
	/** 选择参数 */
	static final String PICKER = "picker";
	/** 输出图片列表 */
	static final String PICKER_LIST = "pickerList";
	
	@Retention(RetentionPolicy.CLASS)
	@IntDef({DARK, LIGHT})
	@interface PickStyle {
	}
	
	int statusBarColor;
	boolean lightStatusBar;
	int pickerLayoutId = R.layout.kp_light_activity_picture_picker;
	int cameraListLayoutId = R.layout.kp_light_item_camera_list;
	int pictureListLayoutId = R.layout.kp_light_item_picture_list;
	int albumAllListLayoutId = R.layout.kp_light_item_album_all_list;
	int albumListLayoutId = R.layout.kp_light_item_album_list;
	int previewLayoutId = R.layout.kp_light_activity_picture_preview;
	
	String fullAlbumName;
	String cacheAlbumPath;
	int pickCount;
	int pickAspectX;
	int pickAspectY;
	int pickMaxWidth;
	int pickMaxHeight;
	int pickCompressQuality;
	boolean pickEditable;
	
	AlbumInfo fullAlbumInfo;
	AlbumInfo cacheAlbumInfo;
	ArrayList<AlbumInfo> albumInfoList;
	AlbumInfo selectedAlbumInfo;
	
	ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
	PictureInfo pictureInfo;
	
	private KPPicker() {
	}
	
	private KPPicker(Parcel in) {
		statusBarColor = in.readInt();
		lightStatusBar = in.readByte() == 1;
		pickerLayoutId = in.readInt();
		cameraListLayoutId = in.readInt();
		pictureListLayoutId = in.readInt();
		albumAllListLayoutId = in.readInt();
		albumListLayoutId = in.readInt();
		previewLayoutId = in.readInt();
		
		fullAlbumName = in.readString();
		cacheAlbumPath = in.readString();
		pickCount = in.readInt();
		pickAspectX = in.readInt();
		pickAspectY = in.readInt();
		pickMaxWidth = in.readInt();
		pickMaxHeight = in.readInt();
		pickCompressQuality = in.readInt();
		pickEditable = in.readByte() != 0;
		
		selectedAlbumInfo = in.readParcelable(AlbumInfo.class.getClassLoader());
		pictureInfoList = in.createTypedArrayList(PictureInfo.CREATOR);
		pictureInfo = in.readParcelable(PictureInfo.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(statusBarColor);
		dest.writeByte((byte) (lightStatusBar ? 1 : 0));
		dest.writeInt(pickerLayoutId);
		dest.writeInt(cameraListLayoutId);
		dest.writeInt(pictureListLayoutId);
		dest.writeInt(albumAllListLayoutId);
		dest.writeInt(albumListLayoutId);
		dest.writeInt(previewLayoutId);
		
		dest.writeString(fullAlbumName);
		dest.writeString(cacheAlbumPath);
		dest.writeInt(pickCount);
		dest.writeInt(pickAspectX);
		dest.writeInt(pickAspectY);
		dest.writeInt(pickMaxWidth);
		dest.writeInt(pickMaxHeight);
		dest.writeInt(pickCompressQuality);
		dest.writeByte((byte) (pickEditable ? 1 : 0));
		
		dest.writeParcelable(selectedAlbumInfo, flags);
		dest.writeTypedList(pictureInfoList);
		dest.writeParcelable(pictureInfo, flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
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
	
	/**
	 * 创建相机照片信息
	 *
	 * @return 图片信息
	 */
	PictureInfo createPictureInfo() {
		PictureInfo pictureInfo = new PictureInfo();
		pictureInfo.picturePath = this.cacheAlbumInfo.albumPath + "/IMG_" + System.currentTimeMillis() + ".jpg";
		pictureInfo.pictureUri = Uri.fromFile(new File(pictureInfo.picturePath));
		return pictureInfo;
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
	 * @param pictureInfo 图片信息
	 * @return true添加成功
	 */
	boolean addPictureInfo(PictureInfo pictureInfo) {
		pictureInfo.obtainPictureSize();
		if (!pictureInfo.isAvailable()) {
			return false;
		}
		this.fullAlbumInfo.pictureInfoList.add(0, pictureInfo);
		this.cacheAlbumInfo.pictureInfoList.add(0, pictureInfo);
		return true;
	}
	
	public static Uri pickPictureUri(Intent data) {
		return pickPictureUri(data, 0);
	}
	
	private static Uri pickPictureUri(Intent data, int position) {
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
		@PickStyle
		int pickStyle = DARK;
		String fullAlbumName;
		String cacheAlbumPath;
		int pickCount;
		int pickAspectX;
		int pickAspectY;
		int pickMaxWidth;
		int pickMaxHeight;
		int pickCompressQuality = 100;
		boolean pickEditable;
		
		public Builder pickStyle(@PickStyle int pickStyle) {
			this.pickStyle = pickStyle;
			return this;
		}
		
		public Builder pickFullAlbumName(String fullAlbumName) {
			this.fullAlbumName = fullAlbumName;
			return this;
		}
		
		public Builder pickCacheAlbumPath(String cacheAlbumPath) {
			this.cacheAlbumPath = cacheAlbumPath;
			return this;
		}
		
		public Builder pickCount(int pickCount) {
			this.pickCount = pickCount;
			return this;
		}
		
		public Builder pickAspect(int pickAspectX, int pickAspectY) {
			this.pickAspectX = pickAspectX;
			this.pickAspectY = pickAspectY;
			return this;
		}
		
		public Builder pickMaxScale(int pickMaxWidth, int pickMaxHeight) {
			this.pickMaxWidth = pickMaxWidth;
			this.pickMaxHeight = pickMaxHeight;
			return this;
		}
		
		public Builder pickCompressQuality(int pickCompressQuality) {
			if (pickCompressQuality < 0) {
				pickCompressQuality = 0;
			} else if (pickCompressQuality > 100) {
				pickCompressQuality = 100;
			}
			this.pickCompressQuality = pickCompressQuality;
			return this;
		}
		
		public Builder pickEditable(boolean pickEditable) {
			this.pickEditable = pickEditable;
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
			if (this.fullAlbumName == null) {
				this.fullAlbumName = context.getString(R.string.kp_all_picture);
			}
			File cachePath = null;
			if (this.cacheAlbumPath != null) {
				cachePath = new File(this.cacheAlbumPath);
			}
			if (cachePath == null || !cachePath.canWrite()) {
				cachePath = context.getExternalCacheDir();
				if (cachePath == null || !cachePath.canWrite()) {
					cachePath = context.getCacheDir();
					if (cachePath == null || !cachePath.canWrite()) {
						cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						if (cachePath == null || !cachePath.canWrite()) {
							throw new RuntimeException("can't write file!");
						}
					}
				}
				cachePath = new File(cachePath, context.getString(R.string.kp_cache_name));
			}
			if (!cachePath.exists() && !cachePath.mkdirs()) {
				throw new RuntimeException("can't write file!");
			}
			this.cacheAlbumPath = cachePath.getPath();
			
			KPPicker kpPicker = new KPPicker();
			if (this.pickStyle == DARK) {
				kpPicker.statusBarColor = 0xFF222222;
				kpPicker.lightStatusBar = false;
				kpPicker.pickerLayoutId = R.layout.kp_dark_activity_picture_picker;
				kpPicker.cameraListLayoutId = R.layout.kp_dark_item_camera_list;
				kpPicker.pictureListLayoutId = R.layout.kp_dark_item_picture_list;
				kpPicker.albumAllListLayoutId = R.layout.kp_dark_item_album_all_list;
				kpPicker.albumListLayoutId = R.layout.kp_dark_item_album_list;
				kpPicker.previewLayoutId = R.layout.kp_dark_activity_picture_preview;
			} else if (this.pickStyle == LIGHT) {
				kpPicker.statusBarColor = 0xFFF0F0F0;
				kpPicker.lightStatusBar = true;
				kpPicker.pickerLayoutId = R.layout.kp_light_activity_picture_picker;
				kpPicker.cameraListLayoutId = R.layout.kp_light_item_camera_list;
				kpPicker.pictureListLayoutId = R.layout.kp_light_item_picture_list;
				kpPicker.albumAllListLayoutId = R.layout.kp_light_item_album_all_list;
				kpPicker.albumListLayoutId = R.layout.kp_light_item_album_list;
				kpPicker.previewLayoutId = R.layout.kp_light_activity_picture_preview;
			}
			
			kpPicker.fullAlbumName = this.fullAlbumName;
			kpPicker.cacheAlbumPath = this.cacheAlbumPath;
			kpPicker.pickCount = this.pickCount;
			kpPicker.pickAspectX = this.pickAspectX;
			kpPicker.pickAspectY = this.pickAspectY;
			kpPicker.pickMaxWidth = this.pickMaxWidth;
			kpPicker.pickMaxHeight = this.pickMaxHeight;
			kpPicker.pickCompressQuality = this.pickCompressQuality;
			kpPicker.pickEditable = this.pickEditable;
			return kpPicker;
		}
	}
	
	
}
