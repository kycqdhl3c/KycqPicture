package com.kycq.library.picture.picker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.PictureHolder> {
	private LayoutInflater inflater;
	
	private AlbumInfo albumInfo;
	private OnPictureListener onPictureListener;
	
	PictureListAdapter(Context context,
	                   AlbumInfo albumInfo,
	                   OnPictureListener onPictureListener) {
		this.inflater = LayoutInflater.from(context);
		this.albumInfo = albumInfo;
		this.onPictureListener = onPictureListener;
	}
	
	/**
	 * 选中图片
	 *
	 * @param pictureInfo 图片信息
	 */
	void notifyPickPicture(PictureInfo pictureInfo) {
		if (this.onPictureListener.onPicture(pictureInfo)) {
			notifyItemInserted(1);
		}
	}
	
	/**
	 * 选中图片
	 *
	 * @param adapterPosition 图片列表位置
	 */
	private void pickPicture(int adapterPosition) {
		PictureInfo pictureInfo = getItem(adapterPosition);
		if (!pictureInfo.isAvailable()) {
			Toast.makeText(inflater.getContext(), R.string.kp_picture_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (onPictureListener.onPicture(pictureInfo)) {
			notifyItemChanged(adapterPosition);
		}
	}
	
	/**
	 * 预览图片
	 *
	 * @param adapterPosition 图片列表位置
	 */
	private void pickPreview(int adapterPosition) {
		if (this.albumInfo.isFullAlbum()) {
			if (adapterPosition == 0) {
				onPictureListener.onCamera();
			} else {
				onPictureListener.onPreview(adapterPosition - 1);
			}
		} else {
			onPictureListener.onPreview(adapterPosition);
		}
	}
	
	private PictureInfo getItem(int position) {
		if (this.albumInfo.isFullAlbum()) {
			return this.albumInfo.pictureInfoList.get(position - 1);
		}
		return this.albumInfo.pictureInfoList.get(position);
	}
	
	@Override
	public int getItemCount() {
		int count = this.albumInfo.pictureInfoList.size();
		if (this.albumInfo.isFullAlbum()) {
			count++;
		}
		return count;
	}
	
	@Override
	public PictureHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new PictureHolder(this.inflater.inflate(R.layout.kp_item_picture_list, parent, false));
	}
	
	@Override
	public void onBindViewHolder(PictureHolder holder, int position) {
		if (this.albumInfo.isFullAlbum() && position == 0) {
			holder.setCameraInfo();
		} else {
			holder.setPictureInfo(getItem(position));
		}
	}
	
	class PictureHolder extends RecyclerView.ViewHolder {
		private SimpleDraweeView kpPictureView;
		private View kpLayer;
		private ImageView kpSelected;
		
		PictureHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickPreview(getAdapterPosition());
				}
			});
			this.kpPictureView = (SimpleDraweeView) itemView.findViewById(R.id.kpPictureView);
			this.kpLayer = itemView.findViewById(R.id.kpLayer);
			this.kpSelected = (ImageView) itemView.findViewById(R.id.kpSelected);
			this.kpSelected.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickPicture(getAdapterPosition());
				}
			});
		}
		
		void setCameraInfo() {
			this.itemView.setBackgroundColor(0xFF000000);
			this.kpPictureView.setAspectRatio(1.0f);
			this.kpPictureView.getHierarchy()
					.setPlaceholderImage(
							R.drawable.kp_ic_camera,
							ScalingUtils.ScaleType.CENTER_INSIDE
					);
			this.kpLayer.setVisibility(View.GONE);
			this.kpSelected.setVisibility(View.GONE);
		}
		
		void setPictureInfo(PictureInfo pictureInfo) {
			this.itemView.setBackgroundColor(0x00000000);
			this.kpPictureView.setAspectRatio(1.0f);
			this.kpPictureView.getHierarchy()
					.setPlaceholderImage(
							R.drawable.kp_ic_picture_loading,
							ScalingUtils.ScaleType.FIT_XY
					);
			this.kpPictureView.getHierarchy()
					.setFailureImage(
							R.drawable.kp_ic_picture_error,
							ScalingUtils.ScaleType.FIT_XY
					);
			
			this.kpLayer.setVisibility(View.VISIBLE);
			this.kpSelected.setVisibility(View.VISIBLE);
			if (pictureInfo.selected) {
				this.kpLayer.setBackgroundResource(R.color.kpPictureSelectedColor);
				this.kpSelected.setImageResource(R.drawable.kp_ic_picture_check_selected);
			} else {
				this.kpLayer.setBackgroundResource(R.color.kpPictureUnSelectedColor);
				this.kpSelected.setImageResource(R.drawable.kp_ic_picture_check_unselected);
			}
			
			this.kpPictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(this.kpPictureView.getController())
							.setImageRequest(
									ImageRequestBuilder
											.newBuilderWithSource(pictureInfo.pictureUri)
											.setResizeOptions(new ResizeOptions(300, 300))
											.build()
							)
							.build()
			);
		}
	}
	
	/**
	 * 图片获取监听
	 */
	interface OnPictureListener {
		/**
		 * 拍照
		 */
		void onCamera();
		
		/**
		 * 选择图片
		 *
		 * @param pictureInfo 图片信息
		 * @return true:有效操作 false:无效操作
		 */
		boolean onPicture(PictureInfo pictureInfo);
		
		/**
		 * 预览
		 *
		 * @param position 图片列表位置
		 */
		void onPreview(int position);
	}
}
