package org.mariotaku.utwitterapi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class ActivatedCheckBox extends CheckBox {

	public ActivatedCheckBox(final Context context) {
		super(context);
	}

	public ActivatedCheckBox(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ActivatedCheckBox(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isActivated() {
		return super.isActivated();
	}

	@Override
	public void setActivated(final boolean activated) {
		super.setActivated(activated);
		setChecked(activated);
	}

}
