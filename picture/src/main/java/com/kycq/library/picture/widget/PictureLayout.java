package com.kycq.library.picture.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

import java.util.ArrayList;

public class PictureLayout extends ViewGroup {
	/** 最多图片数量 */
	private int mMaxCount;
	/** 单行图片数量 */
	private int mRowCount;
	
	/** 图片展示控件ID */
	private int mPictureLayoutId;
	/** 图片展示比例 */
	private float mPictureRatio;
	/** 水平边界 */
	private int mHorizontalPadding;
	/** 垂直边界 */
	private int mVerticalPadding;
	
	/** 支持上传图片 */
	private boolean isPictureUpload;
	/** 上传图片控件 */
	private ImageView mIVUpload;
	
	/** 图片监听 */
	private OnPictureListener mOnPictureListener;
	
	/** 图片地址列表 */
	private ArrayList<Uri> mPictureList = new ArrayList<>();
	
	/**
	 * 构造方法
	 *
	 * @param context 设备上下文环境
	 */
	public PictureLayout(Context context) {
		this(context, null);
	}
	
	/**
	 * 构造方法
	 *
	 * @param context 设备上下文环境
	 * @param attrs   属性集合
	 */
	public PictureLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PictureLayout);
		mMaxCount = a.getInt(R.styleable.PictureLayout_picture_maxCount, 4);
		mRowCount = a.getInt(R.styleable.PictureLayout_picture_rowCount, 4);
		mPictureLayoutId = a.getResourceId(R.styleable.PictureLayout_picture_pictureView, -1);
		mHorizontalPadding = (int) a.getDimension(R.styleable.PictureLayout_picture_horizontalPadding, 0);
		mVerticalPadding = (int) a.getDimension(R.styleable.PictureLayout_picture_verticalPadding, 0);
		mPictureRatio = a.getFloat(R.styleable.PictureLayout_picture_pictureRatio, 1f);
		
		isPictureUpload = a.getBoolean(R.styleable.PictureLayout_picture_pictureUpload, false);
		mIVUpload = createUploadView();
		int imageResId = a.getResourceId(R.styleable.PictureLayout_picture_uploadPicture, -1);
		if (imageResId != -1) {
			setUploadImageResource(imageResId);
		}
		int backgroundResId = a.getResourceId(R.styleable.PictureLayout_picture_uploadBackground, -1);
		if (backgroundResId != -1) {
			setUploadBackgroundResource(backgroundResId);
		}
		mIVUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnPictureListener != null) {
					mOnPictureListener.onUpload();
				}
			}
		});
		if (isPictureUpload) {
			addView(mIVUpload);
		}
		
		a.recycle();
	}
	
	/**
	 * 设置最多图片数量
	 *
	 * @param maxCount 最多图片数量
	 */
	public void setMaxCount(int maxCount) {
		mMaxCount = maxCount;
		requestLayout();
	}
	
	/**
	 * 设置单行图片数量
	 *
	 * @param rowCount 单行图片数量
	 */
	public void setRowCount(int rowCount) {
		mRowCount = rowCount;
		requestLayout();
	}
	
	/**
	 * 设置图片展示比例
	 *
	 * @param pictureRatio 图片展示比例
	 */
	public void setPictureRatio(float pictureRatio) {
		mPictureRatio = pictureRatio;
		requestLayout();
	}
	
	/**
	 * 设置是否支持上传图片
	 *
	 * @param pictureUpload true支持上传图片
	 */
	private void setPictureUpload(boolean pictureUpload) {
		isPictureUpload = pictureUpload;
	}
	
	/**
	 * 设置上传控件图标
	 *
	 * @param resId 图标资源ID
	 */
	private void setUploadImageResource(int resId) {
		mIVUpload.setImageResource(resId);
	}
	
	/**
	 * 设置上传控件背景
	 *
	 * @param resId 背景资源ID
	 */
	private void setUploadBackgroundResource(int resId) {
		mIVUpload.setBackgroundResource(resId);
	}
	
	/**
	 * 设置图片监听
	 *
	 * @param listener 图片监听
	 */
	public void setOnPictureListener(OnPictureListener listener) {
		mOnPictureListener = listener;
	}
	
	/**
	 * 获取图片地址列表
	 *
	 * @return 图片地址列表
	 */
	public ArrayList<Uri> getPictureList() {
		return new ArrayList<>(mPictureList);
	}
	
	/**
	 * 设置图片地址列表
	 *
	 * @param pictureList 图片地址列表
	 */
	public void setPictureList(ArrayList<Uri> pictureList) {
		mPictureList.clear();
		removeAllViews();
		addPictureUri(pictureList);
	}
	
	/**
	 * 添加图片地址列表(当添加的图片数量大于最多图片数量,过滤掉多余的图片)
	 *
	 * @param pictureList 图片地址列表
	 */
	public void addPictureUri(ArrayList<Uri> pictureList) {
		if (pictureList == null) {
			return;
		}
		for (Uri pictureUri : pictureList) {
			if (mPictureList.size() >= mMaxCount) {
				break;
			}
			
			mPictureList.add(pictureUri);
			addPictureView();
		}
		
		if (mPictureList.size() == mMaxCount) {
			removeView(mIVUpload);
		} else if (mIVUpload.getParent() == null && isPictureUpload) {
			addView(mIVUpload);
		}
		
		requestLayout();
	}
	
	/**
	 * 添加图片地址
	 *
	 * @param pictureStr 图片地址字符串
	 */
	public void addPictureUri(String pictureStr) {
		if (pictureStr == null) {
			return;
		}
		addPictureUri(Uri.parse(pictureStr));
	}
	
	/**
	 * 添加图片地址
	 *
	 * @param pictureUri 图片地址
	 */
	public void addPictureUri(Uri pictureUri) {
		if (pictureUri == null) {
			return;
		}
		if (mPictureList.size() >= mMaxCount) {
			return;
		}
		
		mPictureList.add(pictureUri);
		addPictureView();
		
		if (mPictureList.size() == mMaxCount) {
			removeView(mIVUpload);
		} else if (mIVUpload.getParent() == null && isPictureUpload) {
			addView(mIVUpload);
		}
		
		requestLayout();
	}
	
	/**
	 * 删除图片地址
	 *
	 * @param position 图片列表位置
	 */
	public void removePictureUri(int position) {
		if (position < 0 || position >= mPictureList.size()) {
			return;
		}
		
		mPictureList.remove(position);
		removeViewAt(position);
		
		if (mPictureList.size() < mMaxCount
				&& mIVUpload.getParent() == null
				&& isPictureUpload) {
			addView(mIVUpload);
		}
		
		requestLayout();
	}
	
	/**
	 * 删除图片地址
	 *
	 * @param pictureUri 图片地址
	 */
	public void removePictureUri(Uri pictureUri) {
		removePictureUri(mPictureList.indexOf(pictureUri));
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		int parentWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		
		int childWidth = (parentWidthSize - getPaddingLeft() - getPaddingRight() - (mRowCount - 1) * mHorizontalPadding) / mRowCount;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidth * mPictureRatio), MeasureSpec.EXACTLY);
		
		int childCount = getChildCount();
		
		int viewWidthSize = parentWidthSize;
		if (parentWidthMode == MeasureSpec.AT_MOST && childCount < mRowCount) {
			viewWidthSize = getPaddingLeft() + getPaddingRight() + childWidth * childCount + mHorizontalPadding * (childCount - 1);
		}
		
		int viewHeightSize = getPaddingTop() + getPaddingBottom();
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			
			if (index % mRowCount == 0) {
				viewHeightSize += child.getMeasuredHeight();
				if (index != 0) {
					viewHeightSize += mVerticalPadding;
				}
			}
		}
		
		setMeasuredDimension(viewWidthSize, viewHeightSize);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int top = getPaddingTop();
		int left = getPaddingLeft();
		
		int childCount = getChildCount();
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			
			child.layout(left, top, left + width, top + height);
			
			if (index % mRowCount == mRowCount - 1) {
				top += height;
				if (index != 0) {
					top += mVerticalPadding;
				}
				left = getPaddingLeft();
			} else {
				left += width + mHorizontalPadding;
			}
			
			loadPicture(child, index);
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(super.onSaveInstanceState());
		ss.maxCount = mMaxCount;
		ss.rowCount = mRowCount;
		ss.pictureRatio = mPictureRatio;
		ss.horizontalPadding = mHorizontalPadding;
		ss.verticalPadding = mVerticalPadding;
		ss.isPictureUpload = isPictureUpload;
		return ss;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedState) {
			SavedState ss = (SavedState) state;
			super.onRestoreInstanceState(ss.getSuperState());
			
			mMaxCount = ss.maxCount;
			mRowCount = ss.rowCount;
			mPictureRatio = ss.pictureRatio;
			mHorizontalPadding = ss.horizontalPadding;
			mVerticalPadding = ss.verticalPadding;
			isPictureUpload = ss.isPictureUpload;
		}
		super.onRestoreInstanceState(state);
	}
	
	/**
	 * 创建上传控件
	 *
	 * @return 上传控件
	 */
	protected ImageView createUploadView() {
		ImageView ivUpload = new ImageView(getContext());
		ivUpload.setScaleType(ImageView.ScaleType.CENTER);
		return ivUpload;
	}
	
	/**
	 * 添加图片展示控件
	 */
	private void addPictureView() {
		SimpleDraweeView pictureView;
		if (mPictureLayoutId == -1) {
			pictureView = new SimpleDraweeView(getContext());
		} else {
			pictureView = (SimpleDraweeView) LayoutInflater.from(getContext()).inflate(mPictureLayoutId, this, false);
		}
		pictureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = indexOfChild(v);
				if (mOnPictureListener != null) {
					mOnPictureListener.onView(position, mPictureList.get(position));
				}
			}
		});
		addView(pictureView, mPictureList.size() - 1);
	}
	
	/**
	 * 展示图片加载
	 *
	 * @param child 展示图片控件
	 * @param index 图片列表位置
	 */
	private void loadPicture(View child, int index) {
		if (!(child instanceof SimpleDraweeView)) {
			return;
		}
		
		SimpleDraweeView pictureView = (SimpleDraweeView) child;
		pictureView.setController(
				Fresco.newDraweeControllerBuilder()
						.setOldController(
								pictureView.getController()
						)
						.setImageRequest(
								ImageRequestBuilder.newBuilderWithSource(mPictureList.get(index))
										.setResizeOptions(
												new ResizeOptions(
														pictureView.getMeasuredWidth(),
														pictureView.getMeasuredHeight()
												)
										)
										.build()
						)
						.build()
		);
	}
	
	private static class SavedState extends BaseSavedState {
		/** 最多图片数量 */
		private int maxCount;
		/** 单行图片数量 */
		private int rowCount;
		
		/** 图片展示比例 */
		private float pictureRatio;
		/** 水平边界 */
		private int horizontalPadding;
		/** 垂直边界 */
		private int verticalPadding;
		
		/** 支持上传图片 */
		private boolean isPictureUpload;
		
		@SuppressWarnings("WeakerAccess")
		public SavedState(Parcel source) {
			super(source);
			maxCount = source.readInt();
			rowCount = source.readInt();
			pictureRatio = source.readFloat();
			horizontalPadding = source.readInt();
			verticalPadding = source.readInt();
			isPictureUpload = source.readInt() == 1;
		}
		
		@SuppressWarnings("WeakerAccess")
		public SavedState(Parcelable superState) {
			super(superState);
		}
		
		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(maxCount);
			out.writeInt(rowCount);
			out.writeFloat(pictureRatio);
			out.writeInt(horizontalPadding);
			out.writeInt(verticalPadding);
			out.writeInt(isPictureUpload ? 1 : 0);
		}
		
		public static final Creator<SavedState> CREATOR =
				new Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}
					
					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}
	
	/**
	 * 图片监听
	 */
	public interface OnPictureListener {
		/**
		 * 图片上传
		 */
		void onUpload();
		
		/**
		 * 查看图片
		 *
		 * @param position   图片列表位置
		 * @param pictureUri 图片地址
		 */
		void onView(int position, Uri pictureUri);
	}
	
}
