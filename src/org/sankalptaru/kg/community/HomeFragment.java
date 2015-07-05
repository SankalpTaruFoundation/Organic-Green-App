package org.sankalptaru.kg.community;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.CalenderHelper;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class HomeFragment extends Fragment{
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		SharedPreferences pref = getActivity().getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("custom_garden_id", "none");
		editor.commit();

	}
	private static final int RESULT_LOAD_POST_IMAGE = 2;
	protected static final int REQUEST_CODE_COVER_CLICK_IMAGE = 3;
	private static final int REQUEST_CODE_POST_CLICK_IMAGE = 4;
	//	MainActivity mainActivity;
	private ActionBar actionBar;
	private int screenHeight,screenWidth;
	private ImageView change_cover_photo;
	private ImageView coverPhoto;
	private SharedPreferences myPrefs;
	private TextView mProfileNameTxt;
	private Typeface typFace;
	private String garden_node_id;
	private ListView postsListView;
	private static int RESULT_LOAD_COVER_IMAGE = 1;
	//	DisplayImageOptions options;
	private String gardenName;
	private Typeface typfaceProfile;
	private Typeface typFaceDescription;
	private android.support.v4.app.FragmentManager fm;
	private TextView postImageStatus;
	private String sharedImagePath=null;
	private Random rand;
	//	private String commentSubject;
	private String commentStr;
	private Dialog progressDialog;
	//	private EditText subject;
	private String cameraImagePath;
	private EditText comment;
	private TextView noPostsTxtView;
	private boolean isRefreshEnabled;
	private AsyncTask<String, Integer, JSONObject> taskPostImage;
	private AsyncTask<String, Integer, JSONObject> taskPostComment;
	private AsyncTask<String, Integer, JSONObject> taskFetchNode;
	private AsyncTask<String, Integer, JSONObject> taskFetcAllComments;
	private String coverPhotoName=null;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private SwipeRefreshLayout swipeLayout;
	private ProgressBar mProgress;
	private int coverPhoto_height;
	private AlertDialogManager alert = new AlertDialogManager();
	private ConnectionDetector cd;
	//	private Context context;
	private StatusAlert statusAlert;
	private View homeFrgRootView;
	private boolean updateHeader=false;

	public boolean isRefreshEnabled() {
		return isRefreshEnabled;
	}


	public void setRefreshEnabled(boolean isRefreshEnabled) {
		this.isRefreshEnabled = isRefreshEnabled;
	}

	public HomeFragment(FragmentManager fragmentManager){
		fm=fragmentManager;
	}

	public HomeFragment(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		/*	AppUtil.SuperRestartServiceIntent = PendingIntent.getActivity(
				mainActivity.getBaseContext(), 0,
				new Intent(mainActivity.getIntent()), mainActivity.getIntent().getFlags());*/

		homeFrgRootView = inflater.inflate(R.layout.fragment_home, container, false);

		actionBar = getActivity().getActionBar();

		if(!actionBar.isShowing()){
			actionBar.show();
		}
		swipeLayout = (SwipeRefreshLayout) homeFrgRootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);

		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				if(isRefreshEnabled()){
					setRefreshEnabled(false);
					fetchGardenDetails(homeFrgRootView,true);
				}
			}
		});

		rand=new Random(1000);

		initImageLoader(getActivity());

		/*ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();*/


		mProgress=(ProgressBar)homeFrgRootView.findViewById(R.id.profileProgress);

		noPostsTxtView=(TextView)homeFrgRootView.findViewById(R.id.noPoststxt);

		setRefreshEnabled(false);

		ImageView mRefreshImg=(ImageView)homeFrgRootView.findViewById(R.id.refreshImg);
		mRefreshImg.setVisibility(View.GONE);
		/*mRefreshImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(isRefreshEnabled()){
					setRefreshEnabled(false);
					fetchGardenDetails();
				}
			}
		});*/

		ImageView openStatusDialog=(ImageView)homeFrgRootView.findViewById(R.id.openStatusDialog);
		openStatusDialog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				statusAlert =new StatusAlert();
				statusAlert.setCancelable(true);
				statusAlert.show(fm, "status_open");
			}
		});


		typFace=Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Medium.ttf");
		typFaceDescription=Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		typfaceProfile=Typeface.createFromAsset(getActivity().getAssets(), "Roboto-BoldItalic.ttf");

		postsListView=(ListView)homeFrgRootView.findViewById(R.id.profile_post_list);

		final RelativeLayout statuslyt=(RelativeLayout)homeFrgRootView.findViewById(R.id.statusLyt);



		postsListView.setOnScrollListener(new OnScrollListener() {
			private int mLastFirstVisibleItem;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if(firstVisibleItem==0){
					swipeLayout.setEnabled(true);
				}else{
					swipeLayout.setEnabled(false);
				}

				if(mLastFirstVisibleItem<firstVisibleItem)
				{
					//		                Log.i("SCROLLING DOWN","TRUE");
					Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
					statuslyt.startAnimation(slideUp);

				}
				if(mLastFirstVisibleItem>firstVisibleItem)
				{
					//		                Log.i("SCROLLING UP","TRUE");
				}
				mLastFirstVisibleItem=firstVisibleItem;
			}
		});

		fetchGardenDetails(homeFrgRootView,false);

		//		statusLayoutHandle(rootView);

		return homeFrgRootView;
	}

	private void statusLayoutHandle(View rootView) {
		// TODO Auto-generated method stub
		//		subject =(EditText)rootView.findViewById(R.id.statusSubjectTxt);
		comment = (EditText)rootView.findViewById(R.id.statusCommentTxt);

		ImageView sendBtn =(ImageView)rootView.findViewById(R.id.postStatus);
		ImageView addPostImage =(ImageView)rootView.findViewById(R.id.addPostImage);
		ImageView addPostCamImage =(ImageView)rootView.findViewById(R.id.addPostCamImage);

		postImageStatus = (TextView)rootView.findViewById(R.id.postImageStatus);
		postImageStatus.setTypeface(typFace);
		postImageStatus.setText("Add Image");

		TextView sendLabel = (TextView)rootView.findViewById(R.id.sendLabel);
		sendLabel.setTypeface(typFace);
		sendLabel.setText("Post");

		addPostCamImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveCameraImage(false);
			}
		});
		addPostImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				if(isAdded())
					startActivityForResult(i, RESULT_LOAD_POST_IMAGE);
				else{
					statusAlert.dismiss();
					Toast.makeText(getActivity(), "Something went wrong.Try sharing your updates again.",  1000).show();
				}
			}
		});

		sendBtn.setOnClickListener(new OnClickListener() {



			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isAdded()){
					if(comment.getText().length()>0){

						//						commentSubject=subject.getText().toString().trim();
						commentStr=comment.getText().toString().trim();
						if(sharedImagePath==null){
							AlertHomeFragment dialog=new AlertHomeFragment(HomeFragment.this,"Info","Image not added to post.\n\nNote: Sharing your image with your status is recommended.","Proceed Anyway","Cancel",false,"not_added");
							dialog.setCancelable(true);
							dialog.show(fm, "post");
						}
						else{
							myPrefs = getActivity().getSharedPreferences("myPrefs",getActivity().MODE_PRIVATE);
							doPostCommentsActions(myPrefs);
						}
					}else{
						AlertHomeFragment dialog=new AlertHomeFragment(HomeFragment.this,"Warning","All fields are mandatory to be filled.","OK","Proceed",true,"field_check");
						dialog.setCancelable(true);
						dialog.show(fm, "field");
					}
				}
				else{
					statusAlert.dismiss();
					Toast.makeText(getActivity(), "Something went wrong.Try sharing your updates again.",  1000).show();
				}
			}
		});
	}
	private void doPostCommentsActions(SharedPreferences myPrefs) {
		// TODO Auto-generated method stub
		JSONObject js= new JSONObject();

		File imageFile = new File(sharedImagePath);
		if(imageFile.exists()){
			String fileName=System.currentTimeMillis()+""+rand.nextInt()+".jpeg";

			try {
				js.put("filename", fileName);
				js.put("uid",myPrefs.getInt("uid", -1));
				js.put("file", AppUtil.getBase64StringOfFile(imageFile));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cd = new ConnectionDetector(getActivity());

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(getActivity(),
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				taskPostImage=new PostImage(js,myPrefs){
					protected void onPreExecute() {
						AppUtil.initializeProgressDialog(getActivity(), "Uploading Image...", progressDialog);
					};

					protected void onPostExecute(JSONObject result) {
						if(null!=result){
							AppUtil.cancelProgressDialog();
							postComments(result.optString("fid"));
						}
					};
				}.execute("http://www1.kitchengardens.in/svc/file/");
			}
		}

	}



	private void fetchGardenDetails(View rootView, boolean isServerRefersh) {
		// TODO Auto-generated method stub

		myPrefs = getActivity().getSharedPreferences("myPrefs",getActivity().MODE_PRIVATE); 
		String custom_id=myPrefs.getString("custom_garden_id", "none");
		String ownerGardenId=myPrefs.getString("garden_id", "none");

		if(!custom_id.equals("none")){
			garden_node_id=custom_id;
			final RelativeLayout statuslyt=(RelativeLayout)rootView.findViewById(R.id.statusLyt);
			statuslyt.setVisibility(View.GONE);
			isServerRefersh=true;
			/*SharedPreferences pref = mainActivity.getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("custom_garden_id", "none");
			editor.commit();*/
		}else{
			garden_node_id = ownerGardenId;
		}

		File nodeFile=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+"garden_node.txt");
		File commentFile =new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+"garden_node_comments.txt");


		if(isServerRefersh){
			initiateRefreshFromSever(nodeFile,commentFile);
		}else{
			initiateFromSavedData(nodeFile,commentFile);
		}
	}

	private void initiateFromSavedData(File nodeFile, File commentFile) {
		// TODO Auto-generated method stub
		if(nodeFile.exists() && commentFile.exists()){

			try {
				populatePostLayout(new JSONObject(AppUtil.readFile(nodeFile)));

				ArrayList<JSONObject> commentsObjectsList =null;

				commentsObjectsList=getCommentsFromJO(new JSONObject(AppUtil.readFile(commentFile)),commentsObjectsList);

				processCommentResponseAndStoreInLists(commentsObjectsList);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			initiateRefreshFromSever(nodeFile,commentFile);
		}
	}


	private void initiateRefreshFromSever(File nodeFile, File commentFile) {
		// TODO Auto-generated method stub
		if(myPrefs.getString("custom_garden_id", "none").equals("none")){
			if(nodeFile.exists()){
				nodeFile.delete();
			}
			if(commentFile.exists())
				commentFile.delete();
		}

		if(!garden_node_id.equals("none")){
			swipeLayout.setRefreshing(true);
			cd = new ConnectionDetector(getActivity());

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(getActivity(),
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				taskFetchNode=new FetchGardenNodeDetails().execute("http://www1.kitchengardens.in/svc/node/"+garden_node_id);
			}
		}
		else{
			Toast.makeText(getActivity(), "Unable to fetch garden details.", 2000).show();
		}
	}


	public void postComments(String fid) {
		// TODO Auto-generated method stub
		JSONObject jsObject = new JSONObject();
		String nid=myPrefs.getString("garden_id", "none");

		try {
			jsObject.put("nid", nid);
			//			jsObject.put("subject", commentSubject);
			jsObject.put("comment_body", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", commentStr))));
			if(null!=fid)
				jsObject.put("field_image", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("fid", fid))));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		cd = new ConnectionDetector(getActivity());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(getActivity(),
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			taskPostComment=new PostComment(jsObject,myPrefs){

				protected void onPreExecute() {
					AppUtil.initializeProgressDialog(getActivity(), "Finalizing post...", progressDialog);
				};

				protected void onPostExecute(JSONObject result) {
					AppUtil.cancelProgressDialog();
					CalenderHelper calenderHelper=new CalenderHelper(getActivity());
					Calendar cal=Calendar.getInstance();
					Date date=new Date();
					calenderHelper.createEvent("", "Status:"+commentStr, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND),cal.get(Calendar.MILLISECOND),date.getTime(),date.getTime(),null);
					//					subject.setText("");
					comment.setText("");
					sharedImagePath=null;
					Toast.makeText(getActivity(), "Post Successful", 1000).show();
					statusAlert.dismiss();
					if(isRefreshEnabled()){
						setRefreshEnabled(false);
						fetchGardenDetails(homeFrgRootView,true);
					}
				};
			}.execute("http://www1.kitchengardens.in/svc/comment/");
		}
	}



	private class FetchGardenNodeDetails extends AsyncTask<String, Integer, JSONObject>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			// TODO Auto-generated method stub
			JSONObject jsResponse = null;
			try {
				jsResponse=new JSONObject(AppUtil.getResponse(params[0]));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsResponse;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				saveGardenNodeJSONInSdcard(result);
				populatePostLayout(result);
				cd = new ConnectionDetector(getActivity());

				// Check if Internet present
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					alert.showAlertDialog(getActivity(),
							"Internet Connection Error",
							"Please connect to working Internet connection", false);
					// stop executing code by return
					return;
				}else{
					taskFetcAllComments=new FetchAllComments().execute("http://www1.kitchengardens.in/svc/node/"+garden_node_id+"/comments");
				}
			}
		}
	}

	private class FetchAllComments extends AsyncTask<String, Integer, JSONObject>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			// TODO Auto-generated method stub
			JSONObject jsResponse=null;
			try {
				jsResponse=new JSONObject(AppUtil.getResponse(params[0]));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsResponse;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (null != result) {

				ArrayList<JSONObject> commentsObjectsList =null;

				saveGardenNodeComments(result);

				commentsObjectsList=getCommentsFromJO(result,commentsObjectsList);

				processCommentResponseAndStoreInLists(commentsObjectsList);
			}
			else{
				//				mProgress.setVisibility(View.INVISIBLE);
				swipeLayout.setRefreshing(false);
				noPostsTxtView.setVisibility(View.VISIBLE);
				setRefreshEnabled(true);
			}
		}

	}

	private void processCommentResponseAndStoreInLists(ArrayList<JSONObject> commentsObjectsList) {
		// TODO Auto-generated method stub
		//		ArrayList<String> subjectList=new ArrayList<String>();
		ArrayList<String> createdList =new ArrayList<String>();
		ArrayList<String> nameList=new ArrayList<String>();
		ArrayList<String> commentList =new ArrayList<String>();
		ArrayList<String> cidList=new ArrayList<String>();
		ArrayList<Integer> likeCountList=new ArrayList<Integer>();
		HashMap<String, String> cidImagePathMap =new HashMap<String, String>();


		for (int i = 0; i < commentsObjectsList.size(); i++) {

			JSONObject jsonObject = commentsObjectsList.get(i);

			//			String subject = jsonObject.optString("subject");
			String created = jsonObject.optString("created");
			String name =jsonObject.optString("name");
			String comment =jsonObject.optJSONObject("comment_body").optJSONArray("und").optJSONObject(0).optString("value");

			String cid =jsonObject.optString("cid");

			String imageName = null;


			if(jsonObject.optJSONObject("field_image")!=null){
				imageName = jsonObject.optJSONObject("field_image").optJSONArray("und").optJSONObject(0).optString("filename");
			}

			if(jsonObject.optJSONObject("field_like")!=null){
				likeCountList.add(Integer.parseInt(jsonObject.optJSONObject("field_like").optJSONArray("und").optJSONObject(0).optString("value")));
			}else{
				likeCountList.add(0);
			}

			//			subjectList.add(subject);
			createdList.add(created);
			nameList.add(name);
			commentList.add(comment);
			cidList.add(cid);
			cidImagePathMap.put(cid, imageName);
		}

		if(postsListView.getHeaderViewsCount()==0){
			View headerView=getActivity().getLayoutInflater().inflate(R.layout.post_list_header, null);
			postsListView.addHeaderView(headerView);
			coverPhotoActions(headerView);
			mProfileNameTxt=(TextView)headerView.findViewById(R.id.gardenNameTxt);
			mProfileNameTxt.setTypeface(typfaceProfile);
		}

		mProfileNameTxt.setText(gardenName);
		actionBar.setTitle(gardenName);
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(1000))
		.build();
		postsListView.setAdapter(new PostsAdapter(options,getActivity(),createdList,gardenName,commentList,cidImagePathMap,cidList,screenHeight,screenWidth,
				typFace,typfaceProfile,typFaceDescription,getFragmentManager(),animateFirstListener,likeCountList));
		swipeLayout.setRefreshing(false);
		//		mProgress.setVisibility(View.INVISIBLE);
		infalteImageOnCoverPhoto(coverPhotoName);
		if(noPostsTxtView.getVisibility()==View.VISIBLE){
			noPostsTxtView.setVisibility(View.INVISIBLE);
		}

		setRefreshEnabled(true);
	}

	public void saveGardenNodeJSONInSdcard(JSONObject result) {
		// TODO Auto-generated method stub
		if(myPrefs.getString("custom_garden_id", "none").equals("none")){
			AppUtil.createFolderInSDCard();
			AppUtil.createFile(AppUtil.JSON_FILE_PATH+"garden_node.txt");
			AppUtil.writeToFile(result.toString(), AppUtil.JSON_FILE_PATH+"garden_node.txt");
		}
	}


	public void saveGardenNodeComments(JSONObject result) {
		// TODO Auto-generated method stub
		if(myPrefs.getString("custom_garden_id", "none").equals("none")){
			AppUtil.createFolderInSDCard();
			AppUtil.createFile(AppUtil.JSON_FILE_PATH+"garden_node_comments.txt");
			AppUtil.writeToFile(result.toString(), AppUtil.JSON_FILE_PATH+"garden_node_comments.txt");
		}
	}


	private ArrayList<JSONObject> getCommentsFromJO(JSONObject result, ArrayList<JSONObject> commentsObjectsList){

		commentsObjectsList=new ArrayList<JSONObject>();
		Iterator<?> keys = result.keys();
		ArrayList<Integer> keyList=new ArrayList<Integer>();
		while( keys.hasNext() ){
			String key = (String)keys.next();
			keyList.add(Integer.parseInt(key));
		}
		Collections.sort(keyList);
		Collections.reverse(keyList);
		for (int i = 0; i < keyList.size(); i++) {
			commentsObjectsList.add(result.optJSONObject(""+keyList.get(i)));
		}
		return commentsObjectsList;
	}

	private void coverPhotoActions(View rootView){
		getScreenMetrics();
		ImageView edit_profile=(ImageView)rootView.findViewById(R.id.edit_profile_btn);
		edit_profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				openInterestDialog();
			}
		});

		ImageView changeCoverByCamBtn =(ImageView)rootView.findViewById(R.id.chnge_cover_by_camerabtn);
		changeCoverByCamBtn.setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveCameraImage(true);
			}
		});


		coverPhoto =(ImageView)rootView.findViewById(R.id.coverPhoto);
		coverPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(coverPhotoName!=null){
					DisplayImageOptions  options = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.ic_stub)
					.showImageForEmptyUri(R.drawable.ic_empty)
					.showImageOnFail(R.drawable.ic_error)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.considerExifParams(true)
					.displayer(new FadeInBitmapDisplayer(1000))
					.build();
					String url = "http://www1.kitchengardens.in/sites/default/files/"+coverPhotoName;
					getScreenMetrics();
					ImageAlertFragment imageFragment=new ImageAlertFragment(url,options,animateFirstListener,gardenName.split(" Profile")[0]+" Cover Photo","",screenWidth,screenHeight);
					imageFragment.setCancelable(true);
					imageFragment.show(getFragmentManager(), "");
				}
			}
		});
		coverPhoto_height=coverPhoto.getLayoutParams().height=(int) Math.floor(screenHeight/3.5);

		change_cover_photo=(ImageView)rootView.findViewById(R.id.chnge_cvr_pht);

		if(!myPrefs.getString("custom_garden_id", "none").equals("none")){
			change_cover_photo.setVisibility(View.GONE);
			changeCoverByCamBtn.setVisibility(View.GONE);
			edit_profile.setVisibility(View.GONE);
		}

		/*myPrefs = mainActivity.getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		String cover_photo_path= myPrefs.getString("cover_photo_path", "none");
		if(!cover_photo_path.equals("none")){
			File f=new File(cover_photo_path);
			if(f.exists()){
				Bitmap bitmap=Bitmap.createScaledBitmap(BitmapFactory.decodeFile(cover_photo_path), screenWidth, coverPhoto_height, true);
				coverPhoto.setImageBitmap(bitmap);
			}
		}*/


		change_cover_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_COVER_IMAGE);

			}
		});
	}

	protected void saveCameraImage(boolean isCover) {
		// TODO Auto-generated method stub
		File newdir =new File(Environment.getExternalStorageDirectory().getPath(), "/Organic Greens/Images");
		if(!newdir.exists())
			newdir.mkdirs();

		cameraImagePath=Environment.getExternalStorageDirectory().getPath()+ "/Organic Greens/Images/"+new Date().getTime()+".jpeg";
		File newfile = new File(cameraImagePath);
		try {
			newfile.createNewFile();
		} catch (IOException e) {}       

		Uri outputFileUri = Uri.fromFile(newfile);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		if(isCover)
			startActivityForResult(intent, REQUEST_CODE_COVER_CLICK_IMAGE);
		else
			startActivityForResult(intent, REQUEST_CODE_POST_CLICK_IMAGE);

	}


	private void infalteImageOnCoverPhoto(String fileName) {
		// TODO Auto-generated method stub
		if(fileName!=null){
			DisplayImageOptions  options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.displayer(new com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer(2, 2))
			.build();
			coverPhotoName=fileName;
			String url="http://www1.kitchengardens.in/sites/default/files/"+fileName;
			ImageLoader.getInstance().displayImage(url, coverPhoto, options, animateFirstListener);
		}
	}


	public void populatePostLayout(JSONObject result) {
		// TODO Auto-generated method stub
		gardenName=result.optString("title_original");

		SharedPreferences pref = getActivity().getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("discussion_user", gardenName);
		editor.commit();

		JSONObject cityJSObject=result.optJSONObject("field_city");

		JSONObject countryJSObject=result.optJSONObject("field_garden_country");

		if((null==cityJSObject || null==countryJSObject) && myPrefs.getString("custom_garden_id", "none").equals("none")){
			openInterestDialog();
		}

		if(result.optJSONObject("field_image")!=null)
			coverPhotoName=result.optJSONObject("field_image").optJSONArray("und").optJSONObject(0).optString("filename");

		//		mProfileNameTxt.setTypeface(typFace);
		//		mProfileNameTxt.setText(gardenName);
	}
	public void getScreenMetrics(){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == REQUEST_CODE_POST_CLICK_IMAGE  && resultCode == getActivity().RESULT_OK){
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			compressCamBitmap(cameraImagePath);
		}
		else if(requestCode == REQUEST_CODE_COVER_CLICK_IMAGE  && resultCode == getActivity().RESULT_OK){
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			setCoverPhotoImage(cameraImagePath);
		}
		if (requestCode == RESULT_LOAD_COVER_IMAGE && resultCode == getActivity().RESULT_OK && null != data) {
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			setCoverPhotoImage(AppUtil.getGalleryPath(data,getActivity()));
		}
		else if(requestCode == RESULT_LOAD_POST_IMAGE && resultCode == getActivity().RESULT_OK && null != data){
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			compressCamBitmap(AppUtil.getGalleryPath(data,getActivity()));
			postImageStatus.setText("Added");
		}
	}

	private void compressCamBitmap(String imagePath) {
		// TODO Auto-generated method stub
		new ImageCompressionAsyncTask(true,getActivity()){
			protected void onPostExecute(String result) {
				sharedImagePath=result;
				if(cameraImagePath!=null){
					File tempFile=new File(cameraImagePath);
					tempFile.delete();
					cameraImagePath=null;
				}
			};

		}.execute(imagePath);
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	private void setCoverPhotoImage(final String picturePath){
		final File f=new File(picturePath);
		if(f.exists()){
			/*SharedPreferences myPrefs = getActivity().getSharedPreferences("myPrefs",getActivity().MODE_PRIVATE); 

			String citString=myPrefs.getString("user_city", "none");
			String intString=myPrefs.getString("user_interest", "none");

			if(!citString.equals("none") && !intString.equals("none")){
				JSONObject jsObject = new JSONObject();
				try {
					jsObject.put("field_city", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", citString))));
					jsObject.put("field_user_status", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", intString))));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cd = new ConnectionDetector(getActivity());
				// Check if Internet present
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					alert.showAlertDialog(getActivity(),
							"Internet Connection Error",
							"Please connect to working Internet connection", false);
					// stop executing code by return
					return;
				}else{
					new UpdateLike(jsObject,myPrefs){
						protected void onPreExecute() {
							AppUtil.initializeProgressDialog(getActivity(), "Connecting...", progressDialog);
						};
						protected void onPostExecute(String result) {
							AppUtil.cancelProgressDialog();
							getImageFid(f,picturePath);
						};

					}.execute("http://www1.kitchengardens.in/svc/node/"+garden_node_id);
				}
			}else{*/
			getImageFid(f,picturePath);
			/*}*/
		}
	}

	private void getImageFid(File f, String picturePath){
		final SharedPreferences myPrefs = getActivity().getSharedPreferences("myPrefs",getActivity().MODE_PRIVATE); 
		final JSONObject js= new JSONObject();
		Random rand=new Random();
		final String fileName=System.currentTimeMillis()+""+rand.nextInt()+".jpeg";
		new ImageCompressionAsyncTask(true,getActivity()){
			protected void onPostExecute(String result) {
				try {
					if(cameraImagePath!=null){
						File tempFile=new File(cameraImagePath);
						tempFile.delete();
						cameraImagePath=null;
					}
					js.put("filename", fileName);
					js.put("uid",myPrefs.getInt("uid", -1));
					js.put("file", AppUtil.getBase64StringOfFile(new File(result)));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cd = new ConnectionDetector(getActivity());

				// Check if Internet present
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					alert.showAlertDialog(getActivity(),
							"Internet Connection Error",
							"Please connect to working Internet connection", false);
					// stop executing code by return
					return;
				}else{
					new PostImage(js,myPrefs){
						protected void onPreExecute() {
							AppUtil.initializeProgressDialog(getActivity(), "Uploading Image...", progressDialog);
						};

						protected void onPostExecute(JSONObject result) {
							if(null!=result){
								AppUtil.cancelProgressDialog();
								postImage(result.optString("fid"),fileName);
							}
						}
					}.execute("http://www1.kitchengardens.in/svc/file/");
				}
			};

		}.execute(picturePath);
	}

	private void postImage(String fid, final String fileName) {
		// TODO Auto-generated method stub
		JSONObject jsObject = new JSONObject();
		SharedPreferences myPrefs = getActivity().getSharedPreferences("myPrefs",getActivity().MODE_PRIVATE);
		try {

			jsObject.put("field_image", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("fid", fid))));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cd = new ConnectionDetector(getActivity());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(getActivity(),
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			new UpdateLike(jsObject,myPrefs){
				protected void onPreExecute() {
					AppUtil.initializeProgressDialog(getActivity(), "Finalizing post...", progressDialog);
				};
				protected void onPostExecute(String result) {
					AppUtil.cancelProgressDialog();
					infalteImageOnCoverPhoto(fileName);
				};
			}.execute("http://www1.kitchengardens.in/svc/node/"+garden_node_id);
		}
	}
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.diskCacheSize(50 * 1024 * 1024) // 50 Mb
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	class StatusAlert extends DialogFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub

			View rootView = inflater.inflate(R.layout.status_update_layout, container,
					false);
			getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
			getDialog().setTitle("Post Status Update"); 

			statusLayoutHandle(rootView);

			return rootView;
		}

	}


	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();

		if(taskFetcAllComments!=null&&taskFetcAllComments.getStatus()==AsyncTask.Status.RUNNING){
			taskFetcAllComments.cancel(true);
		}

		if(taskFetchNode!=null&&taskFetchNode.getStatus()==AsyncTask.Status.RUNNING){
			taskFetchNode.cancel(true);
		}

		if(taskPostComment!=null&&taskPostComment.getStatus()==AsyncTask.Status.RUNNING){
			taskPostComment.cancel(true);
		}

		if(taskPostImage!=null&&taskPostImage.getStatus()==AsyncTask.Status.RUNNING){
			taskPostImage.cancel(true);
		}
	}
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
	private class GetUserDetailsDialog extends android.support.v4.app.DialogFragment{
		/*	String cityNames="Ambala,Achalpur,Achhnera,Adalaj,Adilabad,Adityapur,Adoni,Adoor,Adyar,Adra,Afzalpur,Agartala,Agra,Ahmedabad,Ahmednagar,Aizawl,Ajmer,Akola,Akot,Alappuzha,Aligarh,Alipurduar,Allahabad,Alwar,Amalapuram,Amalner,Ambejogai,Ambikapur,Amravati,Amreli,Amritsar,Amroha,Anakapalle,Anand,Anantapur,Anantnag,Anjar,Anjangaon,Ankleshwar,Arakkonam,Araria,Arambagh,Arsikere,Arrah,Aruppukkottai,Arvi,Arwal,Asansol,Asarganj,Ashok Nagar,Athni,Aurangabad,Aurangabad,Azamgarh,Bikaner,Bhiwandi,Bagaha,Bageshwar,Bahadurgarh,Baharampur,Bahraich,Balaghat,Balangir,Baleshwar Town,Bengaluru,Bankura,Bapatla,Baramula,Barbil,Bargarh,Barh,Baripada Town,Barnala,Barpeta,Batala,Bathinda,Begusarai,Belagavi,Bellampalle,Ballari,Belonia,Bettiah,Bhabua,Bhadrachalam,Bhadrak,Bhagalpur,Bhainsa,Bharuch,Bhatapara,Bhavnagar,Bhawanipatna,Bheemunipatnam,Bhilai Nagar,Bhilwara,Bhimavaram,Bhiwani,Bhongir,Bhopal,Bhubaneswar,Bhuj,Bilaspur,Bobbili,Bodhan,Bokaro Steel City,Bongaigaon City,Brahmapur,Buxar,Byasanagar,Chaibasa,Chandigarh,Charkhi Dadri,Chatra,Chalakudy,Changanassery,Chennai,Cherthala,Chikkamagaluru,Chhapra,Chilakaluripet,Chirala,Chirkunda,Chirmiri,Chittur-Thathamangalam,Chittoor,Coimbatore,Cuttack,Dewas,Dalli-Rajhara,Medininagar (Daltonganj),Darbhanga,Darjiling,Davanagere,Deesa,Dehradun,Dehri-on-Sone,Delhi,Deoghar,Dhamtari,Dhanbad,Dharmanagar,Dharmavaram,Dhenkanal,Dhoraji,Dhubri,Dhule,Dhuri,Dibrugarh,Dimapur,Diphu,Kalyan-Dombivali,Dumka,Dumraon,Durg,Faridkot,Firozabad,Farooqnagar,Fatehabad,Fazilka,Forbesganj,Firozpur,Firozpur Cantt.,Fatehpur Sikri,Ganjbasoda,Gaya,Giridih,Goalpara,Gobichettipalayam,Gobindgarh,Godhra,Gohana,Gokak,Gooty,Gopalganj,Gudivada,Gudur,Kalaburagi,Gumia,Guntakal,Guntur,Gurdaspur,Gurgaon,Guruvayoor,Guwahati,Gwalior,Charminar, HyderabadHabra,Hajipur,Haldwani-cum-Kathgodam,Hansi,Hapur,Hardwar,Hazaribag,Hindupur,Hisar,Hoshiarpur,Hubli-Dharwad,Hugli-Chinsurah,Hyderabad,Indore JunctionIchalkaranji,Imphal,Indore,Itarsi,Jagdalpur,Jaggaiahpet,Jagraon,Jagtial,Jaipur,Jalandhar Cantt.,Jalandhar,Jalpaiguri,Jamalpur,Jammalamadugu,Jammu,Jamnagar,Jamshedpur,Jamui,Jangaon,Jatani,Jehanabad,Jhansi,Jhargram,Jharsuguda,Jhumri Tilaiya,Jind,Jorhat,Jodhpur,Kozhikode Beach,Khargone,Kadapa,Kadi,Kadiri,Kagaznagar,Kailasahar,Kaithal,Kakinada,Kalpi,Kalyan-Dombivali,Kamareddy,Kancheepuram,Kandukur,Kanhangad,Kannur,Kanpur,Kapadvanj,Kapurthala,Karaikal,Karimganj,Karimnagar,Karjat,Karnal,Karur,Karwar,Kasaragod,Kashipur,Kathua,Katihar,Kavali,Kayamkulam,Kendrapara,Kendujhar,Keshod,Khagaria,Khambhat,Khammam,Khanna,Kharagpur,Kharar,Khowai,Kishanganj,Kochi,Kodungallur,Kohima,Kolar,Kolkata,Kollam,Korba,Koratla,Kot Kapura,Kothagudem,Kottayam,Kovvur,Kozhikode,Kunnamkulam,Kurnool,Kyathampalle,Ladnu,Ladwa,Lahar,Laharpur,Lakheri,Lakhimpur,Lakhisarai,Lakshmeshwar,Lal Gopalganj Nindaura,Lalganj,Lalgudi,Lalitpur,Lalganj,Lalsot,Lanka,Lar,Lathi,Latur,Lilong,Limbdi,Lingsugur,Loha,Lohardaga,Lonar,Lonavla,Longowal,Loni,Losal,Lucknow,Ludhiana,Lumding,Lunawada,Lunglei,MasaurhiMacherla,Machilipatnam,Madanapalle,Maddur,Madhepura,Madhubani,Madhugiri,Madhupur,Madikeri,Madurai,Magadi,Mahad,Mahbubnagar,Mahalingapura,Maharajganj,Maharajpur,Mahasamund,Mahe,Manendragarh,Mahendragarh,Mahesana,Mahidpur,Mahnar Bazar,Mahuva,Maihar,Mainaguri,Makhdumpur,Makrana,Malda,Malaj Khand,Malappuram,Malavalli,Malegaon,Malerkotla,Malkangiri,Malkapur,Malout,Malpura,Malur,Manasa,Manavadar,Manawar,Mancherial,Mandalgarh,Mandamarri,Mandapeta,Mandawa,Mandi,Mandi Dabwali,Mandideep,Mandla,Mandsaur,Mandvi,Mandya,Maner,Mangaldoi,Mangaluru,Mangalvedhe,Manglaur,Mangrol,Mangrol,Mangrulpir,Manihari,Manjlegaon,Mankachar,Manmad,Mansa,Mansa,Manuguru,Manvi,Manwath,Mapusa,Margao,Margherita,Marhaura,Mariani,Marigaon,Markapur,Marmagao,Masaurhi,Mathabhanga,Mattannur,Mauganj,Mavelikkara,Mavoor,Mayang Imphal,Medak,Medinipur,Meerut,Mehkar,Mahemdabad,Memari,Merta City,Mhaswad,Mhow Cantonment,Mhowgaon,Mihijam,Mira-Bhayandar,Mirganj,Miryalaguda,Modasa,Modinagar,Moga,Mohali,Mokameh,Mokokchung,Monoharpur,Moradabad,Morena,Morinda,Morshi,Morvi,Motihari,Motipur,Mount Abu,Mudalagi,Mudabidri,Muddebihal,Mudhol,Mukerian,Mukhed,Muktsar,Mul,Mulbagal,Multai,Greater Mumbai,Mundi,Mundargi,Mungeli,Munger,Murliganj,Murshidabad,Murtijapur,Murwara (Katni),Musabani,Mussoorie,Muvattupuzha,Muzaffarpur,Nabarangapur,Nabha,Nadbai,Nadiad,Nagaon,Nagapattinam,Nagar,Nagari,Nagarkurnool,Nagaur,Nagda,Nagercoil,Nagina,Nagla,Nagpur,Nahan,Naharlagun,Naihati,Naila Janjgir,Nainital,Nainpur,Najibabad,Nakodar,Nakur,Nalbari,Namagiripettai,Namakkal,Nanded-Waghala,Nandgaon,Nandivaram-Guduvancheri,Nandura,Nandurbar,Nandyal,Nangal,Nanjangud,Nanjikottai,Nanpara,Narasapuram,Narasaraopet,Naraura,Narayanpet,Nargund,Narkatiaganj,Narkhed,Narnaul,Narsinghgarh,Narsinghgarh,Narsipatnam,Narwana,Nashik,Nasirabad,Natham,Nathdwara,Naugachhia,Naugawan Sadat,Nautanwa,Navalgund,Navi Mumbai,Navsari,Nawabganj,Nawada,Nawanshahr,Nawapur,Nedumangad,Neem-Ka-Thana,Neemuch,Nehtaur,Nelamangala,Nellikuppam,Nellore,Nepanagar,New Delhi,Neyveli (TS),Neyyattinkara,Nidadavole,Nilanga,Nilambur,Nimbahera,Nirmal,Niwari,Niwai,Nizamabad,Nohar,Noida,Nokha,Nokha,Nongstoin,Noorpur,North Lakhimpur,Nowgong,Nowrozabad (Khodargama),Nuzvid,Oddanchatram,Obra,Ongole,Orai,Osmanabad,Ottappalam,Ozar,Pachora,Pachore,Pacode,Padmanabhapuram,Padra,Padrauna,Paithan,Pakaur,Palacole,Palai,Palakkad,Palani,Palanpur,Palasa Kasibugga,Palghar,Pali,Pali,Palia Kalan,Palitana,Palladam,Pallapatti,Pallikonda,Palwal,Palwancha,Panagar,Panagudi,Panaji,Panamattom,Panchkula,Panchla,Pandharkaoda,Pandharpur,Pandhurna,Pandua,Panipat,Panna,Panniyannur,Panruti,Panvel,Pappinisseri,Paradip,Paramakudi,Parangipettai,Parasi,Paravoor,Parbhani,Pardi,Parlakhemundi,Parli,Parola,Partur,Parvathipuram,Pasan,Paschim Punropara,Pasighat,Patan,Pathanamthitta,Pathankot,Pathardi,Pathri,Patiala,Patna,Pattran,Patratu,Pattamundai,Patti,Pattukkottai,Patur,Pauni,Pauri,Pavagada,Pedana,Peddapuram,Pehowa,Pen,Perambalur,Peravurani,Peringathur,Perinthalmanna,Periyakulam,Periyasemur,Pernampattu,Perumbavoor,Petlad,Phagwara,Phalodi,Phaltan,Phillaur,Phulabani,Phulera,Phulpur,Phusro,Pihani,Pilani,Pilibanga,Pilibhit,Pilkhuwa,Pindwara,Pinjore,Pipar City,Pipariya,Piro,Piriyapatna,Pithampur,Pithapuram,Pithoragarh,Pollachi,Polur,Pondicherry,Ponnani,Ponneri,Ponnur,Porbandar,Porsa,Port Blair,Powayan,Prantij,Pratapgarh,Pratapgarh,Prithvipur,Proddatur,Pudukkottai,Pudupattinam,Pukhrayan,Pulgaon,Puliyankudi,Punalur,Punch,Pune,Punjaipugalur,Punganur,Puranpur,Purna,Puri,Purnia,Purquazi,Purulia,Purwa,Pusad,Puttur,Puthuppally,Puttur,Radhanpur,Rae Bareli,Rafiganj,Raghogarh-Vijaypur,Raghunathpur,Raghunathganj,Rahatgarh,Rahuri,Raayachuru,Raiganj,Raigarh,Ranebennuru,Ranipet,Raikot,Raipur,Rairangpur,Raisen,Raisinghnagar,Rajagangapur,Rajahmundry,Rajakhera,Rajaldesar,Rajam,Rajampet,Rajapalayam,Rajauri,Rajgarh (Alwar),Rajgarh (Churu),Rajgarh,Rajgir,Rajkot,Rajnandgaon,Rajpipla,Rajpura,Rajsamand,Rajula,Rajura,Ramachandrapuram,Ramagundam,Ramanagaram,Ramanathapuram,Ramdurg,Rameshwaram,Ramganj Mandi,Ramgarh,Ramngarh,Ramnagar,Ramnagar,Rampur,Rampur Maniharan,Rampur Maniharan,Rampura Phul,Rampurhat,Ramtek,Ranaghat,Ranavav,Ranchi,Rangia,Rania,Ranibennur,Rapar,Rasipuram,Rasra,Ratangarh,Rath,Ratia,Ratlam,Ratnagiri,Rau,Raurkela,Raver,Rawatbhata,Rawatsar,Raxaul Bazar,Rayachoti,Rayadurg,Rayagada,Reengus,Rehli,Renigunta,Renukoot,Reoti,Repalle,Revelganj,Rewa,Rewari,Rishikesh,Risod,Robertsganj,Robertson Pet,Rohtak,Ron,Roorkee,Rosera,Rudauli,Rudrapur,Rudrapur,Rupnagar,Sadabad,Sadalagi,Sadasivpet,Sadri,Sadulshahar,Sadulpur,Safidon,Safipur,Sagar,Sagara,Sagwara,Saharanpur,Saharsa,Sahaspur,Sahaswan,Sahawar,Sahibganj,Sahjanwa,Saidpur,Saiha,Sailu,Sainthia,Sakaleshapura,Sakti,Salaya,Salem,Salur,Samalkha,Samalkot,Samana,Samastipur,Sambalpur Town,Sambhal,Sambhar,Samdhan,Samthar,Sanand,Sanawad,Sanchore,Sarsod,Sindagi,Sandi,Sandila,Sanduru,Sangamner,Sangareddy,Sangaria,Sangli,Sangole,Sangrur,Sankarankoil,Sankari,Sankeshwara,Santipur,Sarangpur,Sardarshahar,Sardhana,Sarni,Sasaram,Sasvad,Satana,Satara,Satna,Sathyamangalam,Sattenapalle,Sattur,Saunda,Saundatti-Yellamma,Sausar,Savarkundla,Savanur,Savner,Sawai Madhopur,Sawantwadi,Sedam,Sehore,Sendhwa,Seohara,Seoni,Seoni-Malwa,Shahabad,Shahabad, Hardoi,Shahabad, Rampur,Shahade,Shahbad,Shahdol,Shahganj,Shahjahanpur,Shahpur,Shahpura,Shahpura,Shajapur,Shamgarh,Shamli,Shamsabad, Agra,Shamsabad, Farrukhabad,Shegaon,Sheikhpura,Shendurjana,Shenkottai,Sheoganj,Sheohar,Sheopur,Sherghati,Sherkot,Shiggaon,Shikaripur,Shikarpur, Bulandshahr,Shikohabad,Shillong,Shimla,Shivamogga,Shirdi,Shirpur-Warwade,Shirur,Shishgarh,Shivpuri,Sholavandan,Sholingur,Shoranur,Surapura,Shrigonda,Shrirampur,Shrirangapattana,Shujalpur,Siana,Sibsagar,Siddipet,Sidhi,Sidhpur,Sidlaghatta,Sihor,Sihora,Sikanderpur,Sikandra Rao,Sikandrabad,Sikar,Silao,Silapathar,Silchar,Siliguri,Sillod,Silvassa,Simdega,Sindhagi,Sindhnur,Singrauli,Sinnar,Sira,Sircilla,Sirhind Fatehgarh Sahib,Sirkali,Sirohi,Sironj,Sirsa,Sirsaganj,Sirsi,Sirsi,Siruguppa,Sitamarhi,Sitapur,Sitarganj,Sivaganga,Sivagiri,Sivakasi,Siwan,Sohagpur,Sohna,Sojat,Solan,Solapur,Sonamukhi,Sonepur,Songadh,Sonipat,Sopore,Soro,Soron,Soyagaon,Sri Madhopur,Srikakulam,Srikalahasti,Srinagar,Srinagar,Srinivaspur,Srirampore,Srivilliputhur,Suar,Sugauli,Sujangarh,Sujanpur,Sultanganj,Sullurpeta,Sultanpur,Sumerpur,Sumerpur,Sunabeda,Sunam,Sundargarh,Sundarnagar,Supaul,Surandai,Surat,Suratgarh,Suri,Suriyampalayam,Suryapet,Tadpatri,Taki,Talaja,Talcher,Talegaon Dabhade,Talikota,Taliparamba,Talode,Talwara,Tamluk,Tanda,Tandur,Tanuku,Tarakeswar,Tarana,Taranagar,Taraori,Tarbha,Tarikere,Tarn Taran,Tasgaon,Tehri,Tekkalakote,Tenali,Tenkasi,Tenu dam-cum-Kathhara,Terdal,Tezpur,Thakurdwara,Thammampatti,Thana Bhawan,Thane,Thanesar,Thangadh,Thanjavur,Tharad,Tharamangalam,Tharangambadi,Theni Allinagaram,Thirumangalam,Thirupuvanam,Thiruthuraipoondi,Thiruvalla,Thiruvallur,Thiruvananthapuram,Thiruvarur,Thodupuzha,Thoubal,Thrissur,Thuraiyur,Tikamgarh,Tilda Newra,Tilhar,Talikota,Tindivanam,Tinsukia,Tiptur,Tirora,Tiruchendur,Tiruchengode,Tiruchirappalli,Tirukalukundram,Tirukkoyilur,Tirunelveli,Tirupathur,Tirupathur,Tirupati,Tiruppur,Tirur,Tiruttani,Tiruvannamalai,Tiruvethipuram,Tiruvuru,Tirwaganj,Titlagarh,Tittakudi,Todabhim,Todaraisingh,Tohana,Tonk,Tuensang,Tuljapur,Tulsipur,Tumkur,Tumsar,Tundla,Tuni,Tura,Udaipur,Udaipur,Udaipurwati,Udgir,Udhagamandalam,Udhampur,Udumalaipettai,Udupi,Ujhani,Ujjain,Umarga,Umaria,Umarkhed,Umbergaon,Umred,Umreth,Una,Unjha,Unnamalaikadai,Unnao,Upleta,Uran,Uran Islampur,Uravakonda,Urmar Tanda,Usilampatti,Uthamapalayam,Uthiramerur,Utraula,Vadalur,Vadgaon Kasba,Vadipatti,Vadnagar,Vadodara,Vaijapur,Vaikom,Valparai,Valsad,Vandavasi,Vaniyambadi,Vapi,Vapi,Varanasi,Varkala,Vasai-Virar,Vatakara,Vedaranyam,Vellakoil,Vellore,Venkatagiri,Veraval,Vidisha,Vijainagar, Ajmer,Vijapur,Vijaypur,Vijayapura,Vijayawada,Vikarabad,Vikramasingapuram,Viluppuram,Vinukonda,Viramgam,Virudhachalam,Virudhunagar,Visakhapatnam,Visnagar,Viswanatham,Vita,Vizianagaram,Vrindavan,Vyara,Wadhwan,Wadi,Wai,Wanaparthy,Wani,Wankaner,Wara Seoni,Warangal,Wardha,Warhapur,Warisaliganj,Warora,Warud,Washim,Wokha,Yamunanagar,Yanam,Yavatmal,Yawal,Yellandu,Yemmiganur,Yerraguntla,Yevla,Zaidpur,Zamania,Zira,Zirakpur,Zunheboto";
		 */ArrayList<String> cityList=new ArrayList<String>();
		 public GetUserDetailsDialog() {
			 // TODO Auto-generated constructor stub
			 /*String[] tempList=cityNames.split(",");
				for (int i = 0; i < tempList.length; i++) {
					cityList.add(tempList[i]);
				}*/
		 }

		 @Override
		 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				 Bundle savedInstanceState) {
			 View rootView = inflater.inflate(R.layout.interest_layout, container,
					 false);
			 Dialog dialog=getDialog();
			 dialog.setTitle("Enter Your Garden Details");

			 Button getNumberBtn=(Button)rootView.findViewById(R.id.submitBtn);
			 final EditText cityTxt=(EditText)rootView.findViewById(R.id.cityTxt);

			 final EditText countryTxt=(EditText)rootView.findViewById(R.id.countryTxt);

			 final EditText gardenNameTxt=(EditText)rootView.findViewById(R.id.gardenNameTxt);

			 /*ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,cityList);

				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mobileNumTxt.setAdapter(dataAdapter);
			  */
			 /*final Spinner interestSpinner=(Spinner)rootView.findViewById(R.id.levelSpinner);


				ArrayList<String> inArrayList=new ArrayList<String>();
				inArrayList.add("I am new Kitchen Gardening?");
				inArrayList.add("I am experienced in kitchen Gardening");

				ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,inArrayList);

				dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				interestSpinner.setAdapter(dataAdapter1);*/


			 getNumberBtn.setOnClickListener(new OnClickListener() {

				 @Override
				 public void onClick(View paramView) {
					 // TODO Auto-generated method stub
					 String city=cityTxt.getText().toString();
					 String country = countryTxt.getText().toString();
					 String gardenName= gardenNameTxt.getText().toString();
					 if(city.length()>0 && country.length()>0 && gardenName.length()>0 ){
						 dismiss();
						 submitToServer(city,country,gardenName);
					 }else{
						 Toast.makeText(getActivity(), "All fields are mandatory.", 1000).show();
					 }
				 }
			 });
			 return rootView;
		 }

		 protected void submitToServer(String city, String country, String gardenName) {
			 // TODO Auto-generated method stub
			 JSONObject jsObject = new JSONObject();
			 try {
				 jsObject.put("field_city", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", city))));
				 jsObject.put("field_garden_country", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", country))));
				 jsObject.put("title_field", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", gardenName))));
			 } catch (JSONException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
			 cd = new ConnectionDetector(getActivity());
			 // Check if Internet present
			 if (!cd.isConnectingToInternet()) {
				 // Internet Connection is not present
				 alert.showAlertDialog(getActivity(),
						 "Internet Connection Error",
						 "Please connect to working Internet connection", false);
				 // stop executing code by return
				 return;
			 }else{
				 new UpdateLike(jsObject,myPrefs){
					 protected void onPreExecute() {
						 AppUtil.initializeProgressDialog(getActivity(), "Updating...", progressDialog);
					 };
					 protected void onPostExecute(String result) {
						 AppUtil.cancelProgressDialog();
						 updateHeader=true;
						 if(isRefreshEnabled()){
							 setRefreshEnabled(false);
							 fetchGardenDetails(homeFrgRootView,true);
						 }
					 };

				 }.execute("http://www1.kitchengardens.in/svc/node/"+garden_node_id);
			 }
		 }

	}
	private void openInterestDialog(){
		GetUserDetailsDialog openDialog=new GetUserDetailsDialog();
		openDialog.setCancelable(true);
		openDialog.show(fm, "");
	}
}