package com.kycq.library.picture.picker;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;

class AlbumInfo implements Parcelable {
	/** 相册路径 */
	String albumPath;
	/** 相册名称 */
	String albumName;
	/** 相册图片信息列表 */
	ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
	
	private AlbumInfo() {
	}
	
	private AlbumInfo(Parcel in) {
		albumPath = in.readString();
		albumName = in.readString();
		pictureInfoList = in.createTypedArrayList(PictureInfo.CREATOR);
	}
	
	static AlbumInfo buildByName(String albumName) {
		AlbumInfo albumInfo = new AlbumInfo();
		albumInfo.albumPath = "";
		albumInfo.albumName = albumName;
		return albumInfo;
	}
	
	static AlbumInfo buildByPath(String albumPath) {
		AlbumInfo albumInfo = new AlbumInfo();
		albumInfo.albumPath = albumPath;
		albumInfo.albumName = new File(albumPath).getName();
		return albumInfo;
	}
	
	/**
	 * 判断是否是所有图片的相册列表
	 *
	 * @return true是
	 */
	boolean isFullAlbum() {
		return this.albumPath == null || this.albumPath.equals("");
	}
	
	int size() {
		return this.pictureInfoList.size();
	}
	
	@Override
	public int hashCode() {
		return this.albumPath.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AlbumInfo) {
			AlbumInfo other = (AlbumInfo) obj;
			if (this.albumPath == null) {
				return other.albumPath == null;
			}
			return this.albumPath.equals(other.albumPath);
		}
		return false;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(albumPath);
		dest.writeString(albumName);
		dest.writeTypedList(pictureInfoList);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {
		@Override
		public AlbumInfo createFromParcel(Parcel in) {
			return new AlbumInfo(in);
		}
		
		@Override
		public AlbumInfo[] newArray(int size) {
			return new AlbumInfo[size];
		}
	};
}
