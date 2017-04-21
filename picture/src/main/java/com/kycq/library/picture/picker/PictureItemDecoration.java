package com.kycq.library.picture.picker;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class PictureItemDecoration extends RecyclerView.ItemDecoration {
	private int verticalSpace;
	private int horizontalSpace;
	
	PictureItemDecoration(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		this.verticalSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, displayMetrics);
		this.horizontalSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics);
	}
	
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
		int spanCount = manager.getSpanCount();
		int position = parent.getChildAdapterPosition(view);
		if (position < spanCount) {
			outRect.top = this.verticalSpace;
		}
		outRect.left = this.horizontalSpace;
		outRect.right = this.horizontalSpace;
		outRect.bottom = this.verticalSpace;
	}
}
