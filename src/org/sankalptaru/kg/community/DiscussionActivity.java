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
import org.sankalptaru.kg.community.adapter.TitleNavigationAdapter;
import org.sankalptaru.kg.community.helper.CalenderHelper;
import org.sankalptaru.kg.community.helper.ConnectionDetector;
import org.sankalptaru.kg.community.model.SpinnerNavItem;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class DiscussionActivity extends FragmentActivity implements
ActionBar.OnNavigationListener,OnRefreshListener {

	private Dialog progressDialog;

	// action bar
	private ActionBar actionBar;

	// Title navigation Spinner data
	private ArrayList<SpinnerNavItem> navSpinner;

	// Navigation adapter
	private TitleNavigationAdapter adapter;

	// Refresh menu item
	private MenuItem refreshMenuItem;

	private HashMap<Integer, JSONArray> forumCategoriesDetailsList;

	private ListView mGridDiscussion;

	private CommentFragment dialogComments;
	private View commentsHeaderView;
	private int RESULT_LOAD_POST_IMAGE=1;

	private String sharedImagePath=null;

	private String commentStr;
	private EditText answerText;

	private int screenHeight;

	private int screenWidth;

	private SwipeRefreshLayout swipeLayout;

	private ConnectionDetector cd;

	//	private Context context;
	private AlertDialogManager alert =new AlertDialogManager();

	private AutoCompleteTextView searchAutoComplete;

	private ArrayList<String> titleList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainDiscussionThread();
	}

	private void mainDiscussionThread() {
		// TODO Auto-generated method stub
		setContentView(R.layout.fragment_discussion);


		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);
		swipeLayout.setEnabled(false);		

		searchAutoComplete=(AutoCompleteTextView)findViewById(R.id.searchQuestions);
		searchAutoComplete.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {
				// TODO Auto-generated method stub
				for (int i = 0; i < titleList.size(); i++) {
					if(titleList.get(i).equals(searchAutoComplete.getText().toString())){
						mGridDiscussion.setSelection(i);
						break;
					}
				}
				removeSoftKeyboard();
			}
		});
		initImageLoader(DiscussionActivity.this);
		//		AppUtil.SuperRestartServiceIntent = PendingIntent.getActivity(getBaseContext(), 0,
		//				new Intent(getIntent()), getIntent().getFlags());

		actionBar = getActionBar();
		actionBar.setTitle("Forum");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Hide the action bar title
		//		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		// Enabling Spinner dropdown navigation
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Spinner title navigation data

		ImageView openQueryDialog=(ImageView)findViewById(R.id.openQueryDialog);
		openQueryDialog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AskQuestionDialog st =new AskQuestionDialog(getApplicationContext());
				st.setCancelable(true);
				st.show(getFragmentManager(), "status_open");	
			}
		});

		fetchCategoriesDetails(false);
		getScreenMetrics();
		// Changing the action bar icon
		// actionBar.setIcon(R.drawable.ico_actionbar);
	}

	protected void removeSoftKeyboard() {
		// TODO Auto-generated method stub
		searchAutoComplete.setText("");
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	public void getScreenMetrics(){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
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
	private void fetchCategoriesDetails(boolean isServerRefersh) {
		// TODO Auto-generated method stub
		swipeLayout.setRefreshing(true);
		if(null==forumCategoriesDetailsList)
			forumCategoriesDetailsList=new HashMap<Integer, JSONArray>();
		else
			forumCategoriesDetailsList.clear();

		ArrayList<String> categoriesUrlList=new ArrayList<String>();
		categoriesUrlList.add("http://store.sankalptaru.org/svc/views/discussions?args[0]=experience");
		categoriesUrlList.add("http://store.sankalptaru.org/svc/views/discussions?args[0]=problems");
		categoriesUrlList.add("http://store.sankalptaru.org/svc/views/discussions?args[0]=do");
		categoriesUrlList.add("http://store.sankalptaru.org/svc/views/discussions?args[0]=pest");

		if(isServerRefersh){
			initiateServerRefresh(categoriesUrlList);
		}else{
			try {
				initiateRefreshFromSavedData(categoriesUrlList);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initiateRefreshFromSavedData(ArrayList<String> categoriesUrlList) throws JSONException {
		// TODO Auto-generated method stub
		if(isAllCategoriesFileAvailable(categoriesUrlList)){
			for (int i = 0; i < categoriesUrlList.size(); i++) {
				File tempFile=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+"discussion_category_"+i+".txt");
				forumCategoriesDetailsList.put(i, new JSONArray(AppUtil.readFile(tempFile)));
				if(i==(categoriesUrlList.size()-1)){
					getEachCategoryCount();
				}
			}
		}else{
			initiateServerRefresh(categoriesUrlList);
		}
	}

	private boolean isAllCategoriesFileAvailable(ArrayList<String> categoriesUrlList) {
		// TODO Auto-generated method stub
		for (int i = 0; i < categoriesUrlList.size(); i++) {
			File tempFile=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+"discussion_category_"+i+".txt");
			if(!tempFile.exists())
				return false;
		}
		return true;
	}

	private void initiateServerRefresh(ArrayList<String> categoriesUrlList) {
		// TODO Auto-generated method stub
		for (int i = 0; i < categoriesUrlList.size(); i++) {
			File tempFile=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+"discussion_category_"+i+".txt");
			if(tempFile.exists())
				tempFile.delete();
		}

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert .showAlertDialog(DiscussionActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			for (int i = 0; i < categoriesUrlList.size(); i++) {
				new GetCategoriesDetails(i).execute(categoriesUrlList.get(i));
			}
		}
	}



	private class GetCategoriesDetails extends AsyncTask<String, Integer, JSONArray>{

		private int counter;

		public GetCategoriesDetails(int i) {
			// TODO Auto-generated constructor stub
			counter=i;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//			if(counter==0)
			//				AppUtil.initializeProgressDialog(DiscussionActivity.this, "Fetching Categories Details...", progressDialog);
		}

		@Override
		protected JSONArray doInBackground(String... params) {
			// TODO Auto-generated method stub

			JSONArray jsResponse=null;

			try {
				jsResponse= new JSONArray(AppUtil.getResponse(params[0]));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return jsResponse;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				saveDiscussionFiles(counter,result);
				forumCategoriesDetailsList.put(counter, result);
				if(counter==3){
					getEachCategoryCount();
				}
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);

		// Associate searchable configuration with the SearchView
		/*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));*/

		return super.onCreateOptionsMenu(menu);
	}

	public void getEachCategoryCount() {
		// TODO Auto-generated method stub
		ArrayList<Integer> eachCategoryThreadCount=new ArrayList<Integer>();
		for (int i = 0; i < forumCategoriesDetailsList.size(); i++) {
			eachCategoryThreadCount.add(forumCategoriesDetailsList.get(i).length());
		}
		populateNavSpinner(eachCategoryThreadCount);
	}

	public void saveDiscussionFiles(int counter, JSONArray result) {
		// TODO Auto-generated method stub
		AppUtil.createFolderInSDCard();
		AppUtil.createFile(AppUtil.JSON_FILE_PATH+"discussion_category_"+counter+".txt");
		AppUtil.writeToFile(result.toString(), AppUtil.JSON_FILE_PATH+"discussion_category_"+counter+".txt");
	}


	public void populateNavSpinner(ArrayList<Integer> eachCategoryThreadCount) {
		// TODO Auto-generated method stub
		navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Share Your Experiences", R.drawable.ic_general_practices));
		navSpinner
		.add(new SpinnerNavItem("Problems & Help ", R.drawable.ic_help));

		navSpinner.add(new SpinnerNavItem("Do's & Dont's     ", R.drawable.ic_tips));

		navSpinner.add(new SpinnerNavItem("Pest Control   ", R.drawable.ic_recipe));

		// title drop down adapter
		adapter = new TitleNavigationAdapter(DiscussionActivity.this,
				navSpinner,eachCategoryThreadCount);

		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter, DiscussionActivity.this);	
	}

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.action_refresh:
			fetchCategoriesDetails(true);
			return true;

		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Launching new activity
	 * */
	private void LocationFound() {
	}

	/*
	 * Actionbar navigation item select listener
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Action to be taken after selecting a spinner item

		try {
			populateCategorySpecificQuestions(itemPosition);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	private void populateCategorySpecificQuestions(int itemPosition) throws JSONException {
		// TODO Auto-generated method stub
		mGridDiscussion=(ListView)findViewById(R.id.grid_discussion);
		JSONArray categoryQuestionsArray = forumCategoriesDetailsList.get(itemPosition);
		if(null==titleList)
			titleList=new ArrayList<String>();
		else
			titleList.clear();

		ArrayList<String> commentCountList = new ArrayList<String>();

		ArrayList<String> questionCreatedTimeList =new ArrayList<String>();

		ArrayList<String> questionOwnerNameList = new ArrayList<String>();

		ArrayList<String>  questionNidList =new ArrayList<String>();

		ArrayList<String>  lastCommentTimeList =new ArrayList<String>();

		ArrayList<String> descriptionList=new ArrayList<String>();

		ArrayList<String> quetionPicNameList=new ArrayList<String>();


		for (int i = 0; i < categoryQuestionsArray.length(); i++) {
			JSONObject questionObject = categoryQuestionsArray.getJSONObject(i);
			titleList.add(questionObject.optString("title"));
			commentCountList.add(questionObject.optString("comment_count"));
			questionCreatedTimeList.add(questionObject.optString("created"));
			questionOwnerNameList.add(questionObject.optJSONObject("field_garden_name").optJSONArray("und").getJSONObject(0).optString("value"));
			questionNidList.add(questionObject.optString("nid"));
			lastCommentTimeList.add(questionObject.optString("last_comment_timestamp"));
			descriptionList.add(questionObject.optJSONObject("body").optJSONArray("und").getJSONObject(0).optString("value"));
			JSONObject imageObject=questionObject.optJSONObject("field_image");
			if(null!=imageObject){
				quetionPicNameList.add(imageObject.optJSONArray("und").optJSONObject(0).optString("filename"));
			}
			else{
				quetionPicNameList.add(null);
			}
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DiscussionActivity.this, android.R.layout.simple_spinner_item,titleList);

		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		searchAutoComplete.setAdapter(dataAdapter);

		final RelativeLayout queryLyt=(RelativeLayout)findViewById(R.id.queryLyt);

		mGridDiscussion.setAdapter(new QuestionsAdapter(titleList,commentCountList,questionCreatedTimeList,questionOwnerNameList,lastCommentTimeList,descriptionList,questionNidList,quetionPicNameList));


		//this progressbar would be cancel in GetCategories post execute where count==3
		//		AppUtil.cancelProgressDialog();
		swipeLayout.setRefreshing(false);
	}


	private class QuestionsAdapter extends BaseAdapter{

		private ArrayList<String> titleList;
		private ArrayList<String> commentCountList;
		private ArrayList<String> questionCreatedTimeList;
		private ArrayList<String> questionOwnerNameList;
		private ArrayList<String> lastCommentTimeList;
		private LayoutInflater inflater;
		private ArrayList<String> descriptionList;
		private ArrayList<String> questionNidList;
		private ArrayList<String> quetionPicNameList;

		public QuestionsAdapter(ArrayList<String> titleList,ArrayList<String> commentCountList,ArrayList<String> questionCreatedTimeList,ArrayList<String> questionOwnerNameList,ArrayList<String> lastCommentTimeList, ArrayList<String> descriptionList, ArrayList<String> questionNidList, ArrayList<String> quetionPicNameList) {
			// TODO Auto-generated constructor stub
			inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.titleList=titleList;
			this.commentCountList=commentCountList;
			this.lastCommentTimeList=lastCommentTimeList;
			this.questionOwnerNameList=questionOwnerNameList;
			this.questionCreatedTimeList=questionCreatedTimeList;
			this.descriptionList=descriptionList;
			this.questionNidList=questionNidList;
			this.quetionPicNameList=quetionPicNameList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return titleList.size();
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

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View vi=convertView;
			if(convertView==null)
				vi = inflater.inflate(R.layout.discussion_grid_item, null);

			TextView descriptionTxtView= (TextView)vi.findViewById(R.id.profilePostDescrip);

			TextView countTxtView= (TextView)vi.findViewById(R.id.answerCount);

			TextView questionTxtView= (TextView)vi.findViewById(R.id.profilePostTitle);

			TextView answerTimeTxt=(TextView)vi.findViewById(R.id.answerTime);

			TextView questionOwnerTxtView= (TextView)vi.findViewById(R.id.profileUserName);

			answerTimeTxt.setText("last answerd "+AppUtil.getTimeDifference(Long.parseLong(lastCommentTimeList.get(position))));

			final String number = commentCountList.get(position);
			Button btnView=(Button)vi.findViewById(R.id.viewThreadBtn);

			final String nid=questionNidList.get(position);

			if(!number.equals("0")){
				countTxtView.setBackgroundResource(R.drawable.roundbox_green);
				answerTimeTxt.setVisibility(View.VISIBLE);
			}
			else{
				answerTimeTxt.setVisibility(View.GONE);
			}
			countTxtView.setText(number+" Conversations");

			final String description=descriptionList.get(position);
			descriptionTxtView.setText(description);

			final String question=titleList.get(position);
			questionTxtView.setText(question);

			String time=AppUtil.getTimeDifference(Long.parseLong(questionCreatedTimeList.get(position)));

			questionOwnerTxtView.setText(Html.fromHtml("asked by "+"<b>"+questionOwnerNameList.get(position)+"</b>"+"<br/>"+"<font color=\"#e1e1e1\">"+time+"</font>"));

			btnView.setOnClickListener(new OnClickListener() {



				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(getApplicationContext(), "Open comments", 2000).show();
					dialogComments=new CommentFragment(question,description,nid,quetionPicNameList.get(position));
					dialogComments.setCancelable(true);
					dialogComments.show(getFragmentManager(), "");
				}
			});

			return vi;
		}

	}



	private class CommentFragment extends android.app.DialogFragment{

		private static final int REQUEST_CODE_ANSWER_CLICK_IMAGE = 7;
		private ListView commentQuestionList;
		private String question,description,nid,filename;
		private DisplayImageOptions options;
		private String cameraImagePath;

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

		protected void saveCameraImage() {
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
			startActivityForResult(intent, REQUEST_CODE_ANSWER_CLICK_IMAGE);

		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			if(requestCode == REQUEST_CODE_ANSWER_CLICK_IMAGE  && resultCode == RESULT_OK){
				Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
				compressCamBitmap(cameraImagePath);
			}
			else if(requestCode == RESULT_LOAD_POST_IMAGE && resultCode == RESULT_OK && null != data){
				Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
				compressCamBitmap(AppUtil.getGalleryPath(data,DiscussionActivity.this));
			}
		}

		public CommentFragment(String question, String description, String nid, String filename) {
			// TODO Auto-generated constructor stub
			this.description=description;
			this.question=question;
			this.nid=nid;
			this.filename=filename;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_comment_dialog, container,
					false);
			Window window = getDialog().getWindow();

			getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
			window.setBackgroundDrawableResource(android.R.color.transparent);

			ImageView addCamImg=(ImageView)rootView.findViewById(R.id.addCamImg);
			addCamImg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					saveCameraImage();
				}
			});

			ImageView addPostImage=(ImageView)rootView.findViewById(R.id.addAnswerImg);
			addPostImage.setOnClickListener(new OnClickListener() {



				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(
							Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					startActivityForResult(i, RESULT_LOAD_POST_IMAGE);
				}
			});

			answerText=(EditText)rootView.findViewById(R.id.answerBox);

			ImageView sendBtn=(ImageView)rootView.findViewById(R.id.postAnswerImg);
			sendBtn.setOnClickListener(new OnClickListener() {


				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
					if(answerText.getText().length()>0){

						commentStr=answerText.getText().toString().trim();

						if(sharedImagePath==null){
							AlertPostFragment dialog=new AlertPostFragment(DiscussionActivity.this,"Info","Image not added to post.\n\nNote: Sharing your image with your status is recommended.","Proceed Anyway","Cancel",false,"not_added",nid);
							dialog.setCancelable(true);
							dialog.show(getFragmentManager(), "post");
						}
						else{
							myPrefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
							doPostCommentsActions(myPrefs,nid);
						}
					}else{
						AlertPostFragment dialog=new AlertPostFragment(DiscussionActivity.this,"Warning","All fields are mandatory to be filled.","OK","Proceed",true,"field_check",nid);
						dialog.setCancelable(true);
						dialog.show(getFragmentManager(), "field");
					}
				}
			});


			commentQuestionList=(ListView)rootView.findViewById(R.id.discussion_comment_list);

			commentsHeaderView=getLayoutInflater().inflate(R.layout.discussion_comment_list_header, null);
			commentQuestionList.addHeaderView(commentsHeaderView);
			TextView quesView=(TextView)commentsHeaderView.findViewById(R.id.txtDisQuest);
			quesView.setText(Html.fromHtml("<b><font color=\"#000000\"><u>Question</u>:</font></b> "+question));

			TextView quesDescTxt=(TextView)commentsHeaderView.findViewById(R.id.txtDisQuestDesc);
			quesDescTxt.setText(Html.fromHtml("<b><font color=\"#000000\"><u>Description</u>:</font></b> "+description));

			final TextView countText=(TextView)commentsHeaderView.findViewById(R.id.answersCount);

			ImageView questionPic=(ImageView)commentQuestionList.findViewById(R.id.questionPic);
			if(null!=filename){
				options = getRoundDisplayerOption();
				questionPic.getLayoutParams().height = (int) Math.floor(screenHeight/2.5);
				final String url="http://www1.kitchengardens.in/sites/default/files/"+filename;
				ImageLoader.getInstance().displayImage(url, questionPic, options, animateFirstListener);
				questionPic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View paramView) {
						// TODO Auto-generated method stub
						options = getFadeInDisplayerOption();
						getScreenMetrics();
						ImageAlertFragment imageFragment=new ImageAlertFragment(url,options,animateFirstListener,question,description,screenWidth,screenHeight);
						imageFragment.setCancelable(true);
						imageFragment.show(getFragmentManager(), "");
					}
				});
			}

			doActions(countText);
			return rootView;
		}

		private void doActions(final TextView countText) {
			// TODO Auto-generated method stub
			cd = new ConnectionDetector(getApplicationContext());

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(getActivity(),
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				new CSRFToken(){
					protected void onPostExecute(String result) {

						if(result!=null){
							try {
								itterateCommentsJSONArray(result,countText,nid);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					};
				}.execute("http://store.sankalptaru.org/svc/node/"+nid+"/comments");
			}
		}

		class AlertPostFragment extends android.app.DialogFragment {

			private String title; 
			private String message; 
			private String positivetxt;
			private String negativeTxt;
			private boolean setNegativeInvisible;
			private String positiveBtnLogicalHub;
			private DiscussionActivity discussionActivity;
			private String nid;

			public AlertPostFragment(DiscussionActivity discussionActivity, String title, String message, String positivetxt,String negativeTxt, boolean setNegativeInvisible, String positiveBtnLogicalHub, String nid) {
				// TODO Auto-generated constructor stub
				this.title=title;
				this.message=message;
				this.positivetxt=positivetxt;
				this.negativeTxt=negativeTxt;
				this.setNegativeInvisible=setNegativeInvisible;
				this.positiveBtnLogicalHub=positiveBtnLogicalHub;
				this.discussionActivity=discussionActivity;
				this.nid=nid;
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
						if(positiveBtnLogicalHub.equals("not_added")){
							postComments(null,nid);
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


		protected void itterateCommentsJSONArray(String result, TextView countText, String nid) throws JSONException {
			// TODO Auto-generated method stub

			if(!result.equals("[]")){
				JSONObject jsonArray=new JSONObject(result);
				Log.e("data",""+ result);
				HashMap<String, JSONObject> parentJsonObjectMap=new HashMap<String, JSONObject>();

				HashMap<String, ArrayList<JSONObject>> parentChildJSObjectMap =new HashMap<String, ArrayList<JSONObject>>();


				Iterator it=jsonArray.keys();

				while(it.hasNext()) {

					String key=(String) it.next();
					JSONObject jsonObject=jsonArray.optJSONObject(key);
					String parentCommentID=jsonObject.optString("pid");
					if(parentCommentID.equals("0")){
						parentJsonObjectMap.put(key, jsonObject);
					}else{

						if(parentChildJSObjectMap.containsKey(parentCommentID)){
							ArrayList<JSONObject> tempList = parentChildJSObjectMap.get(parentCommentID);
							tempList.add(jsonObject);
							parentChildJSObjectMap.put(parentCommentID, tempList);
						}
						else{
							ArrayList<JSONObject> tempList=new ArrayList<JSONObject>();
							tempList.add(jsonObject);
							parentChildJSObjectMap.put(parentCommentID, tempList);
						}
					}
				}
				Iterator myVeryOwnIterator = parentChildJSObjectMap.keySet().iterator();
				while(myVeryOwnIterator.hasNext()) {
					String key=(String) myVeryOwnIterator.next();
					ArrayList<JSONObject> value=parentChildJSObjectMap.get(key);
					//				Log.e("data", "Key: "+key+" JS List"+value.toString());
				}

				Iterator anotherItera = parentJsonObjectMap.keySet().iterator();

				ArrayList<String> parentCommentsKeyList=new ArrayList<String>();

				while (anotherItera.hasNext()) {
					String key = (String) anotherItera.next();
					//				Log.e("parent", "parent key: "+key+" "+parentJsonObjectMap.get(key));

					parentCommentsKeyList.add(key);
				}

				countText.setText(parentCommentsKeyList.size()+" Answers");

				commentQuestionList.setAdapter(new CommentsAdapter(parentChildJSObjectMap,parentJsonObjectMap,parentCommentsKeyList,nid,getApplicationContext()));
			}
			else{
				commentQuestionList.setAdapter(new CommentsAdapter(null,null,null,nid,getApplicationContext()));
			}
		}
	}

	private void doPostCommentsActions(SharedPreferences myPrefs, final String nid) {
		// TODO Auto-generated method stub
		JSONObject js= new JSONObject();

		File imageFile = new File(sharedImagePath);
		Random rand=new Random();
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
			cd = new ConnectionDetector(getApplicationContext());

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(DiscussionActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				new PostImage(js,myPrefs){
					protected void onPreExecute() {
						AppUtil.initializeProgressDialog(DiscussionActivity.this, "Uploading Image...", progressDialog);
					};

					protected void onPostExecute(JSONObject result) {
						if(null!=result){
							AppUtil.cancelProgressDialog();
							postComments(result.optString("fid"),nid);
						}
					};
				}.execute("http://www1.kitchengardens.in/svc/file/");
			}
		}
	}

	public void postComments(String fid, String nodeID) {
		// TODO Auto-generated method stub
		JSONObject jsObject = new JSONObject();
		String nid=nodeID;

		try {
			jsObject.put("nid", nid);
			jsObject.put("comment_body", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", commentStr))));
			SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
			String gardenName=myPrefs.getString("discussion_user", "none");
			jsObject.put("field_garden_name", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", gardenName))));
			
			if(null!=fid)
				jsObject.put("field_image", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("fid", fid))));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(DiscussionActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			new PostComment(jsObject,myPrefs){

				protected void onPreExecute() {
					AppUtil.initializeProgressDialog(DiscussionActivity.this, "Finalizing post...", progressDialog);
				};

				protected void onPostExecute(JSONObject result) {
					AppUtil.cancelProgressDialog();
					if(dialogComments!=null){
						String nid=	dialogComments.nid;
						String des=	dialogComments.description;
						String quest=	dialogComments.question;
						String filename=dialogComments.filename;

						CalenderHelper calenderHelper=new CalenderHelper(getApplicationContext());
						Calendar cal=Calendar.getInstance();
						Date date=new Date();
						calenderHelper.createEvent("Answered to question: "+quest, "Answer: "+commentStr, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND),cal.get(Calendar.MILLISECOND),date.getTime(),date.getTime(),null);


						dialogComments.getDialog().dismiss();
						dialogComments=null;
						dialogComments=new CommentFragment(quest,des,nid,filename);
						dialogComments.setCancelable(true);
						dialogComments.show(getFragmentManager(), "");
					}

					answerText.setText("");
					commentStr="";
					sharedImagePath=null;
				};

			}.execute("http://www1.kitchengardens.in/svc/comment/");
		}
	}

	private class CommentsAdapter extends BaseAdapter{

		private HashMap<String, ArrayList<JSONObject>> parentChildJSObjectMap;
		private HashMap<String, JSONObject> parentJsonObjectMap;
		private ArrayList<String> parentCommentsKeyList;
		private LayoutInflater inflater;
		private String nid;
		private DisplayImageOptions options;
		private int count=0;
		private Context context;

		public CommentsAdapter(
				HashMap<String, ArrayList<JSONObject>> parentChildJSObjectMap,
				HashMap<String, JSONObject> parentJsonObjectMap,
				ArrayList<String> parentCommentsKeyList, String nid, Context context) {
			// TODO Auto-generated constructor stub
			this.parentChildJSObjectMap=parentChildJSObjectMap;
			this.parentCommentsKeyList=parentCommentsKeyList;
			this.parentJsonObjectMap=parentJsonObjectMap;
			inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.nid=nid;

			if (null!=parentCommentsKeyList) {
				count=parentCommentsKeyList.size();
			}			
			options = getFadeInDisplayerOption();
			this.context=context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return count;
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View vi=convertView;
			if(convertView==null)
				vi = inflater.inflate(R.layout.comments_thread_lyt, null);

			TextView mainComment=(TextView)vi.findViewById(R.id.mainCommentTxt);
			final String key=parentCommentsKeyList.get(position);

			final JSONObject parentObject=parentJsonObjectMap.get(key);
			final ToggleButton likeAnswer=(ToggleButton)vi.findViewById(R.id.likeAnswer);

			final int previousLikeCount=Integer.parseInt(parentObject.optJSONObject("field_like").optJSONArray("und").optJSONObject(0).optString("value"));

			final TextView likeCount=(TextView)vi.findViewById(R.id.likeCount);
			likeCount.setText(previousLikeCount+" Likes");

			likeAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				private int localCount;
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					likeAnswer.setChecked(true);
					JSONObject jsonObject=new JSONObject();
					localCount=previousLikeCount;
					try {
						localCount++;
						jsonObject.put("field_like", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", ""+localCount))));

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
					myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
					cd = new ConnectionDetector(context);

					// Check if Internet present
					if (!cd.isConnectingToInternet()) {
						// Internet Connection is not present
						alert.showAlertDialog(DiscussionActivity.this,
								"Internet Connection Error",
								"Please connect to working Internet connection", false);
						// stop executing code by return
						return;
					}else{
						new UpdateLike(jsonObject,myPrefs){
							protected void onPostExecute(String result) {
								likeCount.setText(localCount+" Likes");
							};

						}.execute("http://www1.kitchengardens.in/svc/comment/"+key);
					}
				}
			});

			LinearLayout l=(LinearLayout)vi.findViewById(R.id.replyparentLyt);

			l.removeAllViews();

			if(parentChildJSObjectMap.containsKey(key)){
				for (int i = 0; i < parentChildJSObjectMap.get(key).size(); i++) {
					JSONObject currentObject=parentChildJSObjectMap.get(key).get(i);
					View child = inflater.inflate(R.layout.reply_thread_lyt,null);

					TextView replyTxt=(TextView)child.findViewById(R.id.replyTxt);
					TextView replyDetailsTxt =(TextView) child.findViewById(R.id.replyDetailsTxt);


					try {
						replyTxt.setText(currentObject.optJSONObject("comment_body").optJSONArray("und").getJSONObject(0).optString("value"));
						replyDetailsTxt.setText("replied by "+currentObject.optJSONObject("field_garden_name").optJSONArray("und").getJSONObject(0).optString("value")+" on "+AppUtil.getDate(Long.parseLong(currentObject.optString("created"))));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					l.addView(child);
				}
			}


			try {
				mainComment.setText(parentObject.optJSONObject("comment_body").optJSONArray("und").getJSONObject(0).optString("value"));
				TextView mainCommentDetails =(TextView)vi.findViewById(R.id.mainCommentDetailsTxt);
				mainCommentDetails.setText("answered on "+AppUtil.getDate(Long.parseLong(parentObject.optString("created")))+" by "+parentObject.optJSONObject("field_garden_name").optJSONArray("und").getJSONObject(0).optString("value"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			final ImageView sharedImage=(ImageView)vi.findViewById(R.id.answerSharedImage);

			sharedImage.getLayoutParams().height = (int) Math.floor(screenHeight/2.5);
			sharedImage.getLayoutParams().width=screenWidth;

			String imageName=null;

			JSONObject imageObject=parentObject.optJSONObject("field_image");

			if(imageObject!=null){
				imageName = imageObject.optJSONArray("und").optJSONObject(0).optString("filename");
				final String url="http://www1.kitchengardens.in/sites/default/files/"+imageName;
				ImageLoader.getInstance().displayImage(url, sharedImage, options, animateFirstListener);
				sharedImage.setVisibility(View.VISIBLE);
				sharedImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ImageAlertFragment imageFragment;
						try {
							getScreenMetrics();
							imageFragment = new ImageAlertFragment(url,options,animateFirstListener,"Answer:",parentObject.optJSONObject("comment_body").optJSONArray("und").getJSONObject(0).optString("value"),screenWidth,screenHeight);
							imageFragment.setCancelable(true);
							imageFragment.show(getFragmentManager(), "");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
			}else{
				sharedImage.setVisibility(View.GONE);
			}


			final EditText replyBox=(EditText)vi.findViewById(R.id.replyEditText);
			final Button btnPostComment=(Button)vi.findViewById(R.id.postComment);

			btnPostComment.setOnClickListener(new OnClickListener() {

				private SharedPreferences myPrefs;

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if(replyBox.getText().length()>0){
						btnPostComment.setEnabled(false);
						JSONObject jsonObject=new JSONObject();
						try {
							jsonObject.put("nid", nid);
							jsonObject.put("pid", key);
							jsonObject.put("comment_body", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", replyBox.getText().toString().trim()))));

							SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
							String gardenName=myPrefs.getString("discussion_user", "none");
							jsonObject.put("field_garden_name", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", gardenName))));
							


						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
						cd = new ConnectionDetector(context);

						// Check if Internet present
						if (!cd.isConnectingToInternet()) {
							// Internet Connection is not present
							alert.showAlertDialog(DiscussionActivity.this,
									"Internet Connection Error",
									"Please connect to working Internet connection", false);
							// stop executing code by return
							return;
						}else{
							new PostComment(jsonObject,myPrefs){

								protected void onPreExecute() {
								};

								protected void onPostExecute(JSONObject result) {
									if(result!=null){
										if(dialogComments!=null){
											String nid=	dialogComments.nid;
											String des=	dialogComments.description;
											String quest=	dialogComments.question;
											String filename=dialogComments.filename;

											dialogComments.getDialog().dismiss();
											dialogComments=null;
											dialogComments=new CommentFragment(quest,des,nid,filename);
											dialogComments.setCancelable(true);
											dialogComments.show(getFragmentManager(), "");
										}
									}
								};

							}.execute("http://www1.kitchengardens.in/svc/comment/");
						}
					}else{
						Toast.makeText(DiscussionActivity.this, "Reply is empty.", 1000).show();
					}
				}
			});

			final TextView addCommentTxt=(TextView)vi.findViewById(R.id.addCommentTxt);
			addCommentTxt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					replyBox.setVisibility(View.VISIBLE);
					addCommentTxt.setVisibility(View.INVISIBLE);
					btnPostComment.setVisibility(View.VISIBLE);
				}
			});
			return vi;
		}
	}

	/**
	 * Async task to load the data from server
	 * **/
	private class SyncData extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			// set the progress bar view

			refreshMenuItem.expandActionView();
		}

		@Override
		protected String doInBackground(String... params) {
			// not making real request in this demo
			// for now we use a timer to wait for sometime
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			refreshMenuItem.collapseActionView();
			// remove the progress bar view
			refreshMenuItem.setActionView(null);
		}
	};





	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
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

	private DisplayImageOptions getFadeInDisplayerOption(){
		DisplayImageOptions localOptions=new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer(1000))
		.build();
		return localOptions;
	}

	private DisplayImageOptions getRoundDisplayerOption(){
		DisplayImageOptions localOptions=new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer(5))
		.build();
		return localOptions;
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		finish();
	}
}
