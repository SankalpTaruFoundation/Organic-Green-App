package org.sankalptaru.kg.community;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class VideoFragment extends FragmentActivity {
	private ActionBar actionBar;
	private SwipeRefreshLayout swipeLayout;
	private ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();
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
	private void init() {
		// TODO Auto-generated method stub
		setContentView(R.layout.fragment_video);

		initImageLoader(VideoFragment.this);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);
		swipeLayout.setEnabled(false);		


		actionBar = getActionBar();
		actionBar.setTitle("Videos");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Hide the action bar title
		//		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		try {
			initiateLayoutPopulation(false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initiateLayoutPopulation(boolean isSeverRefresh) throws JSONException {
		// TODO Auto-generated method stub

		if(isSeverRefresh){
			fetchStVideosDetailsServer();	
		}else{
			fetchDataFromSDCard();
		}
	}

	private void fetchDataFromSDCard() throws JSONException {
		// TODO Auto-generated method stub
		ArrayList<String> fileList=iterrateFilesAndProceed();
		int fileSize=fileList.size();
		if(fileSize>0){
			JSONArray jsonArray=new JSONArray();
			for (int i = 0; i < fileSize; i++) {
				File tempFile=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH+fileList.get(i));
				JSONArray temArray=new JSONObject(AppUtil.readFile(tempFile)).optJSONObject("data").optJSONArray("items");

				for (int j = 0; j < temArray.length(); j++) {
					jsonArray.put(temArray.getJSONObject(j));
				}
			}
			populateListWithData(jsonArray);

		}else{
			fetchStVideosDetailsServer();
		}
	}

	private ArrayList<String> iterrateFilesAndProceed() {
		// TODO Auto-generated method stub
		File path=new File(Environment.getExternalStorageDirectory(),AppUtil.JSON_FILE_PATH);
		ArrayList<String> fileNameList=new ArrayList<String>();
		if(path.exists()){
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fName=files[i].getName();
				if(fName.contains("st_videos_details"))
					fileNameList.add(fName);
			}
		}
		return fileNameList;
	}

	private AlertDialogManager alert =new AlertDialogManager();

	private void fetchStVideosDetailsServer() {
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert .showAlertDialog(VideoFragment.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			//https://gdata.youtube.com/feeds/api/videos?q=sankalptaru&v=2&alt=jsonc&orderby=published&max-results=50&start-index=1
			new CSRFToken(){
				protected void onPreExecute() {
					swipeLayout.setRefreshing(true);
				};
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					if(null!=result){

						try {
							int totalItems=Integer.parseInt(new JSONObject(result).optJSONObject("data").optString("totalItems"));
							final int numberOfPage=totalItems/50;

							for (int i = 1; i <= (numberOfPage+1); i++) {
								int pagenum=i*50;

								int start_index=pagenum-49;

								if(i==(numberOfPage+1)){
									pagenum=totalItems%50;
								}

								final int tempIndex=i;
								new CSRFToken(){
									protected void onPostExecute(String result) {

										saveVideoJson(result,tempIndex);										
										if(tempIndex==(numberOfPage+1)){
											swipeLayout.setRefreshing(false);
											try {
												fetchDataFromSDCard();
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}

								}.execute("https://gdata.youtube.com/feeds/api/videos?q=sankalptaru&v=2&alt=jsonc&max-results="+pagenum+"&start-index="+start_index);


							}

						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.execute("https://gdata.youtube.com/feeds/api/videos?q=sankalptaru&v=2&alt=jsonc&max-results=1&start-index=1");
		}
	}

	protected void populateListWithData(JSONArray jsonArray) {
		// TODO Auto-generated method stub
		ListView vidlist=(ListView)findViewById(R.id.video_list);
		vidlist.setAdapter(new VideoAdapter(jsonArray));
	}

	private class VideoAdapter extends BaseAdapter{

		private JSONArray itemsArray;
		private LayoutInflater inflater;

		public VideoAdapter(JSONArray itemsArray) {
			// TODO Auto-generated constructor stub
			this.itemsArray=itemsArray;
			inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return itemsArray.length();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			View vi=convertView;
			if(convertView==null)
				vi = inflater.inflate(R.layout.video_list_item, null);

			try {
				final JSONObject currentJs= itemsArray.getJSONObject(arg0);
				//				videoThumbnail sharevideoImg videoTitle videoDesc videoDuration videoUploader videoCategory
				ImageView videoThumbnail=(ImageView)vi.findViewById(R.id.videoThumbnail);

				final String videoId=currentJs.optString("id");
				videoThumbnail.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("https://www.youtube.com/watch?v="+videoId));
						startActivity(intent);
					}
				});

				ImageView sharevideoImg=(ImageView)vi.findViewById(R.id.sharevideoImg);


				TextView videoTitle=(TextView)vi.findViewById(R.id.videoTitle);

				TextView videoDesc=(TextView)vi.findViewById(R.id.videoDesc);

				TextView videoDuration=(TextView)vi.findViewById(R.id.videoDuration);

				TextView videoCategory=(TextView)vi.findViewById(R.id.videoCategory);

				TextView videoDate=(TextView)vi.findViewById(R.id.videoDate);

				ImageLoader.getInstance().displayImage(currentJs.optJSONObject("thumbnail").optString("hqDefault"), videoThumbnail, getFadeInDisplayerOption(), animateFirstListener);

				final String title=currentJs.optString("title");
				videoTitle.setText(Html.fromHtml("<b><font color=\"#000000\">Title:</font></b> "+"\n"+title));

				final String desc=currentJs.optString("description");
				videoDesc.setText(Html.fromHtml("<b><font color=\"#000000\">Description:</font></b> "+"\n"+desc));


				videoDuration.setText(Html.fromHtml("<b><font color=\"#000000\">Duration:</font></b> "+"\n"+currentJs.optString("duration")+" sec"));


				videoCategory.setText(Html.fromHtml("<b><font color=\"#000000\">Category:</font></b> "+"\n"+currentJs.optString("category")));

				videoDate.setText(Html.fromHtml("<b><font color=\"#000000\">Uploaded On:</font></b> "+"\n"+currentJs.optString("uploaded").split("T")[0]));

				sharevideoImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
						sharingIntent.setType("text/plain");
						sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
						sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, desc+"\n"+"https://www.youtube.com/watch?v="+videoId+"\n\nPowered by SankalpTaru Organic Greens.All Rights Reserved.");
						startActivity(Intent.createChooser(sharingIntent, "Share video via"));
					}
				});

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return vi;
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
	protected void saveVideoJson(String result, int i) {
		// TODO Auto-generated method stub

		AppUtil.createFolderInSDCard();
		AppUtil.createFile(AppUtil.JSON_FILE_PATH+"st_videos_details_"+i+".txt");
		AppUtil.writeToFile(result, AppUtil.JSON_FILE_PATH+"st_videos_details_"+i+".txt");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.video_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			try {
				initiateLayoutPopulation(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;

		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
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
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		finish();
	}
}
