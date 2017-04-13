package com.kycq.picture;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kycq.library.picture.picker.KPPicker;
import com.kycq.library.picture.viewer.KPViewer;
import com.kycq.library.picture.widget.PictureLayout;
import com.kycq.picture.databinding.ActivitySplashBinding;
import com.kycq.picture.databinding.DialogSettingsBinding;

public class SplashActivity extends AppCompatActivity {
	/** 选取图片 */
	private static final int PICKER = 1;
	/** 查看图片 */
	private static final int VIEWER = 2;
	
	private ActivitySplashBinding mDataBinding;
	
	private boolean isCrop = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
		
		observeViewer();
		observePicker();
	}
	
	private void observeViewer() {
		mDataBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
			}
			
			@Override
			public void onPageSelected(int position) {
				mDataBinding.pictureLayout.setSelectedPosition(position);
			}
		});
	}
	
	/**
	 * 选取图片
	 */
	private void observePicker() {
		mDataBinding.pictureLayout.setOnPictureListener(new PictureLayout.OnPictureListener() {
			@Override
			public void onInsert() {
				new KPPicker.Builder()
						.pickCount(mDataBinding.pictureLayout.getMaxCount() - mDataBinding.pictureLayout.size())
						.pickMaxScale(1080, 0)
						.pickCompressQuality(80)
						.pickEditable(isCrop)
						.pick(SplashActivity.this, PICKER);
			}
			
			@Override
			public void onEdit(int position, Uri pictureUri) {
				mDataBinding.pictureLayout.removePictureUri(position);
				mDataBinding.viewPager.setAdapter(new PicturePagerAdapter(
						SplashActivity.this,
						mDataBinding.pictureLayout.getPictureList(),
						viewClickListener));
				mDataBinding.viewPager.setCurrentItem(mDataBinding.pictureLayout.getSelectedPosition());
			}
			
			@Override
			public void onSelect(int position, Uri pictureUri) {
				mDataBinding.viewPager.setCurrentItem(position);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("设置")
				.setIcon(R.drawable.ic_settings)
				.setVisible(true)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final DialogSettingsBinding dataBinding = DataBindingUtil.inflate(
				getLayoutInflater(),
				R.layout.dialog_settings,
				null, false
		);
		dataBinding.editMaxCount.setText(String.valueOf(mDataBinding.pictureLayout.getMaxCount()));
		dataBinding.editRowCount.setText(String.valueOf(mDataBinding.pictureLayout.getRowCount()));
		dataBinding.editPictureRatio.setText(String.valueOf(mDataBinding.pictureLayout.getPictureRatio()));
		dataBinding.editPictureRound.setText(String.valueOf(mDataBinding.pictureLayout.getPictureRound()));
		dataBinding.cbInsert.setChecked(mDataBinding.pictureLayout.isSupportInsert());
		new AlertDialog.Builder(this)
				.setTitle("设置")
				.setView(dataBinding.getRoot())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int maxCount = 5;
						int rowCount = 0;
						float pictureRatio = 0F;
						float pictureRound = 0F;
						
						String maxCountStr = dataBinding.editMaxCount.getText().toString().trim();
						if (maxCountStr.length() > 0) {
							maxCount = Integer.parseInt(maxCountStr);
						}
						
						String rowCountStr = dataBinding.editRowCount.getText().toString().trim();
						if (rowCountStr.length() > 0) {
							rowCount = Integer.parseInt(rowCountStr);
						}
						
						String pictureRatioStr = dataBinding.editPictureRatio.getText().toString().trim();
						if (pictureRatioStr.length() > 0) {
							pictureRatio = Float.parseFloat(pictureRatioStr);
						}
						
						String pictureRoundStr = dataBinding.editPictureRound.getText().toString().trim();
						if (pictureRoundStr.length() > 0) {
							pictureRound = Float.parseFloat(pictureRoundStr);
						}
						
						mDataBinding.pictureLayout.setMaxCount(maxCount);
						mDataBinding.pictureLayout.setRowCount(rowCount);
						mDataBinding.pictureLayout.setPictureRatio(pictureRatio);
						mDataBinding.pictureLayout.setPictureRound(pictureRound);
						mDataBinding.pictureLayout.setSupportInsert(dataBinding.cbInsert.isChecked());
						isCrop = dataBinding.cbCrop.isChecked();
					}
				})
				.show();
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKER) {
			if (resultCode == RESULT_OK) {
				mDataBinding.pictureLayout.addPictureUri(KPPicker.pickPictureUriList(data));
				mDataBinding.viewPager.setAdapter(new PicturePagerAdapter(
						this,
						mDataBinding.pictureLayout.getPictureList(),
						viewClickListener));
				mDataBinding.pictureLayout.setSelectedPosition(mDataBinding.viewPager.getCurrentItem());
			}
			return;
		}
		if (requestCode == VIEWER) {
			if (resultCode == RESULT_OK) {
				mDataBinding.pictureLayout.setPictureList(KPViewer.viewPictureUriList(data));
				mDataBinding.viewPager.setAdapter(new PicturePagerAdapter(
						this,
						mDataBinding.pictureLayout.getPictureList(),
						viewClickListener));
				mDataBinding.pictureLayout.setSelectedPosition(mDataBinding.viewPager.getCurrentItem());
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private View.OnClickListener viewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			new KPViewer.Builder()
					.viewPictureList(mDataBinding.pictureLayout.getPictureList())
					.viewPosition(mDataBinding.pictureLayout.getSelectedPosition())
					.viewEditable(true)
					.view(SplashActivity.this, VIEWER);
		}
	};
}
