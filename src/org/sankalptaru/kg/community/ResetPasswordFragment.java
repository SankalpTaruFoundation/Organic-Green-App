package org.sankalptaru.kg.community;

import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ResetPasswordFragment extends Fragment {

	private FragmentManager fm;
	private LoginFragment loginFragment; 
	private Context context;
	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private String CSRF;


	public ResetPasswordFragment(FragmentManager fm,
			LoginFragment loginFragment, Context context) {
		// TODO Auto-generated constructor stub
		this.fm=fm;
		this.context=context;
		this.loginFragment=loginFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);

		final EditText emilTxt=(EditText)rootView.findViewById(R.id.textEmail);

		Button resetPasswordBtn=(Button)rootView.findViewById(R.id.requestPswdBtn);
		resetPasswordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String email=emilTxt.getText().toString();
				if(email.length()>0){
					emilTxt.setText("");
					loginFragment.hideSoftKeyboard();
					//					if(AppUtil.isEmailValid(email)){
					JSONObject jsonObject= new JSONObject();
					try {
						jsonObject.put("name",email );
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
							new ForgotPass(jsonObject).execute("http://www1.kitchengardens.in/svc/user/request_new_password");
						}
					}
				}
				//				}
			}
		});
		return rootView;
	}
	private class ForgotPass extends AsyncTask<String, Integer, String>{

		private JSONObject jsObject;
		public ForgotPass(JSONObject json) {
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
		protected String doInBackground(String... params) {
			String jsResponse=null;
			jsResponse=AppUtil.doPost(params[0], jsObject, CSRF,false,"");
			return jsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.e("result", ""+result);
			loginFragment.cancelProgressBar();
			PasswordStatusDialog dialog;
			if(result.contains("true")){
				dialog=new PasswordStatusDialog("Password Reset Successfull","Password reset instructions mailed to the registered e-mail address.","OK");
			}else{
				dialog=new PasswordStatusDialog("Password Reset Failed","Sorry, name is not recognized as a user name or an e-mail address.Contact Administrator.","OK");
			}
			dialog.setCancelable(true);
			dialog.show(fm, "");
		}
	}
	private class PasswordStatusDialog extends android.support.v4.app.DialogFragment{

		private CharSequence title;
		private CharSequence message;
		private CharSequence positiveBtnTxt;
		private CharSequence negativeTxt;
		private Button negativeBtn;

		public PasswordStatusDialog(String titString, String message,
				String positiveBtnTxt) {
			// TODO Auto-generated constructor stub
			this.title=titString;
			this.positiveBtnTxt=positiveBtnTxt;
			this.message=message;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())


			// Set Dialog Icon
			.setIcon(android.R.drawable.ic_dialog_alert)
			// Set Dialog Title
			.setTitle(title)
			// Set Dialog Message
			.setMessage(message)

			// Positive button
			.setPositiveButton(positiveBtnTxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Do something else
				}
			})

			// Negative Button
			.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,    int which) {
					// Do something else

				}
			}).create();

		}

		@Override
		public void onStart() {
			super.onStart();
			AlertDialog d = (AlertDialog) getDialog();
			negativeBtn = (Button)d.getButton(Dialog.BUTTON_NEGATIVE);
			negativeBtn.setVisibility(View.GONE);

		}
	}
}
