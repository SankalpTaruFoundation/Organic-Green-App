package org.sankalptaru.kg.community;

import org.json.JSONArray;
import org.json.JSONException;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class CommunityFragment extends Fragment {

	private SwipeRefreshLayout swipeLayout;
	private MainActivity mainActivity;
	private ConnectionDetector cd;
	private AlertDialogManager alert =new AlertDialogManager();
	private Context context;
	public CommunityFragment(MainActivity mainActivity, Context context){
		this.mainActivity=mainActivity;
		this.context=context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_community, container, false);

		final GridView communityGrid=(GridView)rootView.findViewById(R.id.grid_community);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);

		swipeLayout.setEnabled(false);
		
		loadCommunity(communityGrid);
		return rootView;
	}
	private void loadCommunity(final GridView communityGrid) {
		// TODO Auto-generated method stub
		cd = new ConnectionDetector(context);

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert .showAlertDialog(getActivity(),
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			new CSRFToken(){
				protected void onPreExecute() {swipeLayout.setRefreshing(true);};
				protected void onPostExecute(String result) {
					try {
						JSONArray gardenJSArray=new JSONArray(result);
						communityGrid.setAdapter(new GridAdapter(gardenJSArray));
						swipeLayout.setRefreshing(false);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			}.execute("http://store.sankalptaru.org/svc/views/gardens/");
		}
	}
	private class GridAdapter extends BaseAdapter{

		JSONArray gardenJSArray;
		private LayoutInflater inflater;
		private String name;
		private String count;
		public GridAdapter(JSONArray gardenJSArray) {
			// TODO Auto-generated constructor stub
			this.gardenJSArray=gardenJSArray;
			inflater=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return gardenJSArray.length();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View vi=convertView;
			if(convertView==null)
				vi = inflater.inflate(R.layout.community_grid_item, null);

			TextView profileName=(TextView)vi.findViewById(R.id.profileName);

			TextView countTxt=(TextView)vi.findViewById(R.id.commentCount);

			try {
				count = gardenJSArray.getJSONObject(position).optString("node_comment_statistics_comment_count");
				name=gardenJSArray.getJSONObject(position).optString("node_revision_title");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Button viewProfileBtn=(Button)vi.findViewById(R.id.view_garden_btn);
			viewProfileBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						mainActivity.renderKGWallFragment(gardenJSArray.getJSONObject(position).optString("node_revision_title"),gardenJSArray.getJSONObject(position).optString("nid"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			profileName.setText(Html.fromHtml(name));
			countTxt.setText(count+" Comments");

			return vi;
		}
	}
}
