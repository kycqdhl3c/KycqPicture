package com.kycq.library.picture.picker;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.kycq.library.picture.R;
import com.kycq.library.picture.widget.LoadingDialog;

import java.util.ArrayList;

public class PicturePickerActivity extends AppCompatActivity {
	/** 读取文件权限 */
	private static final int PERMISSION_STORAGE = 1;
	
	/** 获取图片 */
	private static final int PICTURE = 1;
	/** 裁剪图片 */
	private static final int CROP = 2;
	
	/** 图片信息状态保存KEY */
	private static final String PICTURE_INFO_STATE_KEY = "PicturePickerActivity_pictureInfo";
	
	/** 是否裁剪 */
	private boolean isCrop = false;
	
	/** 裁剪X轴比例 */
	private int mAspectX;
	/** 裁剪Y轴比例 */
	private int mAspectY;
	
	/** 临时图片信息 */
	private PictureInfo mPictureInfo;
	
	/** 读取相册任务 */
	private AlbumTask mAlbumTask;
	/** 图片信息列表 */
	private PictureListAdapter mAdapter;
	
	/** 缩放处理任务 */
	private ScaleTask mScaleTask;
	
	/** 加载中窗口 */
	private LoadingDialog mLoadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_activity_picture_picker);
		
		if (savedInstanceState != null) {
			mPictureInfo = savedInstanceState.getParcelable(PICTURE_INFO_STATE_KEY);
		}
		
		int pickerCount = 1;
		int maxWidth = 0, maxHeight = 0;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			pickerCount = bundle.getInt(Picker.PICKER_COUNT, 1);
			isCrop = bundle.getBoolean(Picker.PICKER_CROP, false);
			mAspectX = bundle.getInt(Picker.PICKER_ASPECT_X, 0);
			mAspectY = bundle.getInt(Picker.PICKER_ASPECT_Y, 0);
			maxWidth = bundle.getInt(Picker.PICKER_MAX_WIDTH, 0);
			maxHeight = bundle.getInt(Picker.PICKER_MAX_HEIGHT, 0);
		}
		
		observeToolbar(0, pickerCount);
		observeRecyclerView(pickerCount);
		observeAlbum();
		observeScale(maxWidth, maxHeight);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PICTURE_INFO_STATE_KEY, mPictureInfo);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mAdapter.isSingle()) {
			menu.add(Menu.NONE, R.id.kpMenuPicker, Menu.NONE, getString(R.string.kp_pick))
					.setIcon(R.drawable.kp_ic_picture_done)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			ActivityCompat.finishAfterTransition(this);
			return true;
		} else if (item.getItemId() == R.id.kpMenuPicker) {
			mScaleTask.executeScale(mAdapter.getPictureInfoArray());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 操作栏
	 *
	 * @param currentCount 当前图片数量
	 * @param pickerCount  输出图片数量
	 */
	private void observeToolbar(int currentCount, int pickerCount) {
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (pickerCount == 1) {
			getSupportActionBar().setTitle(getString(R.string.kp_select_picture));
		} else {
			getSupportActionBar().setTitle(getString(R.string.kp_format_select_picture, currentCount, pickerCount));
		}
	}
	
	/**
	 * 图片信息列表
	 *
	 * @param pickerCount 输出图片数量
	 */
	private void observeRecyclerView(int pickerCount) {
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.kpRecyclerView);
		recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		recyclerView.getItemAnimator().setChangeDuration(0);
		
		mAdapter = new PictureListAdapter(this, pickerCount);
		mAdapter.setOnPickerListener(new PictureListAdapter.OnPickerListener() {
			@Override
			public void onCamera() {
				mPictureInfo = PictureInfo.createPictureInfo();
				
				showLoading();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureInfo.getContentUri(PicturePickerActivity.this));
				startActivityForResult(intent, PICTURE);
			}
			
			@Override
			public void onPicker(PictureInfo pictureInfo, int currentCount, int pickerCount) {
				observeToolbar(currentCount, pickerCount);
				
				if (mAdapter.isSingle()) {
					mPictureInfo = pictureInfo;
					if (isCrop) {
						showLoading();
						cropPicture();
					} else {
						mScaleTask.executeScale(mPictureInfo);
					}
				}
			}
		});
		recyclerView.setAdapter(mAdapter);
	}
	
	/**
	 * 读取相册照片列表
	 */
	private void observeAlbum() {
		if (!requestStorage()) {
			return;
		}
		mAlbumTask = new AlbumTask(this, new AlbumTask.OnAlbumListener() {
			@Override
			public void onAlbum(ArrayList<PictureInfo> pictureInfoList) {
				mAdapter.setPictureInfoList(pictureInfoList);
			}
		});
		mAlbumTask.execute();
	}
	
	/**
	 * 缩放处理
	 *
	 * @param maxWidth  输出图片最大宽度
	 * @param maxHeight 输出图片最大高度
	 */
	private void observeScale(int maxWidth, int maxHeight) {
		mScaleTask = new ScaleTask(maxWidth, maxHeight, new ScaleTask.OnScaleListener() {
			@Override
			public void scaleStart() {
				showLoading();
			}
			
			@Override
			public void scaleResult(ArrayList<Uri> pictureUriList) {
				hideLoading();
				
				Intent data = new Intent();
				data.putExtra(Picker.PICKER_LIST, pictureUriList);
				setResult(RESULT_OK, data);
				finish();
			}
		});
	}
	
	/**
	 * 裁剪处理
	 */
	private void cropPicture() {
		PictureInfo cameraPictureInfo = mPictureInfo;
		mPictureInfo = PictureInfo.createPictureInfo();
		
		try {
			Uri inputUri = cameraPictureInfo.getContentUri(this);
			Uri outputUri = mPictureInfo.getContentUri(this);
			
			Intent intent = new Intent("com.android.camera.action.CROP");
			
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.setDataAndType(inputUri, "image/*");
			intent.putExtra("crop", "true");
			if (mAspectX > 0 && mAspectY > 0) {
				intent.putExtra("aspectX", mAspectX);
				intent.putExtra("aspectY", mAspectY);
			}
			intent.putExtra("scale", false);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				intent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, outputUri));
			}
			intent.putExtra("return-data", false);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("noFaceDetection", false);
			startActivityForResult(intent, CROP);
		} catch (Exception ignored) {
			ignored.printStackTrace();
			mScaleTask.executeScale(cameraPictureInfo);
		}
	}
	
	/**
	 * 显示加载中窗口
	 */
	private void showLoading() {
		if (!isFinishing()) {
			if (mLoadingDialog == null) {
				mLoadingDialog = new LoadingDialog(this);
			}
			mLoadingDialog.show();
		}
	}
	
	/**
	 * 隐藏加载中窗口
	 */
	private void hideLoading() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICTURE) {
			if (resultCode == RESULT_OK) {
				// 广播添加至相册
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(mPictureInfo.getPictureUri());
				sendBroadcast(intent);
				
				mPictureInfo.obtainPictureSize();
				mAdapter.addPictureInfo(mPictureInfo);
				mAdapter.pickerPictureUri(0);
			} else {
				mPictureInfo.removePictureInfo();
			}
			hideLoading();
			return;
		}
		if (requestCode == CROP) {
			if (resultCode == RESULT_OK) {
				mScaleTask.executeScale(mPictureInfo);
			} else {
				mPictureInfo.removePictureInfo();
				hideLoading();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 请求读取文件权限
	 *
	 * @return true已授权
	 */
	private boolean requestStorage() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_STORAGE) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				observeAlbum();
			} else {
				finish();
			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		hideLoading();
		if (mAlbumTask != null) {
			mAlbumTask.cancel(true);
		}
		if (mScaleTask != null) {
			mScaleTask.cancel(true);
		}
	}
}
