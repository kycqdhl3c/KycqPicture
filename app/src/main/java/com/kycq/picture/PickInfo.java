package com.kycq.picture;

import android.os.Parcel;
import android.os.Parcelable;

import com.kycq.library.picture.picker.KPPicker;

class PickInfo implements Parcelable {
	int pickStyle = KPPicker.DARK;
	
	int aspectX;
	int aspectY;
	int scaleWidth;
	int scaleHeight;
	int compressQuality = 80;
	boolean isEditable = true;
	
	PickInfo() {
	}
	
	private PickInfo(Parcel in) {
		pickStyle = in.readInt();
		aspectX = in.readInt();
		aspectY = in.readInt();
		scaleWidth = in.readInt();
		scaleHeight = in.readInt();
		compressQuality = in.readInt();
		isEditable = in.readByte() != 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(pickStyle);
		dest.writeInt(aspectX);
		dest.writeInt(aspectY);
		dest.writeInt(scaleWidth);
		dest.writeInt(scaleHeight);
		dest.writeInt(compressQuality);
		dest.writeByte((byte) (isEditable ? 1 : 0));
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<PickInfo> CREATOR = new Creator<PickInfo>() {
		@Override
		public PickInfo createFromParcel(Parcel in) {
			return new PickInfo(in);
		}
		
		@Override
		public PickInfo[] newArray(int size) {
			return new PickInfo[size];
		}
	};
}
