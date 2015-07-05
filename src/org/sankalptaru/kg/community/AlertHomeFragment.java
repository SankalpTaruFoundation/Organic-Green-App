package org.sankalptaru.kg.community;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AlertHomeFragment extends DialogFragment {

	private String title; 
	private String message; 
	private String positivetxt;
	private String negativeTxt;
	private boolean setNegativeInvisible;
	private String positiveBtnLogicalHub;
	private HomeFragment homeFragment;

	public AlertHomeFragment(HomeFragment homeFragment, String title, String message, String positivetxt,String negativeTxt, boolean setNegativeInvisible, String positiveBtnLogicalHub) {
		// TODO Auto-generated constructor stub
		this.title=title;
		this.message=message;
		this.positivetxt=positivetxt;
		this.negativeTxt=negativeTxt;
		this.setNegativeInvisible=setNegativeInvisible;
		this.positiveBtnLogicalHub=positiveBtnLogicalHub;
		this.homeFragment=homeFragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		if(setNegativeInvisible){
			AlertDialog d = (AlertDialog) getDialog();
			Button negativeBtn = (Button)d.getButton(Dialog.BUTTON_NEGATIVE);
			negativeBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return new AlertDialog.Builder(getActivity())
		// Set Dialog Icon
		.setIcon(android.R.drawable.ic_dialog_alert)
		// Set Dialog Title
		.setTitle(title)
		// Set Dialog Message
		.setMessage(message)

		// Positive button
		.setPositiveButton(positivetxt, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Do something else
				Log.e("data", ""+positiveBtnLogicalHub);
				if(positiveBtnLogicalHub.equals("not_added")){
					homeFragment.postComments(null);
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
