package org.mariotaku.utwitterapi.activity;

import java.io.IOException;

import org.mariotaku.utwitterapi.R;
import org.mariotaku.utwitterapi.fragment.ProgressDialogFragment;
import org.mariotaku.utwitterapi.util.OAuthPasswordAuthenticator;
import org.mariotaku.utwitterapi.util.OAuthPasswordAuthenticator.SignInResult;
import org.mariotaku.utwitterapi.util.Utils;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BrowserSigninAgentActivity extends Activity implements OnClickListener, TextWatcher {

	private EditText mEditUsername;
	private EditText mEditPassword;

	private Button mOpenInBrowserButton;
	private Button mSignInButton;
	private SignInTask mTask;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.open_in_browser: {
				final Intent intent = getIntent();
				final Uri data = intent.getData();
				if (data == null) return;
				final Intent viewIntent = new Intent(Intent.ACTION_VIEW, data);
				final Intent chooserIntent = Utils.createExcludingChooserIntent(this, getPackageName(), viewIntent);
				if (chooserIntent != null) {
					startActivity(chooserIntent);
				}
				break;
			}
			case R.id.sign_in: {
				final Intent intent = getIntent();
				final Uri data = intent.getData();
				if (data == null) return;
				final String username = String.valueOf(mEditUsername.getText());
				final String password = String.valueOf(mEditPassword.getText());
				mTask = new SignInTask(this, data.toString(), username, password);
				mTask.execute();
				break;
			}
		}

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mEditUsername = (EditText) findViewById(R.id.edit_username);
		mEditPassword = (EditText) findViewById(R.id.edit_password);
		mOpenInBrowserButton = (Button) findViewById(R.id.open_in_browser);
		mSignInButton = (Button) findViewById(R.id.sign_in);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		updateSignInButton();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_sign_in);
		mEditUsername.addTextChangedListener(this);
		mEditPassword.addTextChangedListener(this);
		mOpenInBrowserButton.setOnClickListener(this);
		mSignInButton.setOnClickListener(this);
		updateSignInButton();
		Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show();
	}

	private void updateSignInButton() {
		mSignInButton.setEnabled(mEditUsername.length() > 0 && mEditPassword.length() > 0);
	}

	private static class SignInTask extends AsyncTask<Void, Void, TaskResult<SignInResult>> {

		private static final String DIALOG_FRAGMENT_TAG = "sign_in_progress";

		private final BrowserSigninAgentActivity mActivity;
		private final OAuthPasswordAuthenticator mAuthenticator;
		private final String mOAuthOrizationUrlString;
		private final String mUsername;
		private final String mPassword;

		SignInTask(final BrowserSigninAgentActivity activity, final String oauthOrizationUrlString,
				final String username, final String password) {
			mActivity = activity;
			mAuthenticator = new OAuthPasswordAuthenticator(activity);
			mOAuthOrizationUrlString = oauthOrizationUrlString;
			mUsername = username;
			mPassword = password;
		}

		@Override
		protected TaskResult<SignInResult> doInBackground(final Void... params) {
			try {
				return TaskResult.data(mAuthenticator.getSignInResult(mOAuthOrizationUrlString, mUsername, mPassword));
			} catch (final IOException e) {
				return TaskResult.exception(e);
			}
		}

		@Override
		protected void onPostExecute(final TaskResult<SignInResult> result) {
			final Fragment f = mActivity.getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
			if (f instanceof DialogFragment) {
				((DialogFragment) f).dismiss();
			}
			if (result.data != null) {
				final SignInResult signInResult = result.data;
				if (signInResult.isCallbackUrl()) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(signInResult.getCallbackUrl()));
					mActivity.startActivity(intent);
					mActivity.setResult(RESULT_OK);
					mActivity.finish();
				} else if (signInResult.isPinCode()) {
					final Intent intent = new Intent(mActivity, PinCodeActivity.class);
					intent.putExtra(Intent.EXTRA_TEXT, signInResult.getPinCode());
					mActivity.startActivity(intent);
					mActivity.setResult(RESULT_OK);
					mActivity.finish();
				}
			} else if (result.exception != null) {
				result.exception.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			final ProgressDialogFragment df = ProgressDialogFragment.show(mActivity, DIALOG_FRAGMENT_TAG);
			df.setCancelable(false);
		}

	}

	private static class TaskResult<T> {
		public final T data;
		public final Exception exception;

		TaskResult(final T data, final Exception exception) {
			this.data = data;
			this.exception = exception;
		}

		public static <T> TaskResult<T> data(final T data) {
			return new TaskResult<T>(data, null);
		}

		public static <T> TaskResult<T> exception(final Exception e) {
			return new TaskResult<T>(null, e);
		}
	}

}
