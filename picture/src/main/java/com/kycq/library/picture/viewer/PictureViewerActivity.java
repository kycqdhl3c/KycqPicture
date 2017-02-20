package com.kycq.library.picture.viewer;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.kycq.library.picture.R;

import java.util.ArrayList;

public class PictureViewerActivity extends AppCompatActivity {
	/** 隐藏标题栏消息 */
	private static final int MESSAGE = 1;
	
	/** 图片展示控件 */
	private ViewPager mViewPager;
	/** 图片展示列表 */
	private PicturePagerAdapter mAdapter;
	
	/** 隐藏标题栏事件 */
	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			if (getSupportActionBar() != null) {
				getSupportActionBar().hide();
			}
			return true;
		}
	});
	
	/** 是否编辑 */
	private boolean isEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getDelegate().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setBackgroundDrawableResource(android.R.color.black);
		
		setContentView(R.layout.kp_activity_picture_viewer);
		
		if (getSupportActionBar() != null) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setBackgroundDrawable(new ColorDrawable(0x99000000));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.hide();
		}
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		
		isEdit = bundle.getBoolean(Viewer.VIEWER_EDIT, false);
		ArrayList<Uri> pictureList = bundle.getParcelableArrayList(Viewer.VIEWER_LIST);
		int position = bundle.getInt(Viewer.VIEWER_POSITION, 0);
		observeViewPager(pictureList, position);
		toggleActionBar();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (isEdit) {
			menu.add(Menu.NONE, R.id.kpMenuDelete, Menu.NONE, getString(R.string.kp_delete))
					.setIcon(R.drawable.kp_ic_picture_delete)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else if (item.getItemId() == R.id.kpMenuDelete) {
			mAdapter.removePicture(mViewPager.getCurrentItem());
			Intent data = new Intent();
			data.putExtra(Viewer.VIEWER_LIST, mAdapter.getPictureList());
			setResult(RESULT_OK, data);
			if (mAdapter.getCount() == 0) {
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 切换标题栏显示/隐藏
	 */
	private void toggleActionBar() {
		if (getSupportActionBar() == null) {
			return;
		}
		
		mHandler.removeMessages(MESSAGE);
		if (getSupportActionBar().isShowing()) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
			mHandler.sendEmptyMessageDelayed(MESSAGE, 3000);
		}
	}
	
	/**
	 * 图片展示列表
	 *
	 * @param pictureList 图片地址列表
	 * @param position    图片列表默认展示位置
	 */
	private void observeViewPager(ArrayList<Uri> pictureList, int position) {
		mViewPager = (ViewPager) findViewById(R.id.kpViewPager);
		mAdapter = new PicturePagerAdapter(this, pictureList);
		mAdapter.setSimpleOnGestureListener(new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				toggleActionBar();
				return true;
			}
		});
		mViewPager.setAdapter(mAdapter);
		mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
			@Override
			public void transformPage(View page, float position) {
				int pageWidth = page.getWidth();
				int pageHeight = page.getHeight();
				
				if (position < -1) {// [-Infinity, -1)
					ViewCompat.setAlpha(page, 0);
				} else if (position <= 1) {// [-1, 1]
					float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
					float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
					float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
					
					if (position < 0) {
						ViewCompat.setTranslationX(page, horizontalMargin - verticalMargin / 2);
					} else {
						ViewCompat.setTranslationX(page, -horizontalMargin + verticalMargin / 2);
					}
					
					ViewCompat.setScaleX(page, scaleFactor);
					ViewCompat.setScaleY(page, scaleFactor);
					
					ViewCompat.setAlpha(page, 0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * (1 - 0.5f));
				} else {// (1, +Infinity]
					ViewCompat.setAlpha(page, 0);
				}
			}
		});
		mViewPager.setCurrentItem(position, false);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(MESSAGE);
	}
}
