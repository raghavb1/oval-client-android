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
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.quinny898.library.persistentsearch.SearchBox.MenuListener;
import com.quinny898.library.persistentsearch.SearchBox.SearchListener;
import com.quinny898.library.persistentsearch.SearchBox.VoiceRecognitionListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class OvalSearchActivity extends SvmpActivity {

	EditText appSearchEditText;
	ProgressDialog pDialog;
	ListView searchResultListView;
	Button tempSearchBtn;

	Gson gson = new Gson();
	RawSearchResultVO rawSearchResultVO;

	Boolean isSearch;
	private SearchBox search;
	
	
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState, R.layout.activity_search);

		search = (SearchBox) findViewById(R.id.searchbox);
		search.setLogoText("OVAL");
		search.enableVoiceRecognition(this);
		searchResultListView = (ListView) findViewById(R.id.searchResultListView);

		ArrayList<String> searchHistory = (ArrayList<String>) dbHandler.getAllSearchHistory();

		for (String searchString : searchHistory) {

			SearchResult option = new SearchResult(searchString, getResources().getDrawable(R.drawable.ic_history));
			search.addSearchable(option);

		}
		
	search.setVoiceRecognitionListener(new VoiceRecognitionListener() {
			
			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				
			
				 
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(search.getWindowToken(),
						0);
				
				
				search.micClick();
				
			}
		});
		
		
	
		
		
		/*
		 * for(int x = 0; x < 10; x++){ SearchResult option = new SearchResult(
		 * "Result " + Integer.toString(x),
		 * getResources().getDrawable(R.drawable.ic_history));
		 * search.addSearchable(option); }
		 */
		
		
		search.setMenuListener(new MenuListener() {

			@Override
			public void onMenuClick() {
				// Hamburger has been clicked
				// Toast.makeText(OvalSearchActivity.this, "Menu click",
				// Toast.LENGTH_LONG).show();
			}

		});

		search.setSearchListener(new SearchListener() {

			@Override
			public void onSearchOpened() {
				// Use this to tint the screen
			}

			@Override
			public void onSearchClosed() {
				// Use this to un-tint the screen

				// Toast.makeText(OvalSearchActivity.this, "Searched Closed",
				// Toast.LENGTH_LONG).show();
			}

			@Override
			public void onSearchTermChanged(String term) {
				// React to the search term changing
				// Called after it has updated results
			}

			@Override
			public void onSearch(String searchTerm) {
				// Toast.makeText(OvalSearchActivity.this, searchTerm +"
				// Searched", Toast.LENGTH_LONG).show();

				if (!searchTerm.isEmpty()) {
					dbHandler.insertSearchHistory(searchTerm);
					makeSearch(searchTerm);
				}

			}

			@Override
			public void onResultClick(SearchResult result) {
				// React to a result being clicked

				makeSearch(result.title);
			}

			@Override
			public void onSearchCleared() {
				// Called when the clear button is clicked

				// Toast.makeText(OvalSearchActivity.this, "Searched Closed",
				// Toast.LENGTH_LONG).show();
			}

		});

		/*
		 * search.setOverflowMenu(R.menu.overflow_menu);
		 * search.setOverflowMenuItemClickListener(new
		 * PopupMenu.OnMenuItemClickListener() {
		 * 
		 * @Override public boolean onMenuItemClick(MenuItem item) { switch
		 * (item.getItemId()) { case R.id.test_menu_item:
		 * Toast.makeText(OvalSearchActivity.this, "Clicked!",
		 * Toast.LENGTH_SHORT).show(); return true; } return false; } });
		 */

		/*
		 * appSearchEditText = (EditText) findViewById(R.id.appSearchEditText);
		 * appSearchEditText.setOnKeyListener(this);
		 */
		
		/*
		 * tempSearchBtn=(Button) findViewById(R.id.tempSearchBtn);
		 * tempSearchBtn.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub
		 * 
		 * String searchStr = appSearchEditText.getText().toString(); if
		 * (!searchStr.isEmpty()) {
		 * 
		 * 
		 * InputMethodManager imm =
		 * (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.hideSoftInputFromWindow(appSearchEditText.getWindowToken(),
		 * InputMethodManager.RESULT_UNCHANGED_SHOWN); makeSearch(searchStr); }
		 * 
		 * } });
		 */

	}

	/*
	 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) { //
	 * TODO Auto-generated method stub
	 * 
	 * if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode ==
	 * EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN
	 * && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
	 * 
	 * if (!event.isShiftPressed()) { Log.v("AndroidEnterKeyActivity",
	 * "Enter Key Pressed!"); switch (v.getId()) { case R.id.appSearchEditText:
	 * String searchStr = appSearchEditText.getText().toString(); if
	 * (!searchStr.isEmpty()) { makeSearch(searchStr); } break;
	 * 
	 * } return true; }
	 * 
	 * }
	 * 
	 * return false; }
	 */

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1234 && resultCode == RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		//	search.populateEditText(matches.get(0));
			search.setLogoText(matches.get(0));
			
			if(search.isSearchOpen())
			{
				search.toggleSearch();
			}
			
			
			/*if (search.isFocused()) {
				search.clearFocus();
			}*/
			/*
			 * InputMethodManager imm = (InputMethodManager)
			 * getSystemService(Context.INPUT_METHOD_SERVICE);
			 * imm.hideSoftInputFromWindow(search.getWindowToken(),
			 * InputMethodManager.RESULT_UNCHANGED_SHOWN);
			 */

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * public void reveal(View v){ startActivity(new Intent(this,
	 * RevealActivity.class)); }
	 */

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
