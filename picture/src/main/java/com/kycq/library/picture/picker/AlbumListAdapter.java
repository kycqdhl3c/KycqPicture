package com.kycq.library.picture.picker;

import android.content.Context;
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

import java.util.ArrayList;

class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumHolder> {
	private LayoutInflater inflater;
	
	private ArrayList<AlbumInfo> albumInfoList;
	private OnAlbumListener onAlbumListener;
	
	private int selectedPosition = 0;
	
	AlbumListAdapter(Context context,
	                 ArrayList<AlbumInfo> albumInfoList,
	                 OnAlbumListener onAlbumListener) {
		this.inflater = LayoutInflater.from(context);
		this.albumInfoList = albumInfoList;
		this.onAlbumListener = onAlbumListener;
		
		onAlbumListener.onAlbum(albumInfoList.get(0));
	}
	
	ArrayList<AlbumInfo> getAlbumInfoList() {
		return this.albumInfoList;
	}
	
	@Override
	public int getItemCount() {
		return this.albumInfoList.size();
	}
	
	@Override
	public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new AlbumHolder(this.inflater.inflate(R.layout.kp_item_album_list, parent, false));
	}
	
	@Override
	public void onBindViewHolder(AlbumHolder holder, int position) {
		holder.setAlbumInfo(this.albumInfoList.get(position));
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
			if (selectedPosition == getAdapterPosition()) {
				this.kpSelected.setVisibility(View.VISIBLE);
			} else {
				this.kpSelected.setVisibility(View.INVISIBLE);
			}
			
			this.kpPictureView.setController(
					Fresco.newDraweeControllerBuilder()
							.setOldController(this.kpPictureView.getController())
							.setImageRequest(
									ImageRequestBuilder.newBuilderWithSource(albumInfo.pictureInfoList.get(0).pictureUri)
											.setResizeOptions(new ResizeOptions(300, 300))
											.build()
							)
							.build()
			);
		}
		
		@Override
		public void onClick(View v) {
			int position = getAdapterPosition();
			int oldSelectedPosition = selectedPosition;
			selectedPosition = position;
			notifyItemChanged(oldSelectedPosition);
			notifyItemChanged(selectedPosition);
			
			onAlbumListener.onAlbum(albumInfoList.get(selectedPosition));
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
