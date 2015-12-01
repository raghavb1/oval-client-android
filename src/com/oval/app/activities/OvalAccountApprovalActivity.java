package com.oval.app.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.mitre.svmp.activities.AppList;
import org.mitre.svmp.activities.AppRTCRefreshAppsActivity;
import org.mitre.svmp.activities.ConnectionList;
import org.mitre.svmp.activities.SvmpActivity;
import com.citicrowd.oval.R;
import org.mitre.svmp.common.ConnectionInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.oval.app.network.CustomSSLSocketFactory;
import com.oval.app.vo.LoginRequestVO;
import com.oval.app.vo.LoginResultVO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class OvalAccountApprovalActivity extends SvmpActivity implements OnClickListener {

	Button checkStatusBtn;
	ConnectionInfo connectionInfo;
	ProgressDialog pDialog;
	LoginRequestVO loginRequestVo = new LoginRequestVO();
	Gson gson = new Gson();
	LoginResultVO loginResultVo = new LoginResultVO();
	private int sendRequestCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.activity_approval);

		checkStatusBtn = (Button) findViewById(R.id.checkStatusBtn);
		checkStatusBtn.setOnClickListener(this);
		Intent intent = getIntent();
		if (intent.hasExtra("connectionID")) {

			// add entry in database with connection ID and pass it here
			int id = intent.getIntExtra("connectionID", 0);
			// ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(id);
			connectionInfo = dbHandler.getConnectionInfoList().get(id);
			// connectionInfo.setStatus(status);
		}

		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.checkStatusBtn:
			if (connectionInfo != null)
				doLogin();

			break;

		default:
			break;
		}

	}

	private void doLogin() {
		loginRequestVo.setUsername(connectionInfo.getUsername());
		loginRequestVo.setPassword("Hash33##");

		new LoginAsyncTask().execute(getString(R.string.api_end_point) + OvalLoginActivity.LOGIN_URL,
				gson.toJson(loginRequestVo));
	}

	private class LoginAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();

			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			String response = null;

			try {

				SSLSocketFactory sslFactory = new CustomSSLSocketFactory(
						CustomSSLSocketFactory.getKeyStoreForCertificate(
								getApplicationContext().getResources().getAssets().open("new_server.crt")));
								// sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
								// to be used in case of bypassing certificates

				// Enable HTTP parameters */
				HttpParams params4 = new BasicHttpParams();
				HttpProtocolParams.setVersion(params4, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params4, HTTP.UTF_8);

				// Register the HTTP and HTTPS Protocols. For HTTPS, register
				// our custom SSL Factory object.
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				registry.register(new Scheme("https", sslFactory, 443));

				// Create a new connection manager using the newly created
				// registry and then create a new HTTP client
				// using this connection manager
				ClientConnectionManager ccm = new ThreadSafeClientConnManager(params4, registry);
				DefaultHttpClient client = new DefaultHttpClient(ccm, params4);
				HttpPost httppostreq = new HttpPost(params[0]);
				StringEntity se = new StringEntity(params[1]);
				se.setContentType("application/json;charset=UTF-8");
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
				httppostreq.setEntity(se);
				HttpResponse httpresponse = client.execute(httppostreq);
				httpresponse.getStatusLine().getStatusCode();

				HttpEntity httpEntity = httpresponse.getEntity();
				response = EntityUtils.toString(httpEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}

			catch (SSLHandshakeException e) {
				e.printStackTrace();
			} catch (SSLException e) {
				e.printStackTrace();
			}

			catch (IOException e) {
				e.printStackTrace();
			}

			catch (Exception e) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			super.onPostExecute(result);

			Type type = new TypeToken<LoginResultVO>() {
			}.getType();
			loginResultVo = gson.fromJson(result, type);
			pDialog.dismiss();
			updateUI(true);

		}

	}

	/*private void updateUI(boolean isSignedIn) {

		if (isSignedIn) {

			if (loginResultVo.getApproved().equalsIgnoreCase("true")) {

				Intent i = new Intent(OvalAccountApprovalActivity.this, OvalSearchActivity.class);
				i.putExtra("connectionID", 0);
				startActivity(i);
				finish();

			} else {
				Toast.makeText(OvalAccountApprovalActivity.this, "Not yet Approved", Toast.LENGTH_LONG).show();

			}

		}

	}*/

	private void updateUI(boolean isSignedIn) {
		String status = loginResultVo.getApproved();

		if (isSignedIn) {
			
			if (status.equalsIgnoreCase("true")) {

				long result;
				
				connectionInfo.setStatus(OvalLoginActivity.STATUS_LOGGEDIN);

				result = dbHandler.updateConnectionInfo(connectionInfo);
				if (result > -1) {
					/*
					 * Intent i = new Intent(OvalLoginActivity.this,
					 * ConnectionList.class); i.setAction(ACTION_LAUNCH_APP);
					 */

					refreshApps(connectionInfo);
					/*
					 * Intent i = new Intent(OvalLoginActivity.this,
					 * OvalSearchActivity.class); i.putExtra("connectionID", 0);
					 * startActivity(i); finish();
					 */
				}
			} else {
				Toast.makeText(OvalAccountApprovalActivity.this, "Not yet Approved", Toast.LENGTH_LONG).show();
			}

			// enrytpion type and auth type to one and certificate to a
			// blank string
			// update id gives connection id that is zero in this case

			// insert or update the ConnectionInfo in the database

			/*
			 * if (.equalsIgnoreCase("true")) {
			 * 
			 * 
			 * } else {
			 * 
			 * }
			 */

		} else {
			Intent i = new Intent(OvalAccountApprovalActivity.this, OvalSearchActivity.class);
			i.putExtra("connectionID", 0);
			startActivity(i);
			finish();
		}
	}

	private void refreshApps(ConnectionInfo connectionInfo) {
		// TODO Auto-generated method stub

		this.sendRequestCode = AppList.REQUEST_REFRESHAPPS_FULL;
		authPrompt(connectionInfo); // utilizes "startActivityForResult", which
									// uses this.sendRequestCode

	}
	
	
	@Override
	protected void afterStartAppRTC(ConnectionInfo connectionInfo) {
		// after we have handled the auth prompt and made sure the service is
		// started...

		// create explicit intent
		Intent intent = new Intent();
		if (this.sendRequestCode == AppList.REQUEST_REFRESHAPPS_QUICK
				|| this.sendRequestCode == AppList.REQUEST_REFRESHAPPS_FULL) {
			// we're refreshing our cached list of apps that reside on the VM
			intent.setClass(OvalAccountApprovalActivity.this, AppRTCRefreshAppsActivity.class);
			if (this.sendRequestCode == AppList.REQUEST_REFRESHAPPS_FULL)
				intent.putExtra("fullRefresh", true);
		}

		intent.putExtra("connectionID", connectionInfo.getConnectionID());

		// start the AppRTCActivity
		startActivityForResult(intent, this.sendRequestCode);
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		// TODO Auto-generated method stub
		if (requestCode == AppList.REQUEST_REFRESHAPPS_QUICK || requestCode == AppList.REQUEST_REFRESHAPPS_FULL) {
			if (responseCode == RESULT_CANCELED) {
				// the activity ended before processing the Apps response
				toastShort(R.string.appList_toast_refreshFail);
			} else if (responseCode == RESULT_OK) {
				toastShort(R.string.appList_toast_refreshSuccess);

				super.onActivityResult(requestCode, RESULT_REPOPULATE, intent);

				Intent i = new Intent(OvalAccountApprovalActivity.this, OvalSearchActivity.class);
				i.putExtra("connectionID", 0);
				startActivity(i);
				finish();
			} else {
				// this is probably a result of an AUTH_FAIL, let superclass
				// handle it
				super.onActivityResult(requestCode, responseCode, intent);
			}
		} else if (responseCode == RESULT_CANCELED && requestCode == AppList.REQUEST_STARTAPP_FINISH) {
			// the user intentionally canceled the activity, and we are
			// supposed to finish this activity after resuming
			finish();
		} else // fall back to superclass method
			super.onActivityResult(requestCode, responseCode, intent);

	}
	
}


