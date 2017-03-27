package com.kycq.library.picture.picker;

import java.io.File;
import java.util.ArrayList;

class AlbumInfo {
	/** 相册路径 */
	public String albumPath;
	/** 相册名称 */
	public String albumName;
	/** 相册图片信息列表 */
	public ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
	
	static AlbumInfo buildByName(String albumName) {
		AlbumInfo albumInfo = new AlbumInfo();
		albumInfo.albumPath = null;
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
		return this.albumPath == null;
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
}
