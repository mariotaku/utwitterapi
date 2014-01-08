package org.mariotaku.utwitterapi.adapter;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.utwitterapi.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationsListAdapter extends BaseAdapter {

	private final PackageManager mPackageManager;
	private final Context mContext;

	private final List<ApplicationInfo> mData = new ArrayList<ApplicationInfo>();

	public ApplicationsListAdapter(final Context context) {
		this(context, context.getPackageManager());
	}

	public ApplicationsListAdapter(final Context context, final PackageManager pm) {
		mPackageManager = pm;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ApplicationInfo getItem(final int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = convertView != null ? convertView : LayoutInflater.from(mContext).inflate(
				R.layout.list_item_client, parent, false);
		final ViewHolder viewholder = view.getTag() == null ? new ViewHolder(view) : (ViewHolder) view.getTag();

		final ApplicationInfo info = getItem(position);
		viewholder.text1.setText(info.loadLabel(mPackageManager));
		viewholder.text2.setText(info.packageName);
		viewholder.icon.setImageDrawable(info.loadIcon(mPackageManager));

		return view;
	}

	public void setData(final List<ApplicationInfo> data) {
		mData.clear();
		if (data != null) {
			mData.addAll(data);
		}
		notifyDataSetChanged();
	}

	private static class ViewHolder {

		final ImageView icon;
		final TextView text1, text2;

		public ViewHolder(final View view) {

			icon = (ImageView) view.findViewById(android.R.id.icon);
			text1 = (TextView) view.findViewById(android.R.id.text1);
			text2 = (TextView) view.findViewById(android.R.id.text2);
		}
	}

}