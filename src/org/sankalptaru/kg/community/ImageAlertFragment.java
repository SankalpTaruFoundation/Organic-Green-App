package org.sankalptaru.kg.community;

import org.sankalptaru.kg.community.helper.TouchImageView;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

class ImageAlertFragment extends DialogFragment{

	String url;
	DisplayImageOptions options;
	ImageLoadingListener animateFirstListener;
	String titString;
	String descString;
	private int screenWidth;
	private int screenHeight;
	public ImageAlertFragment(String url, DisplayImageOptions options,
			ImageLoadingListener animateFirstListener,String title,String description, int screenWidth, int screenHeight) {
		// TODO Auto-generated constructor stub
		this.url=url;
		this.options=options;
		this.animateFirstListener=animateFirstListener;
		titString=title;
		descString=description;
		this.screenHeight=screenHeight;
		this.screenWidth=screenWidth;
	}
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.full_image_dialog, container,
				false);
		Window window = getDialog().getWindow();
		

		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		window.setBackgroundDrawableResource(android.R.color.transparent);
		window.setLayout(screenWidth, screenHeight);
		TouchImageView mainImage=(TouchImageView)rootView.findViewById(R.id.mainImage);
		ImageLoader.getInstance().displayImage(url, mainImage, options, animateFirstListener);

		TextView title=(TextView)rootView.findViewById(R.id.imageTitleText);
		title.setText(titString);

		TextView desc=(TextView)rootView.findViewById(R.id.imageDescText);
		desc.setText(descString);
		return rootView;
	}
}


