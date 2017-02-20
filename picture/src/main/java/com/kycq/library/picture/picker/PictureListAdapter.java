package com.kycq.library.picture.picker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

import java.util.ArrayList;

class PictureListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/** 拍照 */
	private static final int CAMERA = 1;
	/** 图片 */
	private static final int PICTURE = 2;

	private LayoutInflater mInflater;
	/** 输出图片数量 */
	private int mPickerCount;
	/** 选中标签数列 */
	private SparseBooleanArray mCheckedArray = new SparseBooleanArray();

	/** 图片信息列表 */
	private ArrayList<PictureInfo> mPictureInfoList = new ArrayList<>();

	/** 图片获取监听 */
	private OnPickerListener mOnPickerListener;

	/**
	 * 构造方法
	 *
	 * @param context     设备上下文环境
	 * @param pickerCount 输出图片数量
	 */
	PictureListAdapter(Context context, int pickerCount) {
		mInflater = LayoutInflater.from(context);
		mPickerCount = pickerCount;
	}

	/**
	 * 设置图片信息列表
	 *
	 * @param pictureInfoList 图片信息列表
	 */
	void setPictureInfoList(ArrayList<PictureInfo> pictureInfoList) {
		mPictureInfoList.addAll(pictureInfoList);
		notifyDataSetChanged();
	}

	/**
	 * 添加图片信息
	 *
	 * @param pictureInfo 图片信息
	 */
	void addPictureInfo(PictureInfo pictureInfo) {
		SparseBooleanArray checkedArray = new SparseBooleanArray();
		for (int index = mCheckedArray.size() - 1; index >= 0; index--) {
			int key = mCheckedArray.keyAt(index);
			boolean value = mCheckedArray.valueAt(index);
			checkedArray.append(key + 1, value);
		}

		mPictureInfoList.add(0, pictureInfo);
		notifyItemRangeInserted(1, 1);
	}

	/**
	 * 获取选中图片信息数组
	 *
	 * @return 选中图片信息数组
	 */
	PictureInfo[] getPictureInfoArray() {
		PictureInfo[] pictureInfoArray = new PictureInfo[mCheckedArray.size()];
		for (int index = mCheckedArray.size() - 1; index >= 0; index--) {
			pictureInfoArray[index] = mPictureInfoList.get(mCheckedArray.keyAt(index));
		}
		return pictureInfoArray;
	}

	/**
	 * 选中图片
	 *
	 * @param picturePosition 图片列表位置
	 */
	void pickerPictureUri(int picturePosition) {
		PictureInfo pictureInfo = mPictureInfoList.get(picturePosition);
		if (!pictureInfo.isAvailable()) {
			Toast.makeText(mInflater.getContext(), R.string.kp_picture_error, Toast.LENGTH_SHORT).show();
			return;
		}

		if (isSingle()) {
			if (mCheckedArray.size() > 0) {
				mCheckedArray.delete(mCheckedArray.keyAt(0));
			}
			mCheckedArray.put(picturePosition, true);
		} else {
			boolean value = mCheckedArray.get(picturePosition);
			if (value) {
				mCheckedArray.delete(picturePosition);
			} else {
				if (mPickerCount == mCheckedArray.size()) {
					return;
				}
				mCheckedArray.put(picturePosition, true);
			}
			notifyItemChanged(getAdapterPosition(picturePosition));
		}

		mOnPickerListener.onPicker(pictureInfo, mCheckedArray.size(), mPickerCount);
	}

	/**
	 * 是否输出唯一图片
	 *
	 * @return true唯一
	 */
	boolean isSingle() {
		return mPickerCount == 1;
	}

	/**
	 * 获取图片列表位置
	 *
	 * @param adapterPosition 图片控件位置
	 * @return 图片列表位置
	 */
	private int getPicturePosition(int adapterPosition) {
		return adapterPosition - 1;
	}

	/**
	 * 获取图片控件位置
	 *
	 * @param picturePosition 图片列表位置
	 * @return 图片控件位置
	 */
	private int getAdapterPosition(int picturePosition) {
		return picturePosition + 1;
	}

	/**
	 * 设置图片获取监听
	 *
	 * @param listener 图片获取监听
	 */
	void setOnPickerListener(OnPickerListener listener) {
		mOnPickerListener = listener;
	}

	@Override
	public int getItemCount() {
		return mPictureInfoList != null ? mPictureInfoList.size() + 1 : 1;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? CAMERA : PICTURE;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == PICTURE) {
			return new PictureHolder(mInflater.inflate(R.layout.kp_item_picture_list, parent, false));
		} else if (viewType == CAMERA) {
			return new CameraHolder(mInflater.inflate(R.layout.kp_item_camera_list, parent, false));
		}
		return null;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, int adapterPosition) {
		int viewType = holder.getItemViewType();
		if (viewType == PICTURE) {
			PictureHolder pictureHolder = (PictureHolder) holder;

			int picturePosition = getPicturePosition(adapterPosition);

			pictureHolder.setPictureInfo(mPictureInfoList.get(picturePosition));

			if (isSingle()) {
				pictureHolder.ivChecked.setVisibility(View.INVISIBLE);
			} else {
				boolean value = mCheckedArray.get(picturePosition);
				if (value) {
					pictureHolder.ivChecked.setVisibility(View.VISIBLE);
				} else {
					pictureHolder.ivChecked.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	/**
	 * 图片控件展示信息
	 */
	private class PictureHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		/** 图片展示控件 */
		private SimpleDraweeView pictureView;
		/** 图片选中控件 */
		private ImageView ivChecked;

		/** 图片信息 */
		private PictureInfo mPictureInfo;

		PictureHolder(View itemView) {
			super(itemView);

			itemView.setOnClickListener(this);
			pictureView = (SimpleDraweeView) itemView.findViewById(R.id.kpPictureView);
			ivChecked = (ImageView) itemView.findViewById(R.id.kpIVChecked);

			// 设置图片展示控件属性
			pictureView.setAspectRatio(1.0f);
			pictureView.getHierarchy()
					.setPlaceholderImage(
							R.drawable.kp_ic_picture_loading,
							ScalingUtils.ScaleType.FIT_XY
					);
			pictureView.getHierarchy()
					.setFailureImage(
							R.drawable.kp_ic_picture_error,
							ScalingUtils.ScaleType.FIT_XY
					);
		}

		/**
		 * 设置图片信息
		 *
		 * @param pictureInfo 图片信息
		 */
		void setPictureInfo(PictureInfo pictureInfo) {
			if (pictureInfo.equals(mPictureInfo)) {
				return;
			}
			mPictureInfo = pictureInfo;

			pictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(
									pictureView.getController()
							)
							.setImageRequest(
									ImageRequestBuilder.newBuilderWithSource(pictureInfo.getPictureUri())
											.setResizeOptions(new ResizeOptions(300, 300))
											.build()
							)
							.build()
			);
		}

		@Override
		public void onClick(View view) {
			pickerPictureUri(getPicturePosition(getAdapterPosition()));
		}

	}

	/**
	 * 拍照控件展示信息
	 */
	private class CameraHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private SimpleDraweeView ivCamera;

		CameraHolder(View itemView) {
			super(itemView);

			itemView.setOnClickListener(this);
			ivCamera = (SimpleDraweeView) itemView.findViewById(R.id.kpIVCamera);

			// 设置拍照展示控件属性
			ivCamera.setAspectRatio(1.0f);
			ivCamera.getHierarchy()
					.setPlaceholderImage(
							R.drawable.kp_ic_picture_camera,
							ScalingUtils.ScaleType.CENTER_INSIDE
					);
		}

		@Override
		public void onClick(View v) {
			mOnPickerListener.onCamera();
		}
	}

	/**
	 * 图片获取监听
	 */
	interface OnPickerListener {
		/**
		 * 拍照
		 */
		void onCamera();

		/**
		 * 选择
		 *
		 * @param pictureInfo  图片信息
		 * @param currentCount 当前图片数量
		 * @param pickerCount  输出图片数量
		 */
		void onPicker(PictureInfo pictureInfo, int currentCount, int pickerCount);
	}
}
