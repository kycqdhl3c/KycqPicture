package com.kycq.picture;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.kycq.picture.databinding.ItemPicturePagerBinding;

import java.util.ArrayList;

public class PicturePagerAdapter extends PagerAdapter {
	private LayoutInflater inflater;
	private DisplayMetrics displayMetrics;
	
	private ArrayList<Uri> pictureList;
	
	private View.OnClickListener viewClickListener;
	
	public PicturePagerAdapter(Context context,
	                           ArrayList<Uri> pictureList,
	                           View.OnClickListener viewClickListener) {
		this.inflater = LayoutInflater.from(context);
		this.displayMetrics = context.getResources().getDisplayMetrics();
		this.pictureList = pictureList;
		this.viewClickListener = viewClickListener;
	}
	
	@Override
	public int getCount() {
		return this.pictureList.size();
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ItemPicturePagerBinding dataBinding = DataBindingUtil.inflate(
				this.inflater,
				R.layout.item_picture_pager,
				container, false
		);
		
		dataBinding.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewClickListener.onClick(v);
			}
		});
		
		ImageRequest imageRequest =
				ImageRequestBuilder.newBuilderWithSource(this.pictureList.get(position))
						.setResizeOptions(
								new ResizeOptions(
										this.displayMetrics.widthPixels,
										this.displayMetrics.heightPixels))
						.build();
		DraweeController controller =
				Fresco.newDraweeControllerBuilder()
						.setOldController(dataBinding.pictureView.getController())
						.setImageRequest(imageRequest)
						.build();
		dataBinding.pictureView.setController(controller);
		
		container.addView(dataBinding.getRoot());
		return dataBinding.getRoot();
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
}
