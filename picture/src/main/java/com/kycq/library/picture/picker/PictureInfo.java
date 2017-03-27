package com.kycq.library.picture.picker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

class PictureInfo implements Parcelable {
	/** 图片地址 */
	public Uri pictureUri;
	/** 图片路径 */
	public String picturePath;
	/** 图片宽度 */
	public int pictureWidth;
	/** 图片高度 */
	public int pictureHeight;
	/** 图片选择状态 */
	public boolean selected;
	
	PictureInfo() {
	}
	
	private PictureInfo(Parcel in) {
		pictureUri = in.readParcelable(Uri.class.getClassLoader());
		picturePath = in.readString();
		pictureWidth = in.readInt();
		pictureHeight = in.readInt();
		selected = in.readByte() != 0;
	}
	
	/**
	 * 图片是否可用
	 *
	 * @return true可用
	 */
	boolean isAvailable() {
		return pictureWidth > 0 && pictureHeight > 0;
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
		dest.writeByte((byte) (selected ? 1 : 0));
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
