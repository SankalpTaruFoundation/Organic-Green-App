package org.sankalptaru.kg.community;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class AlertloginFragment extends DialogFragment {

	private CharSequence title;
	private String positiveBtnTxt;
	private String negativeTxt;
	private CharSequence message;
	private String reason;
	private Button negativeBtn;
	private String className;
	private LoginSubFragment loginSubFragment;
	private RegisterFragment registerFragment;

	public AlertloginFragment(LoginSubFragment loginSubFragment,RegisterFragment registerFragment, String className,String title,String message,String positiveBtnTxt,String negativeTxt, String reason){

		this.title=title;
		this.message=message;
		this.positiveBtnTxt=positiveBtnTxt;
		this.negativeTxt=negativeTxt;
		this.reason=reason;
		this.className=className;
		this.loginSubFragment=loginSubFragment;
		this.registerFragment=registerFragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		AlertDialog d = (AlertDialog) getDialog();

		negativeBtn = (Button)d.getButton(Dialog.BUTTON_NEGATIVE);
		negativeBtn.setVisibility(View.GONE);

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
				if(reason.equals("null_response")){
					loginSubFragment.resetLoginComponents();
				}
				if(className.equals("Register")){
					registerFragment.resetRegisterComponents();
				}
				if(className.equals("Register")&&reason.equals("success")){
					registerFragment.goToLoginPage();
				}
			}
		})

		// Negative Button
		.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,    int which) {
				// Do something else

			}
		}).create();

	}
}