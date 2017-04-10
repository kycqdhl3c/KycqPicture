package com.kycq.library.picture.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

import java.util.ArrayList;
import java.util.Stack;

public class PictureLayout extends ViewGroup {
	/** 视图重用栈 */
	private Stack<View> scrapStack = new Stack<>();
	
	/** 最多图片数量 */
	private int maxCount;
	/** 单行图片数量 */
	private int rowCount;
	
	/** 图片展示控件ID */
	private int pictureViewId;
	/** 图片展示比例 */
	private float pictureRatio;
	/** 图片圆角大小 */
	private float pictureRound;
	/** 水平边界 */
	private int horizontalPadding;
	/** 垂直边界 */
	private int verticalPadding;
	
	/** 编辑图标 */
	private Drawable editDrawable;
	
	/** 是否支持插入图片 */
	private boolean supportInsert;
	/** 插入图片控件 */
	private ImageView ivInsert;
	
	/** 图片操作监听 */
	private OnPictureListener onPictureListener;
	
	/** 选中位置 */
	private int selectedPosition = -1;
	/** 图片地址列表 */
	private ArrayList<Uri> pictureList = new ArrayList<>();
	
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
		this.maxCount = a.getInt(R.styleable.PictureLayout_picture_maxCount, 4);
		this.rowCount = a.getInt(R.styleable.PictureLayout_picture_rowCount, 4);
		this.pictureViewId = a.getResourceId(R.styleable.PictureLayout_picture_pictureViewLayout, -1);
		this.pictureRatio = a.getFloat(R.styleable.PictureLayout_picture_pictureRatio, 1F);
		this.pictureRound = a.getDimension(R.styleable.PictureLayout_picture_pictureRound, 0F);
		this.horizontalPadding = (int) a.getDimension(R.styleable.PictureLayout_picture_horizontalPadding, 0);
		this.verticalPadding = (int) a.getDimension(R.styleable.PictureLayout_picture_verticalPadding, 0);
		
		int editDrawableId = a.getResourceId(R.styleable.PictureLayout_picture_editDrawable, -1);
		if (editDrawableId != -1) {
			this.editDrawable = getResources().getDrawable(editDrawableId);
		}
		
		this.supportInsert = a.getBoolean(R.styleable.PictureLayout_picture_supportInsert, false);
		this.ivInsert = createInsertView();
		int insertDrawableId = a.getResourceId(R.styleable.PictureLayout_picture_insertDrawable, -1);
		if (insertDrawableId != -1) {
			this.ivInsert.setImageResource(insertDrawableId);
		}
		int insertBackgroundId = a.getResourceId(R.styleable.PictureLayout_picture_insertBackground, -1);
		if (insertBackgroundId != -1) {
			this.ivInsert.setBackgroundResource(insertBackgroundId);
		}
		this.ivInsert.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onPictureListener != null) {
					onPictureListener.onInsert();
				}
			}
		});
		if (this.supportInsert) {
			resetInsertView();
		}
		
		a.recycle();
	}
	
	/**
	 * 获取最多图片数量
	 *
	 * @return 最多图片数量
	 */
	public int getMaxCount() {
		return this.maxCount;
	}
	
	/**
	 * 设置最多图片数量
	 *
	 * @param maxCount 最多图片数量
	 */
	public void setMaxCount(int maxCount) {
		if (this.maxCount == maxCount) {
			return;
		}
		this.maxCount = maxCount;
		int selectedPosition = this.selectedPosition;
		setPictureList(getPictureList());
		setSelectedPosition(selectedPosition);
	}
	
	/**
	 * 获取单行图片数量
	 *
	 * @return 单行图片数量
	 */
	public int getRowCount() {
		return this.rowCount;
	}
	
	/**
	 * 设置单行图片数量
	 *
	 * @param rowCount 单行图片数量
	 */
	public void setRowCount(int rowCount) {
		if (this.rowCount == rowCount) {
			return;
		}
		this.rowCount = rowCount;
		requestLayout();
	}
	
	/**
	 * 获取图片展示比例
	 *
	 * @return 图片展示比例
	 */
	public float getPictureRatio() {
		return this.pictureRatio;
	}
	
	/**
	 * 设置图片展示比例
	 *
	 * @param pictureRatio 图片展示比例
	 */
	public void setPictureRatio(float pictureRatio) {
		if (this.pictureRatio == pictureRatio) {
			return;
		}
		this.pictureRatio = pictureRatio;
		requestLayout();
	}
	
	/**
	 * 获取图片圆角大小
	 *
	 * @return 图片圆角大小
	 */
	public float getPictureRound() {
		return this.pictureRound;
	}
	
	/**
	 * 设置图片圆角大小
	 *
	 * @param pictureRound 图片圆角大小
	 */
	public void setPictureRound(float pictureRound) {
		if (this.pictureRound == pictureRound) {
			return;
		}
		this.pictureRound = pictureRound;
		resetContentView(0);
	}
	
	/**
	 * 是否支持插入图片
	 *
	 * @return true支持
	 */
	public boolean isSupportInsert() {
		return this.supportInsert;
	}
	
	/**
	 * 设置是否支持插入图片
	 *
	 * @param supportInsert true支持
	 */
	public void setSupportInsert(boolean supportInsert) {
		if (this.supportInsert == supportInsert) {
			return;
		}
		this.supportInsert = supportInsert;
		resetInsertView();
	}
	
	
	/**
	 * 设置插入控件图标
	 *
	 * @param resId 图标资源ID
	 */
	private void setInsertDrawableId(int resId) {
		this.ivInsert.setImageResource(resId);
	}
	
	/**
	 * 设置插入控件背景
	 *
	 * @param resId 背景资源ID
	 */
	private void setInsertBackgroundId(int resId) {
		this.ivInsert.setBackgroundResource(resId);
	}
	
	/**
	 * 设置图片监听
	 *
	 * @param listener 图片监听
	 */
	public void setOnPictureListener(OnPictureListener listener) {
		this.onPictureListener = listener;
	}
	
	/**
	 * 获取选中位置
	 *
	 * @return 选中位置
	 */
	public int getSelectedPosition() {
		return this.selectedPosition;
	}
	
	/**
	 * 设置选中位置
	 *
	 * @param selectedPosition 选中位置
	 */
	public void setSelectedPosition(int selectedPosition) {
		if (this.selectedPosition == selectedPosition) {
			if (selectedPosition >= 0 && selectedPosition < size()) {
				selectedContentView(selectedPosition, true);
			}
		} else {
			int oldSelectedPosition = this.selectedPosition;
			this.selectedPosition = selectedPosition;
			
			if (oldSelectedPosition >= 0 && oldSelectedPosition < size()) {
				selectedContentView(oldSelectedPosition, false);
			}
			if (selectedPosition >= 0 && selectedPosition < size()) {
				selectedContentView(selectedPosition, true);
			} else if (selectedPosition >= size() && size() > 0) {
				selectedContentView(size() - 1, true);
			}
		}
	}
	
	/**
	 * 获取图片地址大小
	 *
	 * @return 图片地址大小
	 */
	public int size() {
		return this.pictureList.size();
	}
	
	/**
	 * 获取图片地址列表
	 *
	 * @return 图片地址列表
	 */
	public ArrayList<Uri> getPictureList() {
		return new ArrayList<>(this.pictureList);
	}
	
	/**
	 * 设置图片地址列表
	 *
	 * @param pictureList 图片地址列表
	 */
	public void setPictureList(ArrayList<Uri> pictureList) {
		this.selectedPosition = -1;
		int count = this.pictureList.size();
		for (int index = 0; index < count; index++) {
			removePictureUri(0);
		}
		addPictureUri(pictureList);
	}
	
	/**
	 * 添加图片地址列表(当添加的图片数量大于最多图片数量,过滤掉多余的图片)
	 *
	 * @param pictureUrlList 图片地址列表
	 */
	public void addPictureUrl(ArrayList<String> pictureUrlList) {
		if (pictureUrlList == null) {
			return;
		}
		for (String pictureUrl : pictureUrlList) {
			if (this.pictureList.size() >= this.maxCount) {
				break;
			}
			addPictureUrl(pictureUrl);
		}
	}
	
	/**
	 * 添加图片地址列表(当添加的图片数量大于最多图片数量,过滤掉多余的图片)
	 *
	 * @param pictureUriList 图片地址列表
	 */
	public void addPictureUri(ArrayList<Uri> pictureUriList) {
		if (pictureUriList == null) {
			return;
		}
		for (Uri pictureUri : pictureUriList) {
			if (this.pictureList.size() >= this.maxCount) {
				break;
			}
			addPictureUri(pictureUri);
		}
	}
	
	/**
	 * 添加图片地址
	 *
	 * @param pictureUrl 图片地址
	 */
	public void addPictureUrl(String pictureUrl) {
		if (pictureUrl == null) {
			return;
		}
		addPictureUri(Uri.parse(pictureUrl));
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
		if (this.pictureList.size() >= this.maxCount) {
			return;
		}
		
		addPictureView(pictureUri);
		resetInsertView();
	}
	
	/**
	 * 删除图片地址
	 *
	 * @param pictureUri 图片地址
	 */
	public void removePictureUri(Uri pictureUri) {
		removePictureUri(this.pictureList.indexOf(pictureUri));
	}
	
	/**
	 * 删除图片地址
	 *
	 * @param position 图片列表地址
	 */
	public void removePictureUri(int position) {
		if (position < 0 || position >= this.pictureList.size()) {
			return;
		}
		
		removeContentView(position);
		resetInsertView();
		
		if (this.selectedPosition >= position) {
			if (this.selectedPosition > 0) {
				setSelectedPosition(this.selectedPosition - 1);
			} else if (this.pictureList.size() > 0) {
				setSelectedPosition(0);
			} else {
				setSelectedPosition(-1);
			}
		}
		resetContentView(position);
	}
	
	/**
	 * 添加图片展示控件
	 *
	 * @param pictureUri 图片地址
	 */
	private void addPictureView(Uri pictureUri) {
		int position = this.pictureList.size();
		this.pictureList.add(pictureUri);
		RelativeLayout contentLayout;
		if (scrapStack.isEmpty()) {
			contentLayout = createContentView();
		} else {
			contentLayout = (RelativeLayout) scrapStack.pop();
		}
		addView(contentLayout, this.pictureList.size() - 1);
		resetContentView(position);
	}
	
	/**
	 * 删除图片展示控件
	 *
	 * @param position 图片列表位置
	 */
	private void removeContentView(int position) {
		this.pictureList.remove(position);
		RelativeLayout contentLayout = (RelativeLayout) getChildAt(position);
		removeViewAt(position);
		scrapStack.push(contentLayout);
	}
	
	/**
	 * 设置图片展示控件选中状态
	 *
	 * @param position 图片列表位置
	 * @param selected 选中状态
	 */
	private void selectedContentView(int position, boolean selected) {
		RelativeLayout contentLayout = (RelativeLayout) getChildAt(position);
		contentLayout.setSelected(selected);
	}
	
	/**
	 * 创建插入控件
	 *
	 * @return 插入控件
	 */
	private ImageView createInsertView() {
		ImageView ivUpload = new ImageView(getContext());
		MarginLayoutParams layoutParams = new MarginLayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
		);
		setLayoutParamsMargin(layoutParams);
		ivUpload.setLayoutParams(layoutParams);
		ivUpload.setScaleType(ImageView.ScaleType.CENTER);
		return ivUpload;
	}
	
	/**
	 * 重置插入控件
	 */
	private void resetInsertView() {
		if (!this.supportInsert && this.ivInsert.getParent() != null) {
			removeView(this.ivInsert);
		} else if (this.ivInsert.getParent() != null && this.pictureList.size() >= this.maxCount) {
			removeView(this.ivInsert);
		} else if (this.supportInsert && this.ivInsert.getParent() == null) {
			addView(this.ivInsert);
		}
	}
	
	/**
	 * 创建图片展示控件
	 *
	 * @return 图片展示控件
	 */
	private RelativeLayout createContentView() {
		RelativeLayout contentLayout = new RelativeLayout(getContext());
		
		// 图片控件
		View pictureView = createPictureView();
		contentLayout.addView(pictureView);
		
		// 编辑控件
		View editView = createEditView();
		contentLayout.addView(editView);
		
		return contentLayout;
	}
	
	/**
	 * 创建图片控件
	 *
	 * @return 图片控件
	 */
	private View createPictureView() {
		SimpleDraweeView pictureView;
		if (this.pictureViewId == -1) {
			pictureView = new SimpleDraweeView(getContext());
		} else {
			pictureView = (SimpleDraweeView) LayoutInflater.from(getContext()).inflate(this.pictureViewId, this, false);
		}
		return pictureView;
	}
	
	/**
	 * 创建编辑控件
	 *
	 * @return 编辑控件
	 */
	private View createEditView() {
		ImageView editView = new ImageView(getContext());
		editView.setImageDrawable(this.editDrawable);
		return editView;
	}
	
	/**
	 * 重置图片展示控件
	 *
	 * @param startIndex 重置图片列表开始位置
	 */
	private void resetContentView(int startIndex) {
		for (int index = startIndex; index < this.pictureList.size(); index++) {
			RelativeLayout contentLayout = (RelativeLayout) getChildAt(index);
			bindContentView(contentLayout, index);
			bindPictureView(contentLayout.getChildAt(0), index);
			bindEditView(contentLayout.getChildAt(1), index);
		}
	}
	
	/**
	 * 绑定图片展示控件
	 *
	 * @param contentLayout 图片展示控件
	 * @param position      图片列表位置
	 */
	private void bindContentView(RelativeLayout contentLayout, int position) {
		contentLayout.setSelected(this.selectedPosition == position);
	}
	
	/**
	 * 绑定图片控件
	 *
	 * @param view     图片控件
	 * @param position 图片列表位置
	 */
	private void bindPictureView(View view, final int position) {
		SimpleDraweeView pictureView = (SimpleDraweeView) view;
		pictureView.getHierarchy().setRoundingParams(new RoundingParams().setCornersRadius(this.pictureRound));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
		);
		setLayoutParamsMargin(layoutParams);
		pictureView.setLayoutParams(layoutParams);
		pictureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onPictureListener != null) {
					onPictureListener.onSelect(position, pictureList.get(position));
				}
			}
		});
	}
	
	/**
	 * 绑定编辑控件
	 *
	 * @param view     编辑控件
	 * @param position 图片列表位置
	 */
	private void bindEditView(View view, final int position) {
		ImageView editView = (ImageView) view;
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		editView.setLayoutParams(layoutParams);
		editView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri pictureUri = pictureList.get(position);
				if (onPictureListener != null) {
					onPictureListener.onEdit(position, pictureUri);
				}
			}
		});
	}
	
	/**
	 * 设置布局边缘参数
	 *
	 * @param layoutParams 布局参数
	 */
	private void setLayoutParamsMargin(ViewGroup.MarginLayoutParams layoutParams) {
		if (this.editDrawable != null) {
			int margin = this.editDrawable.getIntrinsicWidth() / 2;
			layoutParams.topMargin = margin;
			layoutParams.rightMargin = margin;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		int parentWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		
		int childWidth = (parentWidthSize - getPaddingLeft() - getPaddingRight() - (this.rowCount - 1) * this.horizontalPadding) / this.rowCount;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidth * this.pictureRatio), MeasureSpec.EXACTLY);
		
		int childCount = getChildCount();
		
		int viewWidthSize = parentWidthSize;
		if (parentWidthMode == MeasureSpec.AT_MOST && childCount < this.rowCount) {
			viewWidthSize = getPaddingLeft() + getPaddingRight() + childWidth * childCount + this.horizontalPadding * (childCount - 1);
		}
		
		int viewHeightSize = getPaddingTop() + getPaddingBottom();
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			
			if (index % this.rowCount == 0) {
				viewHeightSize += child.getMeasuredHeight();
				if (index != 0) {
					viewHeightSize += this.verticalPadding;
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
			
			MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
			child.layout(
					left + layoutParams.leftMargin,
					top + layoutParams.topMargin,
					left + width - layoutParams.rightMargin,
					top + height - layoutParams.bottomMargin
			);
			
			if (index % this.rowCount == this.rowCount - 1) {
				top += height;
				if (index != 0) {
					top += this.verticalPadding;
				}
				left = getPaddingLeft();
			} else {
				left += width + this.horizontalPadding;
			}
			
			loadPictureUri(child, index);
		}
	}
	
	/**
	 * 加载图片地址
	 *
	 * @param child 图片展示控件
	 * @param index 图片列表位置
	 */
	private void loadPictureUri(View child, int index) {
		if (!(child instanceof ViewGroup)) {
			return;
		}
		
		SimpleDraweeView pictureView = (SimpleDraweeView) ((ViewGroup) child).getChildAt(0);
		pictureView.setController(
				Fresco.newDraweeControllerBuilder()
						.setOldController(pictureView.getController())
						.setImageRequest(
								ImageRequestBuilder.newBuilderWithSource(
										this.pictureList.get(index))
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
	
	@Override
	protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
		if (p instanceof MarginLayoutParams) {
			return (MarginLayoutParams) p;
		}
		return new MarginLayoutParams(p);
	}
	
	@Override
	protected MarginLayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}
	
	@Override
	public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(super.onSaveInstanceState());
		ss.maxCount = this.maxCount;
		ss.rowCount = this.rowCount;
		
		ss.pictureViewId = this.pictureViewId;
		ss.pictureRatio = this.pictureRatio;
		ss.pictureRound = this.pictureRound;
		ss.horizontalPadding = this.horizontalPadding;
		ss.verticalPadding = this.verticalPadding;
		
		ss.supportInsert = this.supportInsert;
		
		ss.selectedPosition = this.selectedPosition;
		ss.pictureList = this.pictureList;
		
		return ss;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedState) {
			SavedState ss = (SavedState) state;
			super.onRestoreInstanceState(ss.getSuperState());
			
			this.maxCount = ss.maxCount;
			this.rowCount = ss.rowCount;
			
			this.pictureViewId = ss.pictureViewId;
			this.pictureRatio = ss.pictureRatio;
			this.pictureRound = ss.pictureRound;
			this.horizontalPadding = ss.horizontalPadding;
			this.verticalPadding = ss.verticalPadding;
			
			this.supportInsert = ss.supportInsert;
			
			setSelectedPosition(ss.selectedPosition);
			setPictureList(ss.pictureList);
		}
		super.onRestoreInstanceState(state);
	}
	
	private static class SavedState extends BaseSavedState {
		/** 最多图片数量 */
		private int maxCount;
		/** 单行图片数量 */
		private int rowCount;
		
		/** 图片展示控件ID */
		private int pictureViewId;
		/** 图片展示比例 */
		private float pictureRatio;
		/** 图片圆角大小 */
		private float pictureRound;
		/** 水平边界 */
		private int horizontalPadding;
		/** 垂直边界 */
		private int verticalPadding;
		
		/** 支持插入图片 */
		private boolean supportInsert;
		
		/** 选中位置 */
		private int selectedPosition;
		/** 图片地址列表 */
		private ArrayList<Uri> pictureList;
		
		@SuppressWarnings("WeakerAccess")
		public SavedState(Parcel source) {
			super(source);
			this.maxCount = source.readInt();
			this.rowCount = source.readInt();
			
			this.pictureViewId = source.readInt();
			this.pictureRatio = source.readFloat();
			this.pictureRound = source.readFloat();
			this.horizontalPadding = source.readInt();
			this.verticalPadding = source.readInt();
			
			this.supportInsert = source.readInt() == 1;
			
			this.selectedPosition = source.readInt();
			this.pictureList = source.createTypedArrayList(Uri.CREATOR);
		}
		
		@SuppressWarnings("WeakerAccess")
		public SavedState(Parcelable superState) {
			super(superState);
		}
		
		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(this.maxCount);
			out.writeInt(this.rowCount);
			
			out.writeInt(this.pictureViewId);
			out.writeFloat(this.pictureRatio);
			out.writeFloat(this.pictureRound);
			out.writeInt(this.horizontalPadding);
			out.writeInt(this.verticalPadding);
			
			out.writeInt(this.supportInsert ? 1 : 0);
			
			out.writeInt(this.selectedPosition);
			out.writeTypedList(this.pictureList);
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
	 * 图片操作监听
	 */
	public static abstract class OnPictureListener {
		/**
		 * 插入图片
		 */
		public void onInsert() {
		}
		
		/**
		 * 编辑图片
		 *
		 * @param position   图片列表位置
		 * @param pictureUri 图片地址
		 */
		public void onEdit(int position, Uri pictureUri) {
		}
		
		/**
		 * 选择图片
		 *
		 * @param position   图片列表位置
		 * @param pictureUri 图片地址
		 */
		public void onSelect(int position, Uri pictureUri) {
		}
	}
	
}
