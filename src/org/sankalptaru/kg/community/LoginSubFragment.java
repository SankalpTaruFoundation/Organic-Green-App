package org.sankalptaru.kg.community;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginSubFragment extends Fragment {

	private EditText username;
	private EditText password;
	private String CSRF;
	FragmentManager fm;
	LoginFragment loginFragment;

	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private Context context;
	public LoginSubFragment(FragmentManager fm, LoginFragment loginFragment, Context context) {
		// TODO Auto-generated constructor stub
		this.fm=fm;
		this.loginFragment=loginFragment;
		this.context=context;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		/*AppUtil.SuperRestartServiceIntent = PendingIntent.getActivity(getActivity().getBaseContext(), 0,
				new Intent(getActivity().getIntent()), getActivity().getIntent().getFlags());
		 */
		View rootView = inflater.inflate(R.layout.sub_login_fragment, container, false);

		Button loginBtn=(Button)rootView.findViewById(R.id.loginButton);

		Button resetBtn=(Button)rootView.findViewById(R.id.resetLoginBtn);

		username= (EditText) rootView.findViewById(R.id.textUsername);
		password= (EditText) rootView.findViewById(R.id.textPassword);
		//		user_cityuser_interest

		final CheckBox mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);

		mCbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					// show password
					password.setTransformationMethod(PasswordTransformationMethod.getInstance());
				} else {
					// hide password
					password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}
			}
		});
		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loginFragment.hideSoftKeyboard();
				if(username.length()>0&&password.length()>0){
					JSONObject json = new JSONObject();
					//extract the username and password from UI elements and create a JSON object
					try {
						json.put("username", username.getText().toString().trim());
						json.put("password", password.getText().toString().trim());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cd = new ConnectionDetector(context);

					// Check if Internet present
					if (!cd.isConnectingToInternet()) {
						// Internet Connection is not present
						alert.showAlertDialog(getActivity(),
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

						new Login(json).execute("http://www1.kitchengardens.in/svc/user/login/");
					}
				}
				else{
					Toast.makeText(getActivity(), "All fields are mandatory.", 3000).show();
				}
			}
		});

		resetBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCbShowPwd.setChecked(false);
				resetLoginComponents();
			}
		});

		return rootView;
	}
	public void resetLoginComponents(){
		username.setText("");
		password.setText("");
	}

	
	public class Login extends AsyncTask<String, Integer, JSONObject> {

		private JSONObject jsObject;
		public Login(JSONObject json) {
			// TODO Auto-generated constructor stub
			jsObject=json;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			loginFragment.initializeProgressBar("Authenticating...");
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
			loginFragment.cancelProgressBar();
			if(null != result){
				Log.e("data", ""+result); 

				String username = jsObject.optString("username");

				Toast.makeText(getActivity(), "Welcome, "+username+".!", 1000).show();
				SharedPreferences pref = getActivity().getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
				SharedPreferences.Editor editor = pref.edit();

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
				editor.putString("Google_Email", AppUtil.getUserPrimaryAccountDetails(context));

				editor.commit();
				loginFragment.backToMenu();

			}else {
				AlertloginFragment dialog=new AlertloginFragment(LoginSubFragment.this,null,"Login", "Warning", "Server Error: Wrong Username or Password", "Try Again", "Cancel","null_response");
				dialog.setCancelable(false);
				dialog.show(fm, "login");
			}
		}
	}
}
