package org.sankalptaru.kg.community;

import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.content.Context;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterFragment extends Fragment {

	FragmentManager fm;
	LoginFragment loginFragment;
	//	private EditText username;
	private EditText mail;
	private EditText password;
	private String CSRF;
	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private Context context;

	public RegisterFragment(FragmentManager fm, LoginFragment loginFragment, Context context) {
		// TODO Auto-generated constructor stub
		this.fm=fm;
		this.loginFragment=loginFragment;
		this.context=context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_register, container, false);

		/*Button registerBtn=(Button)rootView.findViewById(R.id.registerButton);

		Button resetBtn=(Button)rootView.findViewById(R.id.resetRegisterBtn);

//		username=(EditText)rootView.findViewById(R.id.textUsername);
		mail=(EditText)rootView.findViewById(R.id.textMail);
		password=(EditText)rootView.findViewById(R.id.textPassword);
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


		registerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loginFragment.hideSoftKeyboard();

				String email=mail.getText().toString().trim();
				String pass=password.getText().toString().trim();
//				String mName=username.getText().toString().trim();

				if(password.length()>0&&mail.length()>0){
					if(AppUtil.isEmailValid(email)){
						JSONObject jsonObject= new JSONObject();
						try {
							jsonObject.put("name", email);
							jsonObject.put("pass", pass);
							jsonObject.put("mail",email );
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
							cd = new ConnectionDetector(context);

							if (!cd.isConnectingToInternet()) {
								// Internet Connection is not present
								alert.showAlertDialog(getActivity(),
										"Internet Connection Error",
										"Please connect to working Internet connection", false);
								// stop executing code by return
								return;
							}else{
								new Register(jsonObject).execute("http://www1.kitchengardens.in/svc/user/");
							}
						}
					}
					else{
						Toast.makeText(getActivity(), "Enter valid E-mail address.", 3000).show();
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

			}
		});
		 */
		return rootView;

	}

	private class Register extends AsyncTask<String, Integer, JSONObject>{

		private JSONObject jsObject;
		private String CSRF;
		public Register(JSONObject json) {
			// TODO Auto-generated constructor stub
			jsObject=json;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			loginFragment.initializeProgressBar("Processing...");
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject jsResponse=null;
			try {
				jsResponse=new JSONObject(AppUtil.doPost(params[0], jsObject, CSRF,false,""));
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
			Log.e("result", ""+result);
			loginFragment.cancelProgressBar();
			if(null!=result){
				try {
					if(result.has("form_errors")){
						JSONObject errorJSObject=result.getJSONObject("form_errors");
						if((errorJSObject.optString("name").length()>0)&&(errorJSObject.optString("mail").length()>0)){
							//							createDialog("E-Mail and Username is already registered", MainActivity.this,"Warning",false);
							AlertloginFragment dialog=new AlertloginFragment(null,RegisterFragment.this,"Register", "Warning", "E-Mail and Username is already registered", "Try Again", "Cancel","both_registered");
							dialog.setCancelable(false);
							dialog.show(fm, "Register");
						}
						else if((errorJSObject.optString("name").length()>0)){
							//							createDialog("Username is already registered", MainActivity.this,"Warning",false);
							AlertloginFragment dialog=new AlertloginFragment(null,RegisterFragment.this,"Register", "Warning", "Username is already registered", "Try Again", "Cancel","name_registered");
							dialog.setCancelable(false);
							dialog.show(fm, "Register");
						}
						else if(errorJSObject.optString("mail").length()>0){
							//							createDialog("E-Mail is already registered", MainActivity.this,"Warning",false);
							AlertloginFragment dialog=new AlertloginFragment(null,RegisterFragment.this,"Register", "Warning", "Username is already registered", "Try Again", "Cancel","mail_registered");
							dialog.setCancelable(false);
							dialog.show(fm, "Register");
						}
					}
					else{
						//						createDialog("Registration Done.\nActivate your account by clicking on activation link sent on registered E-mail address.", MainActivity.this,"Success",true);
						AlertloginFragment dialog=new AlertloginFragment(null,RegisterFragment.this,"Register", "Success", "Registration Done.\n\nActivate your account by clicking on activation link sent on registered E-mail address.", "OK", "Cancel","success");
						dialog.setCancelable(false);
						dialog.show(fm, "Register");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{						
				AlertloginFragment dialog=new AlertloginFragment(null,RegisterFragment.this,"Register", "Warning", "Registeration Failed.", "Try Again", "Cancel","null_response");
				dialog.setCancelable(false);
				dialog.show(fm, "Register");
			}
		}
	}
	public void resetRegisterComponents(){
		mail.setText("");
		password.setText("");
	}

	public void goToLoginPage(){
		loginFragment.changePageToLogin();
	}
}
