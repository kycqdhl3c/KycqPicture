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
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.library.picture.R;

class AlbumListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/** 所有图片相册 */
	private static final int ALBUM_ALL = 0;
	/** 图片相册 */
	private static final int ALBUM = 1;
	
	private LayoutInflater inflater;
	
	private int albumAllListLayoutId;
	private int albumListLayoutId;
	
	private KPPicker kpPicker;
	private OnAlbumListener onAlbumListener;
	
	private int selectedPosition = 0;
	
	AlbumListAdapter(Context context,
	                 int albumAllListLayoutId, int albumListLayoutId,
	                 KPPicker kpPicker,
	                 OnAlbumListener onAlbumListener) {
		this.inflater = LayoutInflater.from(context);
		this.albumAllListLayoutId = albumAllListLayoutId;
		this.albumListLayoutId = albumListLayoutId;
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
	public int getItemViewType(int position) {
		if (position == 0) {
			return ALBUM_ALL;
		}
		return ALBUM;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == ALBUM_ALL) {
			return new AlbumAllHolder(this.inflater.inflate(this.albumAllListLayoutId, parent, false));
		}
		return new AlbumHolder(this.inflater.inflate(this.albumListLayoutId, parent, false));
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int viewType = holder.getItemViewType();
		if (viewType == ALBUM) {
			AlbumHolder albumHolder = (AlbumHolder) holder;
			albumHolder.setAlbumInfo(getItem(position));
		} else if (viewType == ALBUM_ALL) {
			AlbumAllHolder albumAllHolder = (AlbumAllHolder) holder;
			albumAllHolder.setAlbumInfo(getItem(position));
		}
	}
	
	private class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
	
	private class AlbumAllHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView kpTitle;
		private ImageView kpSelected;
		
		AlbumAllHolder(View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(this);
			this.kpTitle = (TextView) itemView.findViewById(R.id.kpTitle);
			this.kpSelected = (ImageView) itemView.findViewById(R.id.kpSelected);
		}
		
		void setAlbumInfo(AlbumInfo albumInfo) {
			this.kpTitle.setText(albumInfo.albumName);
			this.kpSelected.setSelected(selectedPosition == getAdapterPosition());
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
