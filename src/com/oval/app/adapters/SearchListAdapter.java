package com.oval.app.adapters;

import java.util.List;

import org.mitre.svmp.activities.ConnectionList;
import org.mitre.svmp.client.SendNetIntent;
import org.mitre.svmp.common.AppInfo;
import org.mitre.svmp.common.Constants;
import org.mitre.svmp.common.DatabaseHandler;

import com.citicrowd.oval.R;
import com.oval.app.vo.SearchResultItemVO;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter {

	private Activity activity;
	private LayoutInflater inflater;
	private List<SearchResultItemVO> items;
	private DatabaseHandler dbHandler;

	public SearchListAdapter(Activity activity, List<SearchResultItemVO> items) {
		// TODO Auto-generated constructor stub

		this.activity = activity;
		this.items = items;
		this.dbHandler = new DatabaseHandler(activity);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (inflater == null)
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_item_search, null);

		Holder holder = new Holder();
		holder.appNameTextView = (TextView) convertView.findViewById(R.id.appNameTextView);
		holder.appCategoryTextView = (TextView) convertView.findViewById(R.id.appCategoryTextView);
		holder.appIconImageView = (ImageView) convertView.findViewById(R.id.appIconImageView);
		holder.openOrInstallBtn = (Button) convertView.findViewById(R.id.openOrInstallBtn);
		final int finalPosition = position;

		holder.openOrInstallBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (items != null) {
					SearchResultItemVO searchItem = items.get(finalPosition);
					if (searchItem != null) {
						Intent i = new Intent(activity, ConnectionList.class);
						i.setAction(Constants.ACTION_LAUNCH_APP);
						// Intent i = new Intent(OvalLoginActivity.this,
						// OvalSearchActivity.class);
						i.putExtra("connectionID", 1);

						if (dbHandler.getAppInfo(1, searchItem.getApkId()) != null) {
							i.putExtra("pkgName", searchItem.getApkId());
							activity.startActivity(i);
						} else {
							// i.putExtra("pkgName",
							// activity.getString(R.string.oval_app_services_pkgname));
							//
							// i.putExtra("apkPath",
							// activity.getString(R.string.services_prefix_url)
							// + searchItem.getApkPath());

							AppInfo appinfo = new AppInfo(1, searchItem.getApkId(), searchItem.getBasename(), false,
									null, null, 0);
							long result = dbHandler.insertAppInfo(appinfo);

							
							if (result > -1) {
								Intent intent = new Intent(activity, SendNetIntent.class);
								Uri.Builder builder = new Uri.Builder();
								builder.scheme("http").authority("oval.co.in");
								builder.appendQueryParameter("type", "downloadAndInstall");
								builder.appendQueryParameter("url",
										activity.getString(R.string.services_prefix_url) + searchItem.getApkPath());
								intent.setData(builder.build());
								activity.startActivity(intent);
							}
						}

					}
				}

			}
		});

		if (items != null) {
			SearchResultItemVO searchItem = items.get(position);

			if (searchItem != null) {
				holder.appCategoryTextView.setText(searchItem.getTypeName());
				holder.appNameTextView.setText(searchItem.getBasename());
				Picasso.with(activity)
						.load(activity.getString(R.string.services_prefix_url) + searchItem.getIconPath()
								+ searchItem.getLargeIcon())
						.placeholder(R.drawable.ic_launcher).into(holder.appIconImageView);
			}
		}
		return convertView;
	}

	class Holder {
		public ImageView appIconImageView;
		public TextView appNameTextView;
		public TextView appCategoryTextView;
		public Button openOrInstallBtn;
	}

}
