package com.oval.app.activities;

import org.mitre.svmp.activities.SvmpActivity;
import com.citicrowd.oval.R;
import org.mitre.svmp.common.ConnectionInfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class OvalSplashActivity extends SvmpActivity {

	ProgressBar splashPrgrssBr;
	Intent i;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.activity_splash);
		splashPrgrssBr = (ProgressBar) findViewById(R.id.splashPrgrssBr);

		if (dbHandler.getConnectionInfoList().size() == 0) {
			i = new Intent(OvalSplashActivity.this, OvalLoginActivity.class);

		} else {
			ConnectionInfo connectionInfo = dbHandler.getConnectionInfoList().get(0);
			if (connectionInfo == null) {
				i = new Intent(OvalSplashActivity.this, OvalLoginActivity.class);

			} else if (connectionInfo.getStatus() == OvalLoginActivity.STATUS_LOGGEDIN) {
				i = new Intent(OvalSplashActivity.this, OvalSearchActivity.class);
				i.putExtra("connectionID", 0);

			} else if (connectionInfo.getStatus() == OvalLoginActivity.STATUS_REGISTERED) {
				i = new Intent(OvalSplashActivity.this, OvalAccountApprovalActivity.class);
				i.putExtra("connectionID", 0);

			}

		}

		startThread(1000);

	}

	private void startThread(final int x) {
		// TODO Auto-generated method stub
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				launchActivity();

			}
		}, x);
	}

	public void launchActivity() {
		if (i != null) {
			startActivity(i);
			finish();
		}
		
	}

}
