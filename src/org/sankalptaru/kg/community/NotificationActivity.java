package org.sankalptaru.kg.community;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		String msg=myPrefs.getString("notification_msg", "null");
		if(msg.equals("null")){
			backToSplash();
		}else{
			openNotificationDialog(msg);
		}
	}

	private void openNotificationDialog(String message){
		NotificationDialog notifDilaog=new NotificationDialog(message);
		notifDilaog.setCancelable(false);
		notifDilaog.show(getFragmentManager(), "");
	}

	private class NotificationDialog extends android.app.DialogFragment{
		String message;
		public NotificationDialog(String message) {
			// TODO Auto-generated constructor stub
			this.message=message;
			Log.e("nsn", message);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.notification_dialog, container,
					false);
			Window window = getDialog().getWindow();

			getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
			window.setBackgroundDrawableResource(android.R.color.transparent);

			TextView notfiTxt=(TextView)rootView.findViewById(R.id.notificationTxt);
			notfiTxt.setText(message);

			Button closeDialogBtn=(Button)rootView.findViewById(R.id.closeDialogBtn);
			closeDialogBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dismiss();
					backToSplash();
				}
			});

			return rootView;
		}
	}
	protected void backToSplash() {
		// TODO Auto-generated method stub
		SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("notification_msg", "null");
		editor.commit();
		Intent in = new Intent(NotificationActivity.this, SplashScreen.class);
		startActivity(in);
		finish();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);	
	}
}
