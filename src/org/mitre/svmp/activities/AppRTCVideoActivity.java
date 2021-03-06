/*
 * Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this work except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Derived from AppRTCActivity from the libjingle / webrtc AppRTCDemo
// example application distributed under the following license.
/*
 * libjingle
 * Copyright 2013, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.mitre.svmp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.appspot.apprtc.VideoStreamsView;
import org.json.JSONException;
import org.json.JSONObject;
import org.mitre.svmp.apprtc.*;
import org.mitre.svmp.client.*;
import org.mitre.svmp.protocol.SVMPProtocol;
import org.mitre.svmp.protocol.SVMPProtocol.AppsRequest;
import org.mitre.svmp.protocol.SVMPProtocol.IntentAction;
import org.mitre.svmp.protocol.SVMPProtocol.Request;
import org.mitre.svmp.protocol.SVMPProtocol.Response;
import org.mitre.svmp.protocol.SVMPProtocol.Request.RequestType;
import org.webrtc.*;
import com.citicrowd.oval.R;
import com.google.android.gms.drive.internal.RemoveEventListenerRequest;
import com.oval.app.activities.OvalDrawerActivity;

import java.util.HashMap;
import java.util.TimeZone;

/**
 * @author Joe Portner General purpose activity to display a video feed and
 *         allow the user to interact with a remote VM
 */
public class AppRTCVideoActivity extends AppRTCActivity {
	private static final String TAG = AppRTCVideoActivity.class.getName();

	private MediaConstraints sdpMediaConstraints;
	private SDPObserver sdpObserver;
	// private SDPAObserver sdpaObserver;
	private VideoStreamsView vsv;
	private PCObserver pcObserver;
	private TouchHandler touchHandler;
	private RotationHandler rotationHandler;
	private String pkgName; // what app we want to launch when we finish
							// connecting
	private KeyHandler keyHandler;
	private ConfigHandler configHandler;
	private String apkPath;

	private GestureDetectorCompat mDetector;
	boolean scrollClicked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Get info passed to Intent
		final Intent intent = getIntent();
		pkgName = intent.getStringExtra("pkgName");
		apkPath = intent.getStringExtra("apkPath");

		mDetector = new GestureDetectorCompat(this, new CustomScrollGesture());
		/*
		 * getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		 * getActionBar().hide();
		 */

		super.onCreate(savedInstanceState);

		qualitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub

				HashMap<String, String> map = new HashMap<>();
				if (position == 0) {
					map.put("height", "");
					map.put("width", "");

				} else if (position == 1) {
					map.put("height", "640");
					map.put("width", "360");
				} else if (position == 2) {

					map.put("height", "1280");
					map.put("width", "720");
				}
				map.put("type", "resolution_change");

				JSONObject jObj = new JSONObject(map);
				AppRTCHelper.makeWebRTCRequest(jObj);

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

	}

	/*
	 * @Override protected void startProgressDialog() { // not needed }
	 * 
	 * @Override public void stopProgressDialog() { // TODO Auto-generated
	 * method stub //not needed }
	 */
	class DrawingView extends SurfaceView {

		private final SurfaceHolder surfaceHolder;
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		public DrawingView(Context context) {
			super(context);
			surfaceHolder = getHolder();
			paint.setColor(Color.RED);
			paint.setStyle(Style.FILL);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (surfaceHolder.getSurface().isValid()) {
					Canvas canvas = surfaceHolder.lockCanvas();
					canvas.drawColor(Color.BLACK);
					canvas.drawCircle(event.getX(), event.getY(), 50, paint);
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			return false;
		}
	}

	@Override
	protected void connectToRoom() {
		// Uncomment to get ALL WebRTC tracing and SENSITIVE libjingle logging.
		// Logging.enableTracing(
		// "/sdcard/trace.txt",
		// EnumSet.of(Logging.TraceLevel.TRACE_ALL),
		// Logging.Severity.LS_SENSITIVE);

		Point deviceDisplaySize = new Point();
		// displaySize.set(720, 1280);
		getWindowManager().getDefaultDisplay().getSize(deviceDisplaySize);

		Point displaySize = new Point();
		displaySize.set(deviceDisplaySize.x, deviceDisplaySize.y * (16 / 9));

		// displaySize.set(720, 1280);

		vsv = new VideoStreamsView(this, displaySize, performanceAdapter);
		vsv.setPreserveEGLContextOnPause(true);
		vsv.setBackgroundColor(Color.WHITE); // start this VideoStreamsView
												// with a color of dark gray

		createTopPanel();

		setContentView(vsv);

		addContentView(ll,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50 * deviceDisplaySize.y / 1280));
		addContentView(scrollBtnsRLayout,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				// addContentView(view, params);

		// vsvProgrssBar.setVisibility(View.INVISIBLE);

		touchHandler = new TouchHandler(this, displaySize, performanceAdapter);
		rotationHandler = new RotationHandler(this);
		keyHandler = new KeyHandler(this);
		configHandler = new ConfigHandler(this);

		AppRTCHelper.abortUnless(PeerConnectionFactory.initializeAndroidGlobals(this),
				"Failed to initializeAndroidGlobals");

		// Create observers.
		sdpObserver = new SDPObserver(this);
		// sdpaObserver = new SDPAObserver(this);
		pcObserver = new PCObserver(this);

		sdpMediaConstraints = new MediaConstraints();
		sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
		sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

		super.connectToRoom();
	}

	private void createTopPanel() {
		// TODO Auto-generated method stub

		ll.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		ImageView homeStreamingBtn = (ImageView) findViewById(R.id.homeStreamingBtn);
		homeStreamingBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(AppRTCVideoActivity.this, OvalDrawerActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				disconnectAndExit();

			}
		});
		ImageView stopStreamingBtn = (ImageView) findViewById(R.id.stopStreamingBtn);

		stopStreamingBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				disconnectAndExit();
			}
		});
		
		scrollUpImgVw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub
				if (scrollClicked == false) {
					scrollClicked=true;
					Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vb.vibrate(50);
				//	vsv.onPause();
					vsvProgrssBar.setVisibility(View.VISIBLE);

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							vsvProgrssBar.setVisibility(View.INVISIBLE);
						//	vsv.onResume();
							scrollClicked = false;
						}
					}, 2000);

					SVMPProtocol.Request.Builder msg = SVMPProtocol.Request.newBuilder();
					SVMPProtocol.TouchEvent.Builder eventmsg = SVMPProtocol.TouchEvent.newBuilder();

					eventmsg.setAction(51);
					msg.setType(RequestType.TOUCHEVENT);
					msg.addTouch(eventmsg); // TODO: batch touch events

					// Send touch event to VM

					sendMessage(msg.build());
				}

			
			}
		});
		
		scrolldownImgVw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub
				if (scrollClicked == false) {
					scrollClicked=true;
					
					Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vb.vibrate(50);
				//	vsv.onPause();
					vsvProgrssBar.setVisibility(View.VISIBLE);

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							vsvProgrssBar.setVisibility(View.INVISIBLE);
							//vsv.onResume();
							scrollClicked = false;
						}
					}, 2000);

					SVMPProtocol.Request.Builder msg = SVMPProtocol.Request.newBuilder();
					SVMPProtocol.TouchEvent.Builder eventmsg = SVMPProtocol.TouchEvent.newBuilder();

					eventmsg.setAction(50);
					msg.setType(RequestType.TOUCHEVENT);
					msg.addTouch(eventmsg); // TODO: batch touch events

					// Send touch event to VM

					sendMessage(msg.build());
				}

			
			}
		});

		
		

		
		((ViewGroup) scrollBtnsRLayout.getParent()).removeView(scrollBtnsRLayout);

		((ViewGroup) ll.getParent()).removeView(ll);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// send the updated configuration to the VM (i.e. whether or not a
		// hardware keyboard is plugged in)
		configHandler.handleConfiguration(newConfig);
	}

	public VideoStreamsView getVSV() {
		return vsv;
	}

	public PCObserver getPCObserver() {
		return pcObserver;
	}

	/*
	 * public MediaConstraints getSdpMediaConstraints() { return
	 * sdpMediaConstraints; }
	 * 
	 * public boolean isInitiator() { return appRtcClient.isInitiator(); }
	 */

	// called from PCObserver
	public MediaConstraints getPCConstraints() {
		MediaConstraints value = null;
		if (appRtcClient != null)
			value = appRtcClient.getSignalingParams().pcConstraints;
		return value;
	}

	/*
	 * @Override protected void startProgressDialog() {
	 * vsv.setBackgroundColor(Color.DKGRAY); // if it isn't already set, make //
	 * the background color dark // gray super.startProgressDialog(); }
	 */

	@Override
	public void onPause() {
		// vsv.onPause();
		super.onPause(true);
	}

	@Override
	public void onResume() {
		vsv.onResume();
		super.onResume();
	}

	// MessageHandler interface method
	// Called when the client connection is established
	@Override
	public void onOpen() {
		super.onOpen();

		// set up ICE servers
		pcObserver.onIceServers(appRtcClient.getSignalingParams().iceServers);

		// send timezone information
		Request.Builder request = Request.newBuilder();
		request.setType(Request.RequestType.TIMEZONE);
		request.setTimezoneId(TimeZone.getDefault().getID());
		sendMessage(request.build());

		touchHandler.sendScreenInfoMessage();
		// rotationHandler.initRotationUpdates();

		// send the initial configuration to the VM
		Configuration config = getResources().getConfiguration();
		configHandler.handleConfiguration(config);

		// tell the VM what app we want to start
		if (apkPath != null) {
			sendAppsMessageToOvalAppSrvc();
		}
		// sendAppsMessageToOvalAppSrvc();
		else {
			sendAppsMessage();
		}

		PeerConnection pc = pcObserver.getPC();
		if (pc != null)
			// pcObserver.changeResolution("AUTO");
			pc.createOffer(sdpObserver, sdpMediaConstraints);
	}

	// sends "APPS" request to VM; if pkgName is not null, start that app,
	// otherwise go to the Launcher
	private void sendAppsMessage() {

		AppsRequest.Builder aBuilder = AppsRequest.newBuilder();

		aBuilder.setType(AppsRequest.AppsRequestType.LAUNCH);
		// if we've been given a package name, start that app
		if (pkgName != null)
			aBuilder.setPkgName(pkgName);
		Request.Builder rBuilder = Request.newBuilder();

		rBuilder.setType(Request.RequestType.APPS);

		rBuilder.setApps(aBuilder);
		sendMessage(rBuilder.build());

	}

	private void sendAppsMessageToOvalAppSrvc() {

		SVMPProtocol.Request.Builder msg = SVMPProtocol.Request.newBuilder();
		SVMPProtocol.Intent.Builder intentProtoBuffer = SVMPProtocol.Intent.newBuilder();
		intentProtoBuffer.setAction(IntentAction.ACTION_VIEW);
		intentProtoBuffer.setData(apkPath);

		// Set the Request message params and send it off.
		msg.setType(RequestType.INTENT);
		msg.setIntent(intentProtoBuffer.build());

		sendMessage(msg.build());

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

				sendAppsMessage();

			}
		}, 30000);
	}

	// MessageHandler interface method
	// Called when a message is sent from the server, and the SessionService
	// doesn't consume it
	public boolean onMessage(Response data) {
		switch (data.getType()) {
		case APPS:
			if (data.hasApps() && data.getApps().getType() == SVMPProtocol.AppsResponse.AppsResponseType.EXIT) {
				// we have exited a remote app; exit back to our parent activity
				// and act accordingly
				disconnectAndExit();
			}
			break;
		case SCREENINFO:
			handleScreenInfo(data);
			break;
		case WEBRTC:

			makePanelVisible();

			try {
				JSONObject json = new JSONObject(data.getWebrtcMsg().getJson());
				Log.d(TAG, "Received WebRTC message from peer:\n" + json.toString(4));
				String type;
				// peerconnection_client doesn't put a "type" on candidates
				try {
					type = (String) json.get("type");
				} catch (JSONException e) {
					json.put("type", "candidate");
					type = (String) json.get("type");
				}

				// Check out the type of WebRTC message.
				if (type.equals("candidate")) {
					IceCandidate candidate = new IceCandidate((String) json.get("id"), json.getInt("label"),
							(String) json.get("candidate"));
					getPCObserver().addIceCandidate(candidate);
				} else if (type.equals("answer") || type.equals("offer")) {

					SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type),
							AppRTCHelper.preferISAC((String) json.get("sdp")));
					getPCObserver().getPC().setRemoteDescription(sdpObserver, sdp);
				} else if (type.equals("bye")) {

					logAndToast(R.string.appRTC_toast_clientHandler_finish);
					disconnectAndExit();
				} else {
					throw new RuntimeException("Unexpected message: " + data);
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			break;
		default:
			// any messages we don't understand, pass to our parent for
			// processing
			super.onMessage(data);
		}
		return true;
	}

	private void makePanelVisible() {
		// TODO Auto-generated method stub
		AppRTCVideoActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Log.d("UI thread", "I am the UI thread");
				ll.setVisibility(View.VISIBLE);
				scrollBtnsRLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void onDisconnectAndExit() {
		if (rotationHandler != null)
			rotationHandler.cleanupRotationUpdates();
		if (pcObserver != null)
			pcObserver.quit();
	}

	/////////////////////////////////////////////////////////////////////
	// Bridge input callbacks to the Touch Input Handler
	/////////////////////////////////////////////////////////////////////
	private void handleScreenInfo(Response msg) {
		touchHandler.handleScreenInfoResponse(msg);
	}

	private float mDownX;
	private float mDownY;
	private final float SCROLL_THRESHOLD = 10;
	private boolean isOnClick;
	// private boolean isVsvPaused = false;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		Log.e(TAG, "inside activity on touch.");
		Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mDownX = ev.getX();
			mDownY = ev.getY();
			isOnClick = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:

			if (isOnClick) {
				Log.i(TAG, "onClick ");

				// pauseVsv();
				vb.vibrate(50);
				// return touchHandler.onTouchEvent(ev);

				// TODO onClick code
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isOnClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD
					|| Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD)) {
				Log.i(TAG, "movement detected");
				isOnClick = false;

				//vsv.onPause();

			}
			break;
		default:

			break;
		}
		vsvProgrssBar.setVisibility(View.VISIBLE);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				vsvProgrssBar.setVisibility(View.INVISIBLE);
				//vsv.onResume();
			}
		}, 1000);

		Log.e(TAG, "on activity touch is finishing");

		return touchHandler.onTouchEvent(ev);

	}

	Handler handler = new Handler();

	private void pauseVsv() {
		// TODO Auto-generated method stub
		Log.i(TAG, "VSV Paused");
		vsv.onPause();
		// isVsvPaused = true;

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// isVsvPaused = false;
				Log.i(TAG, "VSV Resumed");
				vsv.onResume();
			}
		}, 2000);

	}

	// intercept KeyEvent before it is dispatched to the window
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return keyHandler.tryConsume(event) || super.dispatchKeyEvent(event);
	}

	class CustomScrollGesture extends GestureDetector.SimpleOnGestureListener {
		public static final String TAG = "CustomScrollGesture";

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub

			Log.i(TAG, "Single TAp");
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(10);

			return super.onSingleTapConfirmed(e);
		}
	}
}
