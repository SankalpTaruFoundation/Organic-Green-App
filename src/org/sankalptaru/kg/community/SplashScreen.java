package org.sankalptaru.kg.community;

import static org.sankalptaru.kg.community.pushnotifications.helper.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static org.sankalptaru.kg.community.pushnotifications.helper.CommonUtilities.EXTRA_MESSAGE;
import static org.sankalptaru.kg.community.pushnotifications.helper.CommonUtilities.SENDER_ID;

import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.CalenderHelper;
import org.sankalptaru.kg.community.helper.ConnectionDetector;
import org.sankalptaru.kg.community.pushnotifications.helper.ServerUtilities;
import org.sankalptaru.kg.community.pushnotifications.helper.WakeLocker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class SplashScreen extends Activity{
	private String CSRF;
	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private SharedPreferences myPrefs;
	private LinearLayout autoLoginLyt;
	private TextView loadtext;
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.st_splash);


		final ImageView i=(ImageView)findViewById(R.id.splash_logo);
		final TextView t=(TextView)findViewById(R.id.disclaimer);


		new Handler().postDelayed(new Runnable() {
			public void run() {

				Animation a=AnimationUtils.loadAnimation(SplashScreen.this, R.anim.slide_down);
				a.setDuration(3500);
				i.startAnimation(a);
				i.setVisibility(View.VISIBLE);

				Animation b=AnimationUtils.loadAnimation(SplashScreen.this, R.anim.slide_up);
				b.setDuration(3500);
				t.startAnimation(b);
				t.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {


					public void run() {
						myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
						autoLoginLyt=(LinearLayout)findViewById(R.id.autoLoginLyt);
						loadtext=(TextView)findViewById(R.id.loadingTxt);
						boolean flag =myPrefs.getBoolean("isRegesterdWithGCM", false);
						Log.e("flag", ""+flag);
						if(!flag)
						{
//							autoLoginLyt.setVisibility(View.VISIBLE);
//							loadtext.setText("Connecting with server...");
							registerWithGCM();
							doValidationAndLoginActions();

						}else{
							doValidationAndLoginActions();
						}
					}
				},4000);
			}
		}, 1000);
	}


	private void registerWithGCM() {
		// TODO Auto-generated method stub
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(SplashScreen.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}


		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM			
			GCMRegistrar.register(this, SENDER_ID);
			if(!GCMRegistrar.getRegistrationId(this).equals("")){
				changeGCMPrefFlagToTrue(true);
			}
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.			
				changeGCMPrefFlagToTrue(true);
				Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						ServerUtilities.register(context, "", "", regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
						String regIDlocal= GCMRegistrar.getRegistrationId(SplashScreen.this);
						if (regIDlocal.equals("")) {
							changeGCMPrefFlagToTrue(false);
						}
						else{
							if (GCMRegistrar.isRegisteredOnServer(SplashScreen.this)) {
								// Skips registration.			
								changeGCMPrefFlagToTrue(true);
								Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
							} 
						}
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}


	private void doValidationAndLoginActions() {
		// TODO Auto-generated method stub

		double hrs=AppUtil.getTimeDifferenceForLogin(myPrefs.getLong("timeOfLogin", 0));
		if(hrs>2.0&&myPrefs.getInt("uid", -1)!=-1){
			autoLoginLyt.setVisibility(View.VISIBLE);
			loadtext.setText("Loading...");
			final JSONObject json = new JSONObject();
			//extract the username and password from UI elements and create a JSON object
			try {
				json.put("username", myPrefs.getString("username_cred", ""));
				json.put("password", myPrefs.getString("password_cred", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cd = new ConnectionDetector(getApplicationContext());

			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(SplashScreen.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				new CSRFToken(){
					protected void onPostExecute(String result) {
						CSRF=result;

					};
				}.execute("http://www1.kitchengardens.in/services/session/token");
				new LoginSplash(json).execute("http://www1.kitchengardens.in/svc/user/login/");
			}
		}else{
			createCalander();
			Intent i=new Intent(SplashScreen.this,MainActivity.class);
			startActivity(i);
			finish();	
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);			
		}
	}
	public class LoginSplash extends AsyncTask<String, Integer, JSONObject> {

		private JSONObject jsObject;
		public LoginSplash(JSONObject json) {
			// TODO Auto-generated constructor stub
			jsObject=json;
			Log.e("jsObject", ""+jsObject);
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(String... arg0) {

			JSONObject jsResponse=null;
			try {
				jsResponse=new JSONObject(AppUtil.doPost(arg0[0], jsObject, CSRF,false,""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsResponse;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = pref.edit();
			if(null != result){
				Log.e("data", ""+result); 

				String username = jsObject.optString("username");
				JSONObject userObject=result.optJSONObject("user");

				int uid = userObject.optInt("uid");
				String mail = userObject.optString("mail");
				String name = userObject.optString("name");
				String garden_id= userObject.optJSONObject("field_profile_node").optJSONArray("und").optJSONObject(0).optString("target_id");
				String token = result.optString("token");
				String session_id = result.optString("sessid");
				String session_name = result.optString("session_name");

				editor.putString("username_cred", jsObject.optString("username"));
				editor.putString("password_cred", jsObject.optString("password"));
				editor.putString("mail", mail);
				editor.putString("username", name);
				editor.putInt("uid", uid);
				editor.putString("garden_id", garden_id);
				editor.putString("token", token);
				editor.putString("sessid", session_id);
				editor.putString("session_name", session_name);
				editor.putLong("timeOfLogin", new java.util.Date().getTime()/ 1000L);
				editor.putString("Google_Email", AppUtil.getUserPrimaryAccountDetails(getApplicationContext()));
				editor.commit();
			}else {
				editor.putInt("uid", -1);
				editor.commit();
			}

			createCalander();

			Intent i=new Intent(SplashScreen.this,MainActivity.class);
			startActivity(i);
			finish();	
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
		}

	}
	private void createCalander() {
		// TODO Auto-generated method stub
		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		boolean doCalenderExists=myPrefs.getBoolean("st_calender", false);
		Log.e("doCalenderExists",""+ doCalenderExists +" "+myPrefs.getLong("calId", -1));

		if(!doCalenderExists){
			CalenderHelper calHelper=new CalenderHelper(getApplicationContext());
			String calID=calHelper.createCalender();
			SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("st_calender", true);
			editor.putLong("calId", Long.parseLong(calID));
			editor.commit();
		}
	}
	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message
			 * depending upon your app requirement
			 * For now i am just displaying it on the screen
			 * */

			// Showing received message
			//			lblMessage.append(newMessage + "\n");			
			Toast.makeText(getApplicationContext(), "New Message " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	private void changeGCMPrefFlagToTrue(boolean value){
		SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("isRegesterdWithGCM", value);
		editor.commit();
		doValidationAndLoginActions();
	}
}
