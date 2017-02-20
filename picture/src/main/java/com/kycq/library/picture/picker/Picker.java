package com.kycq.library.picture.picker;

public class Picker {
	/** 选择数量 */
	public static final String PICKER_COUNT = "pickerCount";

	/** 是否裁剪(当选择数量为1时有效) */
	public static final String PICKER_CROP = "pickerCrop";

	/** 裁剪X轴比例(当进行裁剪时有效) */
	public static final String PICKER_ASPECT_X = "pickerAspectX";
	/** 裁剪Y轴比例(当进行裁剪时有效) */
	public static final String PICKER_ASPECT_Y = "pickerAspectY";

	/** 输出图片最大宽度 */
	public static final String PICKER_MAX_WIDTH = "pickerMaxWidth";
	/** 输出图片最大高度 */
	public static final String PICKER_MAX_HEIGHT = "pickerMaxHeight";

	/** 输出图片列表 */
	public static final String PICKER_LIST = "pickerList";

	private Picker() {
		throw new AssertionError("No instances!");
	}

}
