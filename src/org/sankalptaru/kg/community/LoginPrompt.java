package org.sankalptaru.kg.community;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class LoginPrompt extends Fragment {


	public LoginPrompt(){}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.login_prompt_main, container, false);

		RelativeLayout prmptLyt=(RelativeLayout)rootView.findViewById(R.id.loginPrmptLyt);
		Animation b=AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
		b.setDuration(1000);
		prmptLyt.startAnimation(b);
		prmptLyt.setVisibility(View.VISIBLE);

		Button btn=(Button)rootView.findViewById(R.id.createActBtn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent in =new Intent(getActivity(), LoginFragment.class);
				in.putExtra("pagename", "register");
				startActivity(in);
				getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				getActivity().finish();
			}
		});

		Button resetPswdBtn=(Button)rootView.findViewById(R.id.resetPswdBtn);
		resetPswdBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent in =new Intent(getActivity(), LoginFragment.class);
				in.putExtra("pagename", "reset_pswd");
				startActivity(in);
				getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				getActivity().finish();
			}
		});
		
		final RadioButton radio=(RadioButton)rootView.findViewById(R.id.stRadio);
		radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				Intent in =new Intent(getActivity(), LoginFragment.class);
				in.putExtra("pagename", "login");
				startActivity(in);
				getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				radio.setChecked(false);
				getActivity().finish();
			}
		});

		return rootView;
	}
}
