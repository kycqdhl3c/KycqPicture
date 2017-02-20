package com.kycq.picture;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;

import com.kycq.library.picture.picker.Picker;
import com.kycq.library.picture.picker.PicturePickerActivity;
import com.kycq.library.picture.viewer.PictureViewerActivity;
import com.kycq.library.picture.viewer.Viewer;
import com.kycq.library.picture.widget.PictureLayout;
import com.kycq.picture.databinding.ActivitySplashBinding;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
	/** 选取图片 */
	private static final int PICKER = 1;
	/** 查看图片 */
	private static final int VIEWER = 2;
	
	private ActivitySplashBinding mDataBinding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
		
		observePickerCount();
		observeCrop();
		observeAspectX();
		observeAspectY();
		observeMaxWidth();
		observeMaxHeight();
		observePicker();
		
		mDataBinding.editPickerCount.setText("1");
		mDataBinding.checkCrop.setChecked(true);
		mDataBinding.setIsCrop(true);
		mDataBinding.editAspectX.setText("0");
		mDataBinding.editAspectY.setText("0");
		mDataBinding.editMaxWidth.setText("0");
		mDataBinding.editMaxHeight.setText("0");
	}
	
	/**
	 * 输出图片数量
	 */
	private void observePickerCount() {
		mDataBinding.editPickerCount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if (s.length() == 0) {
						return;
					}
					
					int pickerCount = Integer.parseInt(s.toString());
					if (pickerCount <= 0) {
						mDataBinding.editPickerCount.setText("1");
					} else if (pickerCount > 9) {
						mDataBinding.editPickerCount.setText("9");
					} else if (pickerCount == 1) {
						mDataBinding.checkCrop.setEnabled(true);
						mDataBinding.setIsCrop(mDataBinding.checkCrop.isChecked());
					} else {
						mDataBinding.checkCrop.setEnabled(false);
						mDataBinding.setIsCrop(false);
					}
				} catch (NumberFormatException ignored) {
					mDataBinding.editPickerCount.setText("1");
				}
				mDataBinding.editPickerCount.setSelection(mDataBinding.editPickerCount.length());
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				
			}
		});
	}
	
	/**
	 * 裁剪
	 */
	private void observeCrop() {
		mDataBinding.checkCrop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mDataBinding.setIsCrop(isChecked);
			}
		});
	}
	
	/**
	 * 裁剪X轴比例
	 */
	private void observeAspectX() {
		mDataBinding.editAspectX.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if (s.length() == 0) {
						return;
					}
					
					int aspectX = Integer.parseInt(s.toString());
					if (aspectX < 0) {
						mDataBinding.editAspectX.setText("0");
					}
				} catch (NumberFormatException ignored) {
					mDataBinding.editAspectX.setText("0");
				}
				mDataBinding.editAspectX.setSelection(mDataBinding.editAspectX.length());
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}
	
	/**
	 * 裁剪Y轴比例
	 */
	private void observeAspectY() {
		mDataBinding.editAspectY.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if (s.length() == 0) {
						return;
					}
					
					int aspectY = Integer.parseInt(s.toString());
					if (aspectY < 0) {
						mDataBinding.editAspectY.setText("0");
					}
				} catch (NumberFormatException ignored) {
					mDataBinding.editAspectY.setText("0");
				}
				mDataBinding.editAspectY.setSelection(mDataBinding.editAspectY.length());
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}
	
	/**
	 * 最大宽度
	 */
	private void observeMaxWidth() {
		mDataBinding.editMaxWidth.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if (s.length() == 0) {
						return;
					}
					
					int maxWidth = Integer.parseInt(s.toString());
					if (maxWidth < 0) {
						mDataBinding.editMaxWidth.setText("0");
					}
				} catch (NumberFormatException ignored) {
					mDataBinding.editMaxWidth.setText("0");
				}
				mDataBinding.editMaxWidth.setSelection(mDataBinding.editMaxWidth.length());
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}
	
	/**
	 * 最大高度
	 */
	private void observeMaxHeight() {
		mDataBinding.editMaxHeight.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					if (s.length() == 0) {
						return;
					}
					
					int maxHeight = Integer.parseInt(s.toString());
					if (maxHeight < 0) {
						mDataBinding.editMaxHeight.setText("0");
					}
				} catch (NumberFormatException ignored) {
					mDataBinding.editMaxHeight.setText("0");
				}
				mDataBinding.editMaxHeight.setSelection(mDataBinding.editMaxHeight.length());
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}
	
	/**
	 * 选取图片
	 */
	private void observePicker() {
		mDataBinding.pictureLayout.setOnPictureListener(new PictureLayout.OnPictureListener() {
			@Override
			public void onUpload() {
				int pickerCount = Integer.parseInt(mDataBinding.editPickerCount.getText().toString().trim());
				boolean isCrop = mDataBinding.checkCrop.isEnabled() && mDataBinding.checkCrop.isChecked();
				int aspectX = Integer.parseInt(mDataBinding.editAspectX.getText().toString().trim());
				int aspectY = Integer.parseInt(mDataBinding.editAspectY.getText().toString().trim());
				int maxWidth = Integer.parseInt(mDataBinding.editMaxWidth.getText().toString().trim());
				int maxHeight = Integer.parseInt(mDataBinding.editMaxHeight.getText().toString().trim());
				
				Intent intent = new Intent(SplashActivity.this, PicturePickerActivity.class);
				intent.putExtra(Picker.PICKER_COUNT, pickerCount);
				intent.putExtra(Picker.PICKER_CROP, isCrop);
				intent.putExtra(Picker.PICKER_ASPECT_X, aspectX);
				intent.putExtra(Picker.PICKER_ASPECT_Y, aspectY);
				intent.putExtra(Picker.PICKER_MAX_WIDTH, maxWidth);
				intent.putExtra(Picker.PICKER_MAX_HEIGHT, maxHeight);
				startActivityForResult(intent, PICKER);
			}
			
			@Override
			public void onView(int position, Uri pictureUri) {
				ArrayList<Uri> pictureList = mDataBinding.pictureLayout.getPictureList();
				Intent intent = new Intent(SplashActivity.this, PictureViewerActivity.class);
				intent.putExtra(Viewer.VIEWER_LIST, pictureList);
				intent.putExtra(Viewer.VIEWER_POSITION, position);
				intent.putExtra(Viewer.VIEWER_EDIT, true);
				startActivityForResult(intent, VIEWER);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKER) {
			if (resultCode == RESULT_OK) {
				ArrayList<Uri> pictureList = data.getParcelableArrayListExtra(Picker.PICKER_LIST);
				mDataBinding.pictureLayout.addPictureUri(pictureList);
			}
			return;
		}
		if (requestCode == VIEWER) {
			if (resultCode == RESULT_OK) {
				ArrayList<Uri> pictureList = data.getParcelableArrayListExtra(Viewer.VIEWER_LIST);
				mDataBinding.pictureLayout.setPictureList(pictureList);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
