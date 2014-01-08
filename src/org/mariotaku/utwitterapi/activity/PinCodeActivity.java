package org.mariotaku.utwitterapi.activity;

import org.mariotaku.utwitterapi.R;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PinCodeActivity extends Activity implements OnClickListener {

	private TextView mPinCodeView;
	private Button mCopyButton;
	private Button mOKButton;

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.copy: {
				final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				cm.setPrimaryClip(ClipData.newPlainText(getString(R.string.pin_code), mPinCodeView.getText()));
				Toast.makeText(this, R.string.pin_code_copied, Toast.LENGTH_SHORT).show();
				break;
			}
			case R.id.ok: {
				setResult(RESULT_OK);
				finish();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mPinCodeView = (TextView) findViewById(R.id.pin_code);
		mCopyButton = (Button) findViewById(R.id.copy);
		mOKButton = (Button) findViewById(R.id.ok);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final CharSequence pinCode = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (TextUtils.isEmpty(pinCode)) {
			finish();
			return;
		}
		setContentView(R.layout.activity_pin_code);
		mPinCodeView.setText(pinCode);
		mCopyButton.setOnClickListener(this);
		mOKButton.setOnClickListener(this);
	}

}
