package org.sankalptaru.kg.community;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class PostsAdapter extends BaseAdapter {

	/*private	ArrayList<String> subjectList;*/
	private	 ArrayList<String> createdList;
	private	String gardenName;
	private	ArrayList<String> commentList;
	private	HashMap<String, String> cidImagePathMap;
	private Activity activity;
	private LayoutInflater inflater;
	DisplayImageOptions options;
	private ArrayList<String>cidList;
	private int screenHgth,screenWdth;
	private Typeface typface,typfaceProfile,typFaceDescription;
	ImageLoadingListener animateFirstListener;
	private ArrayList<Integer> likeCountList;

	android.app.FragmentManager  fm;
	private ConnectionDetector cd;
	private AlertDialogManager alert =new AlertDialogManager();
	private Context context;


	public PostsAdapter(DisplayImageOptions options, Activity activity, /*ArrayList<String> subjectList,*/
			ArrayList<String> createdList, String gardenName,
			ArrayList<String> commentList,
			HashMap<String, String> cidImagePathMap, ArrayList<String> cidList, int screenHeight, int screenWidth, Typeface typFace, Typeface typfaceProfile, Typeface typFaceDescription, android.app.FragmentManager fragmentManager, ImageLoadingListener animateFirstListener2, ArrayList<Integer> likeCountList) {
		// TODO Auto-generated constructor stub
		/*this.subjectList=subjectList;*/
		this.createdList=createdList;
		this.cidImagePathMap=cidImagePathMap;
		this.commentList=commentList;
		this.gardenName=gardenName;
		this.activity=activity;
		this.cidList=cidList;
		inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.options=options;
		screenHgth=screenHeight;
		screenWdth=screenWidth;
		typface=typFace;
		this.likeCountList=likeCountList;
		this.typFaceDescription=typFaceDescription;
		this.typfaceProfile=typfaceProfile;
		this.fm=fragmentManager;
		this.animateFirstListener=animateFirstListener2;
		context=activity;
	}

	public PostsAdapter(DisplayImageOptions options2, Activity activity2,
			ArrayList<String> subjectList2, ArrayList<String> createdList2,
			ArrayList<String> nameList2, ArrayList<String> commentList2,
			HashMap<String, String> cidImagePathMap2,
			ArrayList<String> cidList2, int screenHeight, int screenWidth,
			Typeface typFace2, Typeface typfaceProfile2,
			Typeface typFaceDescription2, FragmentManager fm2,
			ImageLoadingListener animateFirstListener2,
			ArrayList<Integer> likeCountList2, Activity activity3) {
		// TODO Auto-generated constructor stub
	}


	public int getCount() {
		return createdList.size();
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View vi=convertView;
		if(convertView==null)
			vi = inflater.inflate(R.layout.garden_list_item, null);
		//			holder = new ViewHolder();

		final TextView comment_likes=(TextView)vi.findViewById(R.id.comment_likes);
		comment_likes.setText(likeCountList.get(position)+" Likes");

		final TextView postLikeTxt=(TextView)vi.findViewById(R.id.postLikeTxt);
		postLikeTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				postLikeTxt.setEnabled(false);
				JSONObject jsonObject=new JSONObject();
				int localCount=likeCountList.get(position);
				try {
					localCount=localCount+1;
					jsonObject.put("field_like", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", ""+localCount))));

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SharedPreferences myPrefs = activity.getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
				myPrefs = activity.getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
				cd = new ConnectionDetector(context);

				// Check if Internet present
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					alert.showAlertDialog(activity,
							"Internet Connection Error",
							"Please connect to working Internet connection", false);
					// stop executing code by return
					return;
				}else{
					new UpdateLike(jsonObject,myPrefs){
						protected void onPostExecute(String result) {
							Log.e("data", ""+result+""+cidList.get(position));
							comment_likes.setText((likeCountList.get(position)+1)+" Likes");
						};

					}.execute("http://www1.kitchengardens.in/svc/comment/"+cidList.get(position));
				}
			}
		});


		TextView username = (TextView)vi.findViewById(R.id.profileUserName);
		username.setTypeface(typface);

		/*TextView title=(TextView)vi.findViewById(R.id.profilePostTitle);
		title.setTypeface(typfaceProfile);
		title.setTextColor(Color.parseColor("#3a8300"));*/

		TextView description =(TextView)vi.findViewById(R.id.profilePostDescrip);
		description.setTypeface(typfaceProfile);

		ImageView image=(ImageView)vi.findViewById(R.id.profilePostImg);
		image.getLayoutParams().height = (int) Math.floor(screenHgth/2.5);
		image.getLayoutParams().width=screenWdth;


		username.setText(Html.fromHtml("<b>"+gardenName+"</b>"+" shared on "+AppUtil.getDate(Long.parseLong(createdList.get(position)))));


//		title.setText(subjectList.get(position));


		description.setText(commentList.get(position));


		final String token =cidImagePathMap.get(cidList.get(position));

		TextView postShareTxt=(TextView)vi.findViewById(R.id.postShareTxt);
		postShareTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				/*sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subjectList.get(position));*/
				if(token!=null){
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hey,\nHere is my garden, "+commentList.get(position)+"\n"+"http://store.sankalptaru.org/sites/default/files/"+token+"\n\nPowered by SankalpTaru Organic Greens. All Rights Reserved.");
				}
				else{
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, commentList.get(position));
				}
				activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
			}
		});

		if(token==null){
			//			token="none.jpg";
			image.setVisibility(View.GONE);
		}
		else{
			image.setVisibility(View.VISIBLE);
			final String url="http://www1.kitchengardens.in/sites/default/files/"+token;
			ImageLoader.getInstance().displayImage(url, image, options, animateFirstListener);
			image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					ImageAlertFragment imageFragment=new ImageAlertFragment(url,options,animateFirstListener,"Update",commentList.get(position),screenWdth,screenHgth);
					imageFragment.setCancelable(true);
					imageFragment.show(fm, "");
				}
			});
		}


		return vi;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
