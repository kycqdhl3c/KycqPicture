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

import java.util.ArrayList;

class PicturePagerAdapter extends PagerAdapter {
	private LayoutInflater mInflater;
	/** 屏幕数据 */
	private DisplayMetrics mMetrics;

	/** 图片地址列表 */
	private ArrayList<Uri> mPictureList;

	/** 事件监听 */
	private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;

	/**
	 * 构造方法
	 *
	 * @param context     设备上下文环境
	 * @param pictureList 图片地址列表
	 */
	PicturePagerAdapter(Context context, ArrayList<Uri> pictureList) {
		mInflater = LayoutInflater.from(context);
		mMetrics = context.getResources().getDisplayMetrics();
		mPictureList = pictureList;
	}

	/**
	 * 设置事件监听
	 *
	 * @param listener 事件监听
	 */
	void setSimpleOnGestureListener(GestureDetector.SimpleOnGestureListener listener) {
		mSimpleOnGestureListener = listener;
	}

	/**
	 * 删除图片地址
	 *
	 * @param position 图片列表位置
	 */
	void removePicture(int position) {
		mPictureList.remove(position);
		notifyDataSetChanged();
	}

	/**
	 * 获取图片地址列表
	 *
	 * @return 图片地址列表
	 */
	ArrayList<Uri> getPictureList() {
		return mPictureList;
	}

	@Override
	public int getCount() {
		return mPictureList.size();
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
	public Object instantiateItem(final ViewGroup container, final int position) {
		View view = mInflater.inflate(R.layout.kp_item_picture_pager, container, false);
		ZoomableDraweeView pictureView = (ZoomableDraweeView) view.findViewById(R.id.kpPictureView);
		// 图片控件配置
		pictureView.getHierarchy()
				.setPlaceholderImage(
						R.drawable.kp_ic_picture_loading,
						ScalingUtils.ScaleType.CENTER
				);
		pictureView.getHierarchy()
				.setFailureImage(
						R.drawable.kp_ic_picture_error,
						ScalingUtils.ScaleType.CENTER
				);
		pictureView.setTapListener(new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				return mSimpleOnGestureListener.onSingleTapConfirmed(event);
			}

		});

		Uri pictureUri = mPictureList.get(position);
		// 加载中图片
		LoadingDrawable loadingDrawable = new LoadingDrawable(mInflater.getContext(), pictureView);
		loadingDrawable.updateSizes(LoadingDrawable.LARGE);
		loadingDrawable.setProgressRotation(0.8f);
		loadingDrawable.setStartEndTrim(0f, 0.5f);
		loadingDrawable.setAlpha(255);
		pictureView.getHierarchy().setPlaceholderImage(loadingDrawable);

		ImageRequest imageRequest =
				ImageRequestBuilder.newBuilderWithSource(pictureUri)
						.setResizeOptions(new ResizeOptions(mMetrics.widthPixels, mMetrics.heightPixels))
						.build();

		DraweeController controller =
				Fresco.newDraweeControllerBuilder()
						.setControllerListener(new PictureControllerListener(loadingDrawable))
						.setOldController(pictureView.getController())
						.setImageRequest(imageRequest)
						.build();

		pictureView.setController(controller);

		container.addView(view);
		return view;
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
