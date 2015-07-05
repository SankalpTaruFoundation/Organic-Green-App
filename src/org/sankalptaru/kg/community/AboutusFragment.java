package org.sankalptaru.kg.community;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutusFragment extends Fragment {

	public AboutusFragment(){}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_aboutus, container, false);
		TextView foo = (TextView)rootView.findViewById(R.id.aboutusDesc);
		foo.setText(Html.fromHtml(getString(R.string.aboutus)));
		
		return rootView;
	}

}
