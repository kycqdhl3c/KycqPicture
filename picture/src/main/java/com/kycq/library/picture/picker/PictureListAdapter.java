package com.kycq.library.picture.picker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

class PictureListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/** 拍照 */
	private static final int CAMERA = 0;
	/** 图片 */
	private static final int PICTURE = 1;
	
	private LayoutInflater inflater;
	
	private int cameraListLayoutId;
	private int pictureListLayoutId;
	
	private boolean isSingle;
	private AlbumInfo albumInfo;
	
	private OnPictureListener onPictureListener;
	
	PictureListAdapter(Context context,
	                   int cameraListLayoutId, int pictureListLayoutId,
	                   boolean isSingle,
	                   AlbumInfo albumInfo,
	                   OnPictureListener onPictureListener) {
		this.inflater = LayoutInflater.from(context);
		this.cameraListLayoutId = cameraListLayoutId;
		this.pictureListLayoutId = pictureListLayoutId;
		this.isSingle = isSingle;
		this.albumInfo = albumInfo;
		this.onPictureListener = onPictureListener;
	}
	
	/**
	 * 选中图片
	 *
	 * @param pictureInfo 图片信息
	 */
	void notifyPickPicture(PictureInfo pictureInfo) {
		if (this.onPictureListener.onPick(pictureInfo)
				&& this.albumInfo.pictureInfoList.contains(pictureInfo)) {
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
		
		if (onPictureListener.onPick(pictureInfo)) {
			notifyItemChanged(adapterPosition);
		}
	}
	
	/**
	 * 拍照
	 */
	private void pickCamera() {
		onPictureListener.onCamera();
	}
	
	/**
	 * 预览图片
	 *
	 * @param adapterPosition 图片列表位置
	 */
	private void pickPreview(int adapterPosition) {
		if (this.albumInfo.isFullAlbum()) {
			adapterPosition--;
		}
		if (isSingle) {
			onPictureListener.onPick(this.albumInfo.pictureInfoList.get(adapterPosition));
		} else {
			onPictureListener.onPreview(this.albumInfo, adapterPosition);
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
	public int getItemViewType(int position) {
		if (this.albumInfo.isFullAlbum() && position == 0) {
			return CAMERA;
		}
		return PICTURE;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == CAMERA) {
			return new CameraHolder(this.inflater.inflate(this.cameraListLayoutId, parent, false));
		}
		return new PictureHolder(this.inflater.inflate(this.pictureListLayoutId, parent, false));
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int viewType = holder.getItemViewType();
		if (viewType == PICTURE) {
			PictureHolder pictureHolder = (PictureHolder) holder;
			pictureHolder.setPictureInfo(getItem(position));
		}
	}
	
	private class PictureHolder extends RecyclerView.ViewHolder {
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
		
		void setPictureInfo(PictureInfo pictureInfo) {
			this.kpLayer.setVisibility(View.VISIBLE);
			this.kpLayer.setSelected(pictureInfo.selected);
			this.kpSelected.setVisibility(isSingle ? View.GONE : View.VISIBLE);
			this.kpSelected.setSelected(pictureInfo.selected);
			
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
	
	private class CameraHolder extends RecyclerView.ViewHolder {
		
		CameraHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickCamera();
				}
			});
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
		boolean onPick(PictureInfo pictureInfo);
		
		/**
		 * 预览
		 *
		 * @param albumInfo 相册信息
		 * @param position  图片列表位置
		 */
		void onPreview(AlbumInfo albumInfo, int position);
	}
}
