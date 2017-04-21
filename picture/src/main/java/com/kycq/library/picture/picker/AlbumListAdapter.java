package com.kycq.library.picture.picker;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumHolder> {
	private LayoutInflater inflater;
	
	private KPPicker kpPicker;
	private OnAlbumListener onAlbumListener;
	
	private int selectedPosition = 0;
	
	AlbumListAdapter(Context context,
	                 KPPicker kpPicker,
	                 OnAlbumListener onAlbumListener) {
		this.inflater = LayoutInflater.from(context);
		this.kpPicker = kpPicker;
		this.onAlbumListener = onAlbumListener;
	}
	
	void setSelectedPosition(int selectedPosition) {
		int oldSelectedPosition = this.selectedPosition;
		this.selectedPosition = selectedPosition;
		notifyItemChanged(oldSelectedPosition);
		notifyItemChanged(selectedPosition);
		
		onAlbumListener.onAlbum(getItem(selectedPosition));
	}
	
	void setSelectedAlbumInfo(AlbumInfo albumInfo) {
		if (albumInfo.equals(this.kpPicker.fullAlbumInfo)) {
			setSelectedPosition(0);
		} else if (albumInfo.equals(this.kpPicker.cacheAlbumInfo)
				&& this.kpPicker.cacheAlbumInfo.size() > 0) {
			setSelectedPosition(1);
		} else {
			int index = this.kpPicker.albumInfoList.indexOf(albumInfo);
			if (index < 0) {
				setSelectedPosition(0);
			} else if (this.kpPicker.cacheAlbumInfo.size() > 0) {
				setSelectedPosition(index + 2);
			} else {
				setSelectedPosition(index + 1);
			}
		}
	}
	
	private AlbumInfo getItem(int position) {
		if (position == 0) {
			return this.kpPicker.fullAlbumInfo;
		}
		position--;
		boolean hasCache = this.kpPicker.cacheAlbumInfo.size() > 0;
		if (position == 0 && hasCache) {
			return this.kpPicker.cacheAlbumInfo;
		}
		if (hasCache) {
			position--;
		}
		return this.kpPicker.albumInfoList.get(position);
	}
	
	@Override
	public int getItemCount() {
		if (this.kpPicker.cacheAlbumInfo.size() > 0) {
			return this.kpPicker.albumInfoList.size() + 2;
		}
		return this.kpPicker.albumInfoList.size() + 1;
	}
	
	@Override
	public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new AlbumHolder(this.inflater.inflate(R.layout.kp_item_album_list, parent, false));
	}
	
	@Override
	public void onBindViewHolder(AlbumHolder holder, int position) {
		holder.setAlbumInfo(getItem(position));
	}
	
	class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private SimpleDraweeView kpPictureView;
		private TextView kpTitle;
		private TextView kpContent;
		private ImageView kpSelected;
		
		AlbumHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(this);
			this.kpPictureView = (SimpleDraweeView) itemView.findViewById(R.id.kpPictureView);
			this.kpTitle = (TextView) itemView.findViewById(R.id.kpTitle);
			this.kpContent = (TextView) itemView.findViewById(R.id.kpContent);
			this.kpSelected = (ImageView) itemView.findViewById(R.id.kpSelected);
		}
		
		/**
		 * 设置相册信息
		 *
		 * @param albumInfo 相册信息
		 */
		void setAlbumInfo(AlbumInfo albumInfo) {
			// 设置图片展示控件属性
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
			
			this.kpTitle.setText(albumInfo.albumName);
			this.kpContent.setText(inflater.getContext().getString(
					R.string.kp_format_pieces_of_picture, albumInfo.pictureInfoList.size()));
			this.kpSelected.setSelected(selectedPosition == getAdapterPosition());
			
			Uri pictureUri = albumInfo.pictureInfoList.size() > 0
					? albumInfo.pictureInfoList.get(0).pictureUri : Uri.EMPTY;
			this.kpPictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(this.kpPictureView.getController())
							.setImageRequest(
									ImageRequestBuilder.newBuilderWithSource(pictureUri)
											.setResizeOptions(new ResizeOptions(300, 300))
											.build()
							)
							.build()
			);
		}
		
		@Override
		public void onClick(View v) {
			setSelectedPosition(getAdapterPosition());
		}
	}
	
	/**
	 * 相册选择监听
	 */
	interface OnAlbumListener {
		/**
		 * 选择相册
		 *
		 * @param albumInfo 相册信息
		 */
		void onAlbum(AlbumInfo albumInfo);
	}
}
