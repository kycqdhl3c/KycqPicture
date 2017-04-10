package com.kycq.library.picture.picker;

import android.os.AsyncTask;

import java.util.ArrayList;

public class PickTask extends AsyncTask<Void,Void,Void> {
	/** 所有照片相册 */
	private AlbumInfo fullAlbumInfo;
	/** 已选中的照片列表 */
	private ArrayList<PictureInfo> pictureInfoList;
	
	@Override
	protected Void doInBackground(Void... params) {
		ArrayList<PictureInfo> oldPictureInfoList = this.pictureInfoList;
		ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
		for (PictureInfo pictureInfo : oldPictureInfoList) {
			int index = fullAlbumInfo.pictureInfoList.indexOf(pictureInfo);
			if (index > 0) {
				PictureInfo selectedPictureInfo = fullAlbumInfo.pictureInfoList.get(index);
				selectedPictureInfo.selected = true;
				pictureInfoList.add(selectedPictureInfo);
			}
		}
		this.pictureInfoList = pictureInfoList;
		return null;
	}
	
	public interface OnPickListener {
		
		void onPick(ArrayList<PictureInfo> pictureInfoList);
	}
}
