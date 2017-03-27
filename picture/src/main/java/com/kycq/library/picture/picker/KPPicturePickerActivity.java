package com.kycq.library.picture.picker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kycq.library.picture.R;

import java.util.ArrayList;

public class KPPicturePickerActivity extends AppCompatActivity {
	/** 读取文件权限 */
	private static final int PERMISSION_STORAGE = 1;
	
	/** 获取图片 */
	private static final int PICTURE = 1;
	/** 裁剪图片 */
	private static final int CROP = 2;
	
	/** 选择参数信息 */
	private KPPicker kpPicker;
	
	private TextView kpTitle;
	
	private RecyclerView kpRecyclerViewPicture;
	private View kpAlbumLayer;
	private RecyclerView kpRecyclerViewAlbum;
	private View kpActionbar;
	private View kpAlbum;
	private TextView kpAlbumName;
	private TextView kpPreview;
	
	private Animation layerShowAnimation;
	private Animation layerHideAnimation;
	private Animation albumShowAnimation;
	private Animation albumHideAnimation;
	
	private AlbumTask albumTask;
	private AlbumListAdapter albumListAdapter;
	private PictureListAdapter pictureListAdapter;
	
	private ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();
	private PictureInfo tempPictureInfo;
	
	private ScaleTask scaleTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		this.kpPicker = bundle.getParcelable(KPPicker.PICKER);
		if (this.kpPicker == null) {
			finish();
			return;
		}
		
		observeViews();
		observeToolbar();
		toggleToolbar();
		requestAlbum();
	}
	
	private void observeViews() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(R.color.kpStatusBarColor));
		}
		
		setContentView(R.layout.kp_activity_picture_picker);
		
		this.kpRecyclerViewPicture = (RecyclerView) findViewById(R.id.kpRecyclerViewPicture);
		
		this.kpAlbumLayer = findViewById(R.id.kpAlbumLayer);
		this.kpAlbumLayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAlbumAndLayer();
			}
		});
		this.kpRecyclerViewAlbum = (RecyclerView) findViewById(R.id.kpRecyclerViewAlbum);
		
		this.kpActionbar = findViewById(R.id.kpActionbar);
		this.kpAlbum = findViewById(R.id.kpAlbum);
		this.kpAlbum.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAlbumAndLayer();
			}
		});
		this.kpAlbumName = (TextView) findViewById(R.id.kpAlbumName);
		this.kpPreview = (TextView) findViewById(R.id.kpPreview);
		
		this.layerShowAnimation = new AlphaAnimation(0F, 1F);
		this.layerShowAnimation.setDuration(400);
		this.layerHideAnimation = new AlphaAnimation(1F, 0F);
		this.layerHideAnimation.setDuration(400);
		this.layerHideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				kpAlbumLayer.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		
		this.albumShowAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_PARENT, 1F,
				Animation.RELATIVE_TO_PARENT, 0F);
		this.albumShowAnimation.setDuration(400);
		this.albumHideAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_SELF, 0F,
				Animation.RELATIVE_TO_PARENT, 0F,
				Animation.RELATIVE_TO_PARENT, 1F);
		this.albumHideAnimation.setDuration(400);
		this.albumHideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				kpRecyclerViewAlbum.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}
	
	private void observeToolbar() {
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.kp_picture_picker_toolbar);
		
		View customView = getSupportActionBar().getCustomView();
		Toolbar toolbar = (Toolbar) customView.getParent();
		toolbar.setContentInsetsAbsolute(0, 0);
		toolbar.setLayoutParams(
				new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						getResources().getDimensionPixelSize(R.dimen.kpActionBarSize)
				)
		);
		
		View kpBack = customView.findViewById(R.id.kpBack);
		kpBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		View kpPick = customView.findViewById(R.id.kpPick);
		kpPick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestScale();
			}
		});
		this.kpTitle = (TextView) customView.findViewById(R.id.kpTitle);
	}
	
	private void toggleToolbar() {
		if (this.kpPicker.pickCount == 1) {
			this.kpTitle.setText(R.string.kp_select_picture);
		} else {
			this.kpTitle.setText(
					getString(R.string.kp_format_select_picture,
							this.pictureInfoList.size(), this.kpPicker.pickCount)
			);
		}
	}
	
	private void requestAlbum() {
		if (!requestStoragePermission()) {
			return;
		}
		this.albumTask = new AlbumTask(
				this.kpPicker, this.getContentResolver(),
				new AlbumTask.OnAlbumListener() {
					@Override
					public void onAlbum(ArrayList<AlbumInfo> albumInfoList) {
						kpActionbar.setVisibility(View.VISIBLE);
						observeAlbumList(albumInfoList);
					}
				});
		this.albumTask.execute();
	}
	
	private void observeAlbumList(ArrayList<AlbumInfo> albumInfoList) {
		this.kpRecyclerViewAlbum.setLayoutManager(new LinearLayoutManager(this));
		this.kpRecyclerViewAlbum.getItemAnimator().setChangeDuration(0);
		this.kpRecyclerViewAlbum.getItemAnimator().setAddDuration(0);
		this.kpRecyclerViewAlbum.getItemAnimator().setMoveDuration(0);
		this.kpRecyclerViewAlbum.getItemAnimator().setRemoveDuration(0);
		
		this.albumListAdapter = new AlbumListAdapter(
				this, albumInfoList,
				new AlbumListAdapter.OnAlbumListener() {
					@Override
					public void onAlbum(AlbumInfo albumInfo) {
						if (kpAlbumLayer.getVisibility() == View.VISIBLE) {
							toggleAlbumAndLayer();
						}
						kpAlbumName.setText(albumInfo.albumName);
						observePictureList(albumInfo);
					}
				}
		);
		this.kpRecyclerViewAlbum.setAdapter(this.albumListAdapter);
	}
	
	private void toggleAlbumAndLayer() {
		if (this.kpAlbumLayer.getVisibility() == View.VISIBLE) {
			this.kpAlbumLayer.startAnimation(this.layerHideAnimation);
			this.kpRecyclerViewAlbum.startAnimation(this.albumHideAnimation);
		} else {
			this.kpAlbumLayer.startAnimation(this.layerShowAnimation);
			this.kpAlbumLayer.setVisibility(View.VISIBLE);
			this.kpRecyclerViewAlbum.startAnimation(this.albumShowAnimation);
			this.kpRecyclerViewAlbum.setVisibility(View.VISIBLE);
		}
	}
	
	private void observePictureList(AlbumInfo albumInfo) {
		this.kpRecyclerViewPicture.setLayoutManager(new GridLayoutManager(this, 3));
		this.kpRecyclerViewPicture.getItemAnimator().setChangeDuration(0);
		this.kpRecyclerViewPicture.getItemAnimator().setAddDuration(0);
		this.kpRecyclerViewPicture.getItemAnimator().setMoveDuration(0);
		this.kpRecyclerViewPicture.getItemAnimator().setRemoveDuration(0);
		
		this.pictureListAdapter = new PictureListAdapter(
				this, albumInfo,
				new PictureListAdapter.OnPictureListener() {
					@Override
					public void onCamera() {
						showLoading();
						
						tempPictureInfo = kpPicker.createPictureInfo();
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(
								MediaStore.EXTRA_OUTPUT,
								kpPicker.getPictureContentUri(KPPicturePickerActivity.this, tempPictureInfo)
						);
						startActivityForResult(intent, PICTURE);
					}
					
					@Override
					public boolean onPicture(PictureInfo pictureInfo) {
						int index = pictureInfoList.indexOf(pictureInfo);
						if (index != -1) {
							pictureInfo.selected = false;
							pictureInfoList.remove(index);
						} else {
							if (pictureInfoList.size() == kpPicker.pickCount) {
								return false;
							}
							pictureInfo.selected = true;
							
							// if (mPickCount == 1) {
							// 	mPictureInfo = pictureInfo;
							// 	if (kpPicker.isCrop) {
							// 		showLoading();
							// 		cropPicture();
							// 	} else {
							// 		mPictureInfoList.add(pictureInfo);
							// 		mScaleTask.executeScale(mPictureInfoList);
							// 	}
							// 	return false;
							// } else {
							pictureInfoList.add(pictureInfo);
							// }
						}
						toggleToolbar();
						return true;
					}
					
					@Override
					public void onPreview(int position) {
						
					}
				}
		);
		this.kpRecyclerViewPicture.setAdapter(this.pictureListAdapter);
	}
	
	private void requestScale() {
		if (this.scaleTask != null) {
			this.scaleTask.cancel(true);
		}
		
		showLoading();
		this.scaleTask = new ScaleTask(
				this.kpPicker, new ScaleTask.OnScaleListener() {
					@Override
					public void onScale(ArrayList<Uri> pictureUriList) {
						hideLoading();
						
						Intent data = new Intent();
						data.putExtra(KPPicker.PICKER_LIST, pictureUriList);
						setResult(RESULT_OK, data);
						finish();
					}
				});
		this.scaleTask.executeScale(this.pictureInfoList);
	}
	
	void showLoading() {
		
	}
	
	void hideLoading() {
		
	}
	
	@Override
	public void onBackPressed() {
		if (this.kpAlbumLayer.getVisibility() == View.VISIBLE) {
			toggleAlbumAndLayer();
		} else {
			ActivityCompat.finishAfterTransition(this);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICTURE) {
			if (resultCode == RESULT_OK) {
				// 广播添加至相册
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(this.tempPictureInfo.pictureUri);
				sendBroadcast(intent);
				
				if (KPPicker.addPictureInfo(
						this.albumListAdapter.getAlbumInfoList(),
						this.tempPictureInfo)) {
					this.albumListAdapter.notifyDataSetChanged();
					this.pictureListAdapter.notifyPickPicture(this.tempPictureInfo);
				} else {
					this.kpPicker.removePictureInfo(this.tempPictureInfo);
				}
			} else {
				this.kpPicker.removePictureInfo(this.tempPictureInfo);
			}
			hideLoading();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		hideLoading();
		if (this.albumTask != null) {
			this.albumTask.cancel(true);
		}
		if (this.scaleTask != null) {
			this.scaleTask.cancel(true);
		}
	}
	
	/**
	 * 请求读取文件权限
	 *
	 * @return true已授权
	 */
	private boolean requestStoragePermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				ActivityCompat.requestPermissions(
						this,
						new String[]{
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						PERMISSION_STORAGE
				);
			} else {
				ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						PERMISSION_STORAGE
				);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_STORAGE) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				requestAlbum();
			} else {
				finish();
			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
