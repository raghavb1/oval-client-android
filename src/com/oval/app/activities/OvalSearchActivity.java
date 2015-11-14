package com.oval.app.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.mitre.svmp.activities.SvmpActivity;
import com.citicrowd.oval.R;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oval.app.adapters.SearchListAdapter;
import com.oval.app.network.CustomSSLSocketFactory;
import com.oval.app.network.HTTPServiceHandler;
import com.oval.app.vo.LoginResultVO;
import com.oval.app.vo.RawSearchResultVO;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class OvalSearchActivity extends SvmpActivity implements OnKeyListener {

	EditText appSearchEditText;
	ProgressDialog pDialog;
	ListView searchResultListView;
	Button tempSearchBtn;

	Gson gson = new Gson();
	RawSearchResultVO rawSearchResultVO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.activity_search);

		appSearchEditText = (EditText) findViewById(R.id.appSearchEditText);
		appSearchEditText.setOnKeyListener(this);
		searchResultListView=(ListView) findViewById(R.id.searchResultListView);
		tempSearchBtn=(Button) findViewById(R.id.tempSearchBtn);
		tempSearchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String searchStr = appSearchEditText.getText().toString();
				if (!searchStr.isEmpty()) {
					makeSearch(searchStr);
				}
				
			}
		});

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode == EditorInfo.IME_ACTION_DONE
				|| event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

			if (!event.isShiftPressed()) {
				Log.v("AndroidEnterKeyActivity", "Enter Key Pressed!");
				switch (v.getId()) {
				case R.id.appSearchEditText:
					String searchStr = appSearchEditText.getText().toString();
					if (!searchStr.isEmpty()) {
						makeSearch(searchStr);
					}
					break;

				}
				return true;
			}

		}

		return false;
	}

	private void makeSearch(String searchStr) {
		// TODO Auto-generated method stub

		new SearchAsyncTask().execute(searchStr);

	}

	private class SearchAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();
			pDialog = new ProgressDialog(OvalSearchActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {

			HTTPServiceHandler httpServiceHandler = new HTTPServiceHandler(getApplicationContext());

			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("q", params[0]));

			String jsonStr = httpServiceHandler.makeSecureServiceCall(OvalLoginActivity.SEARCH_URL,
					HTTPServiceHandler.GET, nameValuePair);

			Log.d("Response: ", "> " + jsonStr);
			return jsonStr;

		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			super.onPostExecute(result);

			Type type = new TypeToken<RawSearchResultVO>() {
			}.getType();
			rawSearchResultVO = gson.fromJson(result, type);

			pDialog.dismiss();
			updateUI();

		}

	}

	public void updateUI() {
		// TODO Auto-generated method stub
		if (rawSearchResultVO != null) {
			if (rawSearchResultVO.getData() != null) {
				SearchListAdapter adapter = new SearchListAdapter(this, rawSearchResultVO.getData().getSearchList());
				searchResultListView.setAdapter(adapter);
			}
		}
		
		
		
	}

}
