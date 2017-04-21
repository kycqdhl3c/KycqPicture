package com.kycq.library.picture.picker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kycq.library.picture.R;

import java.util.ArrayList;

public class KPPicturePreviewActivity extends AppCompatActivity {
	/** 图片信息列表 */
	static final String PREVIEW_PICTURE_INFO_LIST = "previewPictureInfoList";
	/** 图片位置 */
	static final String PREVIEW_PICTURE_POSITION = "previewPicturePosition";
	/** 图片预览 */
	static final String PREVIEW_PREVIEW = "previewPreview";
	/** 图片选择 */
	static final String PREVIEW_DONE = "previewDone";
	
	private KPPicker kpPicker;
	
	private View toolbarView;
	private TextView kpTitle;
	private TextView kpDone;
	private ViewPager kpViewPager;
	private View kpActionbar;
	private View kpEdit;
	private TextView kpSelected;
	
	private boolean isFullScreen;
	
	private boolean isPreview;
	private PicturePagerAdapter picturePagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		
		this.kpPicker = bundle.getParcelable(KPPicker.PICKER);
		ArrayList<PictureInfo> pictureInfoList;
		pictureInfoList = bundle.getParcelableArrayList(PREVIEW_PICTURE_INFO_LIST);
		if (pictureInfoList == null) {
			this.isPreview = true;
			pictureInfoList = new ArrayList<>(this.kpPicker.pictureInfoList);
		}
		int position = bundle.getInt(PREVIEW_PICTURE_POSITION);
		if (savedInstanceState != null) {
			this.kpPicker = savedInstanceState.getParcelable(KPPicker.PICKER);
		}
		
		observeViews();
		observeToolbar();
		observePicturePager(pictureInfoList, position);
		alterPickCount();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KPPicker.PICKER, this.kpPicker);
	}
	
	private void observeViews() {
		getDelegate().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(ActivityCompat.getColor(this, R.color.kpStatusBarColor));
		}
		
		setContentView(R.layout.kp_activity_picture_preview);
		
		this.kpViewPager = (ViewPager) findViewById(R.id.kpViewPager);
	}
	
	@SuppressLint("InflateParams")
	private void observeToolbar() {
		if (getSupportActionBar() == null) {
			this.toolbarView = getLayoutInflater().inflate(R.layout.kp_picture_picker_toolbar, null);
			addContentView(
					this.toolbarView,
					new ViewGroup.MarginLayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							getResources().getDimensionPixelSize(R.dimen.kpActionBarSize)
					)
			);
		} else {
			getSupportActionBar().setDisplayShowHomeEnabled(false);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			getSupportActionBar().setCustomView(R.layout.kp_picture_picker_toolbar);
			this.toolbarView = getSupportActionBar().getCustomView();
			Toolbar toolbar = (Toolbar) this.toolbarView.getParent();
			toolbar.setContentInsetsAbsolute(0, 0);
			toolbar.setLayoutParams(
					new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.MATCH_PARENT,
							getResources().getDimensionPixelSize(R.dimen.kpActionBarSize)
					));
		}
		
		View kpBack = this.toolbarView.findViewById(R.id.kpBack);
		kpBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		this.kpTitle = (TextView) this.toolbarView.findViewById(R.id.kpTitle);
		this.kpDone = (TextView) this.toolbarView.findViewById(R.id.kpDone);
		this.kpDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra(KPPicker.PICKER, kpPicker);
				data.putExtra(PREVIEW_PREVIEW, isPreview);
				data.putExtra(PREVIEW_DONE, true);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		this.kpActionbar = findViewById(R.id.kpActionbar);
		this.kpEdit = findViewById(R.id.kpEdit);
		this.kpSelected = (TextView) findViewById(R.id.kpSelected);
		this.kpSelected.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedPictureInfo();
			}
		});
	}
	
	private void selectedPictureInfo() {
		PictureInfo pictureInfo = this.picturePagerAdapter.getItem(this.kpViewPager.getCurrentItem());
		if (pictureInfo.selected) {
			pictureInfo.selected = false;
			this.kpPicker.pictureInfoList.remove(pictureInfo);
		} else {
			if (this.kpPicker.pictureInfoList.size() < this.kpPicker.pickCount) {
				pictureInfo.selected = true;
				this.kpPicker.pictureInfoList.add(pictureInfo);
			}
		}
		this.kpSelected.setSelected(pictureInfo.selected);
		Intent data = new Intent();
		data.putExtra(KPPicker.PICKER, this.kpPicker);
		data.putExtra(PREVIEW_PREVIEW, this.isPreview);
		setResult(RESULT_OK, data);
		alterPickCount();
	}
	
	private void alterPickCount() {
		if (this.kpPicker.pickCount > 1) {
			this.kpDone.setEnabled(this.kpPicker.pictureInfoList.size() > 0);
			this.kpDone.setText(
					getString(R.string.kp_format_selected,
							this.kpPicker.pictureInfoList.size(), this.kpPicker.pickCount)
			);
		}
	}
	
	private void observePicturePager(ArrayList<PictureInfo> pictureInfoList, int position) {
		this.picturePagerAdapter = new PicturePagerAdapter(
				this, pictureInfoList,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent event) {
						toggleFullScreen();
						return true;
					}
				});
		// 切换动画
		this.kpViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
			@Override
			public void transformPage(View page, float position) {
				int pageWidth = page.getWidth();
				int pageHeight = page.getHeight();
				
				if (position < -1) {// [-Infinity, -1)
					page.setAlpha(0);
				} else if (position <= 1) {// [-1, 1]
					float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
					float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
					float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
					
					if (position < 0) {
						page.setTranslationX(horizontalMargin - verticalMargin / 2);
					} else {
						page.setTranslationY(-horizontalMargin + verticalMargin / 2);
					}
					
					page.setScaleX(scaleFactor);
					page.setScaleY(scaleFactor);
					
					page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * (1 - 0.5f));
				} else {// (1, +Infinity]
					page.setAlpha(0);
				}
			}
		});
		this.kpViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				KPPicturePreviewActivity.this.onPageSelected(position);
			}
		});
		this.kpViewPager.setAdapter(this.picturePagerAdapter);
		this.kpViewPager.setCurrentItem(position, false);
		onPageSelected(position);
	}
	
	private void onPageSelected(int position) {
		kpTitle.setText(
				getString(R.string.kp_format_select_picture,
						position + 1, picturePagerAdapter.getCount())
		);
		PictureInfo pictureInfo = picturePagerAdapter.getItem(position);
		kpSelected.setSelected(pictureInfo.selected);
	}
	
	/**
	 * 切换全屏
	 */
	private void toggleFullScreen() {
		if (this.isFullScreen) {
			showToolbar();
			showActionbar();
		} else {
			hideToolbar();
			hideActionbar();
		}
		this.isFullScreen = !this.isFullScreen;
	}
	
	private void showToolbar() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().show();
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, -1F,
				Animation.RELATIVE_TO_SELF, 0F);
		animation.setDuration(300);
		this.toolbarView.startAnimation(animation);
		this.toolbarView.setVisibility(View.VISIBLE);
	}
	
	private void hideToolbar() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, -1F);
		animation.setDuration(300);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				toolbarView.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		this.toolbarView.startAnimation(animation);
	}
	
	private void showActionbar() {
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 1F,
				Animation.RELATIVE_TO_SELF, 0F);
		animation.setDuration(300);
		this.kpActionbar.startAnimation(animation);
		this.kpActionbar.setVisibility(View.VISIBLE);
	}
	
	private void hideActionbar() {
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 1F);
		animation.setDuration(300);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				kpActionbar.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		this.kpActionbar.startAnimation(animation);
	}
}
