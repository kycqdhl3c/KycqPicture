package com.kycq.library.picture.viewer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.kycq.library.picture.R;

public class KPPictureViewerActivity extends AppCompatActivity {
	/** 查看参数信息 */
	private KPViewer kpViewer;
	
	private View toolbarView;
	private TextView kpTitle;
	private ViewPager kpViewPager;
	
	private boolean isFullScreen;
	
	private PicturePagerAdapter picturePagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		this.kpViewer = bundle.getParcelable(KPViewer.VIEWER);
		if (this.kpViewer == null) {
			finish();
			return;
		}
		
		observeViews();
		observeToolbar();
		observePicturePager();
		toggleFullScreen();
	}
	
	private void observeViews() {
		getDelegate().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(ActivityCompat.getColor(this, R.color.kpStatusBarColor));
		}
		
		setContentView(R.layout.kp_activity_picture_viewer);
		
		this.kpViewPager = (ViewPager) findViewById(R.id.kpViewPager);
	}
	
	private void observeToolbar() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		
		this.toolbarView = findViewById(R.id.kpToolbar);
		View kpBack = findViewById(R.id.kpBack);
		kpBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		this.kpTitle = (TextView) findViewById(R.id.kpTitle);
		
		View kpEdit = findViewById(R.id.kpEdit);
		kpEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				picturePagerAdapter.removeItem(kpViewPager.getCurrentItem());
				
				Intent data = new Intent();
				data.putExtra(KPViewer.VIEWER_LIST, kpViewer.pictureList);
				setResult(RESULT_OK, data);
				if (kpViewer.pictureList.size() == 0) {
					finish();
				} else {
					onPageSelected(kpViewPager.getCurrentItem());
				}
			}
		});
		kpEdit.setVisibility(this.kpViewer.editable ? View.VISIBLE : View.GONE);
	}
	
	private void observePicturePager() {
		this.picturePagerAdapter = new PicturePagerAdapter(
				this, this.kpViewer,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent event) {
						if (kpViewer.editable) {
							toggleFullScreen();
						} else {
							ActivityCompat.finishAfterTransition(KPPictureViewerActivity.this);
						}
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
				KPPictureViewerActivity.this.onPageSelected(position);
			}
		});
		this.kpViewPager.setAdapter(this.picturePagerAdapter);
		this.kpViewPager.setCurrentItem(this.kpViewer.position, false);
		onPageSelected(this.kpViewer.position);
	}
	
	private void onPageSelected(int position) {
		if (kpViewer.pictureList.size() == 1) {
			kpTitle.setText(R.string.kp_view);
		} else {
			kpTitle.setText(
					getString(R.string.kp_format_view,
							position + 1, picturePagerAdapter.getCount())
			);
		}
	}
	
	/**
	 * 切换全屏
	 */
	private void toggleFullScreen() {
		this.isFullScreen = !this.isFullScreen;
		if (isFullScreen) {
			// 状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
			hideToolbar();
		} else {
			// 状态栏
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			showToolbar();
		}
	}
	
	private void showToolbar() {
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
}
