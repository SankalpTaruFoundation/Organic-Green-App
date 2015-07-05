package org.sankalptaru.kg.community;

import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class StoreFragment extends Fragment {

	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private Context context;

	public StoreFragment(Context context){
		this.context=context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_store, container, false);
		WebView storeWebVw=(WebView)rootView.findViewById(R.id.storeWeb);
		
		loadStore(storeWebVw);
		
		final LinearLayout loadingLyt=(LinearLayout)rootView.findViewById(R.id.webLoadingView);

		storeWebVw.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				loadingLyt.setVisibility(View.VISIBLE);
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				loadingLyt.setVisibility(View.INVISIBLE);
			}
		});

		return rootView;
	}

	private void loadStore(WebView storeWebVw) {
		// TODO Auto-generated method stub
		cd = new ConnectionDetector(context);

		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(getActivity(),
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			storeWebVw.loadUrl("http://store.sankalptaru.org/products");
		}

	}
}
