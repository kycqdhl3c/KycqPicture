package com.kycq.library.picture.viewer;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;
import com.kycq.library.picture.widget.LoadingDrawable;
import com.kycq.library.picture.widget.ZoomableDraweeView;

class PicturePagerAdapter extends PagerAdapter {
	private LayoutInflater inflater;
	private DisplayMetrics displayMetrics;
	
	private KPViewer kpViewer;
	
	/** 事件监听 */
	private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;
	
	PicturePagerAdapter(Context context,
	                    KPViewer kpViewer,
	                    GestureDetector.SimpleOnGestureListener simpleOnGestureListener) {
		this.inflater = LayoutInflater.from(context);
		this.displayMetrics = context.getResources().getDisplayMetrics();
		this.kpViewer = kpViewer;
		this.simpleOnGestureListener = simpleOnGestureListener;
	}
	
	Uri getItem(int position) {
		return this.kpViewer.pictureList.get(position);
	}
	
	/**
	 * 删除图片地址
	 *
	 * @param position 图片列表位置
	 */
	void removeItem(int position) {
		this.kpViewer.pictureList.remove(position);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return this.kpViewer.pictureList.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	@Override
	public View instantiateItem(ViewGroup container, int position) {
		View convertView = this.inflater.inflate(R.layout.kp_dark_item_picture_pager, container, false);
		ZoomableDraweeView pictureView = (ZoomableDraweeView) convertView.findViewById(R.id.kpPictureView);
		// 图片控件配置
		pictureView.getHierarchy()
				.setPlaceholderImage(
						R.drawable.kp_dark_ic_picture_loading,
						ScalingUtils.ScaleType.CENTER
				);
		pictureView.getHierarchy()
				.setFailureImage(
						R.drawable.kp_dark_ic_picture_error,
						ScalingUtils.ScaleType.CENTER
				);
		pictureView.setTapListener(new GestureDetector.SimpleOnGestureListener() {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				return simpleOnGestureListener.onSingleTapConfirmed(event);
			}
			
		});
		
		Uri pictureUri = this.kpViewer.pictureList.get(position);
		// 加载中图片
		LoadingDrawable loadingDrawable = new LoadingDrawable(this.inflater.getContext(), pictureView);
		loadingDrawable.updateSizes(LoadingDrawable.LARGE);
		loadingDrawable.setProgressRotation(0.8f);
		loadingDrawable.setStartEndTrim(0f, 0.5f);
		loadingDrawable.setAlpha(255);
		pictureView.getHierarchy().setPlaceholderImage(loadingDrawable);
		
		ImageRequest imageRequest =
				ImageRequestBuilder.newBuilderWithSource(pictureUri)
						.setResizeOptions(
								new ResizeOptions(
										this.displayMetrics.widthPixels,
										this.displayMetrics.heightPixels))
						.build();
		
		DraweeController controller =
				Fresco.newDraweeControllerBuilder()
						.setControllerListener(new PictureControllerListener(loadingDrawable))
						.setOldController(pictureView.getController())
						.setImageRequest(imageRequest)
						.build();
		
		pictureView.setController(controller);
		
		container.addView(convertView);
		return convertView;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	private static class PictureControllerListener extends BaseControllerListener<ImageInfo> {
		private LoadingDrawable mLoadingDrawable;
		
		PictureControllerListener(LoadingDrawable loadingDrawable) {
			mLoadingDrawable = loadingDrawable;
		}
		
		@Override
		public void onSubmit(String id, Object callerContext) {
			mLoadingDrawable.start();
		}
		
		@Override
		public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
			mLoadingDrawable.stop();
		}
		
		@Override
		public void onFailure(String id, Throwable throwable) {
			mLoadingDrawable.stop();
		}
	}
}
