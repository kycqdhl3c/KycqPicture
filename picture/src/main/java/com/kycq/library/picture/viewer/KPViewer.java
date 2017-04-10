package com.kycq.library.picture.viewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class KPViewer implements Parcelable {
	/** 查看参数 */
	static final String VIEWER = "viewer";
	/** 图片列表 */
	static final String VIEWER_LIST = "viewerList";
	
	ArrayList<Uri> pictureList;
	int position;
	boolean editable;
	
	private KPViewer() {
	}
	
	private KPViewer(Parcel in) {
		this.pictureList = in.createTypedArrayList(Uri.CREATOR);
		this.position = in.readInt();
		this.editable = in.readInt() == 1;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(this.pictureList);
		dest.writeInt(this.position);
		dest.writeInt(this.editable ? 1 : 0);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<KPViewer> CREATOR = new Creator<KPViewer>() {
		@Override
		public KPViewer createFromParcel(Parcel in) {
			return new KPViewer(in);
		}
		
		@Override
		public KPViewer[] newArray(int size) {
			return new KPViewer[size];
		}
	};
	
	public static Uri viewPictureUri(Intent data) {
		return viewPictureUri(data, 0);
	}
	
	public static Uri viewPictureUri(Intent data, int position) {
		ArrayList<Uri> pictureUriList = viewPictureUriList(data);
		if (pictureUriList == null || pictureUriList.size() < position) {
			return null;
		}
		return pictureUriList.get(position);
	}
	
	public static ArrayList<Uri> viewPictureUriList(Intent data) {
		return data.getParcelableArrayListExtra(VIEWER_LIST);
	}
	
	public static class Builder {
		private ArrayList<Uri> pictureList;
		private int position;
		private boolean editable;
		
		public Builder viewPictureList(ArrayList<Uri> pictureList) {
			this.pictureList = pictureList;
			return this;
		}
		
		public Builder viewPosition(int position) {
			this.position = position;
			return this;
		}
		
		public Builder viewEditable(boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public void view(Activity activity, int requestCode) {
			Intent intent = new Intent(activity, KPPictureViewerActivity.class);
			intent.putExtra(VIEWER, buildViewer(activity));
			activity.startActivityForResult(intent, requestCode);
		}
		
		private KPViewer buildViewer(Context context) {
			KPViewer kpViewer = new KPViewer();
			kpViewer.pictureList = this.pictureList;
			kpViewer.position = this.position;
			kpViewer.editable = this.editable;
			return kpViewer;
		}
	}
}
