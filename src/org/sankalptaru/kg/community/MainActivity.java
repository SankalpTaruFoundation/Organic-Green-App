package org.sankalptaru.kg.community;

import java.io.File;
import java.util.ArrayList;

import org.sankalptaru.kg.community.adapter.NavDrawerListAdapter;
import org.sankalptaru.kg.community.model.NavDrawerItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class MainActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	//	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private int uid;
	private ArrayList<String> titleArrayList;
	private String customGardenName=null;
	private boolean isHomeFragFirstTime =true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*AppUtil.SuperRestartServiceIntent = PendingIntent.getActivity(getBaseContext(), 0,
				new Intent(getIntent()), getIntent().getFlags());
		 */

		beforeLoginView();
		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		//		Log.e("editor", myPrefs.getString("demo", "null"));

		uid = myPrefs.getInt("uid", -1);

		if(uid!=-1)
			afterLoginView();

	}

	private void afterLoginView() {
		// TODO Auto-generated method stub
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		String [] tempStringArray = getResources().getStringArray(R.array.nav_drawer_items_after);

		if(null==titleArrayList)
			titleArrayList=new ArrayList<String>();
		else
			titleArrayList.clear();


		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		String userMail=myPrefs.getString("mail", "User");	

		for (int i = 0; i <= (tempStringArray.length); i++) {
			if(i==0){
				titleArrayList.add(i, userMail);
			}
			else{
				titleArrayList.add(i, tempStringArray[i-1]);
			}
		}


		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons_after);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(0), navMenuIcons.getResourceId(0, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(1), navMenuIcons.getResourceId(1, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(2), navMenuIcons.getResourceId(2, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(3), navMenuIcons.getResourceId(3, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(4), navMenuIcons.getResourceId(4, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(5), navMenuIcons.getResourceId(5, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(6), navMenuIcons.getResourceId(6, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(7), navMenuIcons.getResourceId(7, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(8), navMenuIcons.getResourceId(8, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(9), navMenuIcons.getResourceId(9, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(10), navMenuIcons.getResourceId(10, -1)));


		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems,true);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
				) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		displayView(1);
	}

	private void beforeLoginView(){
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		String [] tempArray = getResources().getStringArray(R.array.nav_drawer_items_before);

		if(null==titleArrayList)
			titleArrayList=new ArrayList<String>();
		else
			titleArrayList.clear();

		for (int i = 0; i < tempArray.length; i++) {
			titleArrayList.add(tempArray[i]);
		}

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons_before);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(0), navMenuIcons.getResourceId(0, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(1), navMenuIcons.getResourceId(1, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(2), navMenuIcons.getResourceId(2, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(3), navMenuIcons.getResourceId(3, -1)));
		navDrawerItems.add(new NavDrawerItem(titleArrayList.get(4), navMenuIcons.getResourceId(4, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems,false);
		mDrawerList.setAdapter(adapter);

		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
				) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		displayView(0);
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
	ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			if(uid!=-1&&position==0)
			{

			}
			else{
				if(customGardenName!=null)
					customGardenName=null;
				SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("custom_garden_id", "none");
				editor.commit();
				displayView(position);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
	/*	case R.id.action_settings:
			return true;*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		SharedPreferences myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		//		Log.e("editor", myPrefs.getString("demo", "null"));

		uid = myPrefs.getInt("uid", -1);


		if(uid==-1){
			fragment=beforeLoginSwitch(fragment, position);
		}else{
			fragment=afterLoginSwitch(fragment, position);
		}


		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			if(customGardenName!=null)
				setTitle(Html.fromHtml(customGardenName));
			else
				setTitle(titleArrayList.get(position));
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	private Fragment beforeLoginSwitch(Fragment fragment,int position){
		switch (position) {
		case 0:
			fragment = new LoginPrompt();
			break;
		case 1:
			fragment =new StoreFragment(getApplicationContext());
			break;
		case 2:
			fragment = new AboutusFragment();
			break;
		case 3:
			Intent intentEmail = new Intent(Intent.ACTION_SEND);
			intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@sankalptaru.org"});
			intentEmail.putExtra(Intent.EXTRA_SUBJECT, "I have a support query from SankalpTaru Organic Greens");
			intentEmail.putExtra(Intent.EXTRA_TEXT, "");
			intentEmail.setType("message/rfc822");
			startActivity(Intent.createChooser(intentEmail, "Choose an email provider :"));	
			break;
		case 4:
			Intent intentSupport = new Intent(Intent.ACTION_SEND);
			intentSupport.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@sankalptaru.org"});
			intentSupport.putExtra(Intent.EXTRA_SUBJECT, "I have a feedback for SankalpTaru Organic Greens");
			intentSupport.putExtra(Intent.EXTRA_TEXT, "");
			intentSupport.setType("message/rfc822");
			startActivity(Intent.createChooser(intentSupport, "Choose an email provider :"));	
			break;

		default:
			break;
		}
		return fragment;
	}

	/*private void goToLoginView(){
		Toast.makeText(MainActivity.this, "Login to proceed.", 2000).show();
		Intent in =new Intent(MainActivity.this, LoginFragment.class);
		startActivity(in);
		mDrawerList.setItemChecked(0, true);
		mDrawerList.setSelection(0);
		mDrawerLayout.closeDrawer(mDrawerList);
	}*/

	private Fragment afterLoginSwitch(Fragment fragment,int position){
		switch (position) {
		case 0:

			break;
		case 1:
			SharedPreferences	myPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
			String custom_id=myPrefs.getString("custom_garden_id", "none");
			if(isHomeFragFirstTime&&!custom_id.equals("none")){
				fragment = new HomeFragment(getSupportFragmentManager());
				isHomeFragFirstTime=true;
			}else if(isHomeFragFirstTime){
				fragment = new HomeFragment(getSupportFragmentManager());
				isHomeFragFirstTime=false;
			}
			break;
		case 2:
			Intent in =new Intent(MainActivity.this, DiscussionActivity.class);
			startActivity(in);
			mDrawerList.setItemChecked(0, true);
			mDrawerList.setSelection(0);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 3:
			fragment = new CommunityFragment(MainActivity.this,getApplicationContext());
			isHomeFragFirstTime=true;
			break;
		case 4:
			Intent intent =new Intent(MainActivity.this, CalenderActivity.class);
			startActivity(intent);
			mDrawerList.setItemChecked(0, true);
			mDrawerList.setSelection(0);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 5:
			fragment =new StoreFragment(getApplicationContext());
			isHomeFragFirstTime=true;
			break;
		case 6:
			Intent intentVideo =new Intent(MainActivity.this, VideoFragment.class);
			startActivity(intentVideo);
			mDrawerList.setItemChecked(0, true);
			mDrawerList.setSelection(0);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 7:
			SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("uid", -1);
			editor.commit();
			mDrawerLayout.closeDrawers();
			uid=-1;
			beforeLoginView();
			deleteUserFilesFromSDCard(new File(Environment.getExternalStorageDirectory(),"/Organic Greens/"));
			break;
		case 8:
			fragment = new AboutusFragment();
			isHomeFragFirstTime=true;
			break;
		case 9:
			Intent intentEmail = new Intent(Intent.ACTION_SEND);
			intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@sankalptaru.org"});
			intentEmail.putExtra(Intent.EXTRA_SUBJECT, "I have a support query from SankalpTaru Organic Greens");
			intentEmail.putExtra(Intent.EXTRA_TEXT, "");
			intentEmail.setType("message/rfc822");
			startActivity(Intent.createChooser(intentEmail, "Choose an email provider :"));	
			break;
		case 10:
			Intent intentSupport = new Intent(Intent.ACTION_SEND);
			intentSupport.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@sankalptaru.org"});
			intentSupport.putExtra(Intent.EXTRA_SUBJECT, "I have a feedback for SankalpTaru Organic Greens");
			intentSupport.putExtra(Intent.EXTRA_TEXT, "");
			intentSupport.setType("message/rfc822");
			startActivity(Intent.createChooser(intentSupport, "Choose an email provider :"));	
			break;

		default:
			break;
		}

		return fragment;

	}

	private boolean deleteUserFilesFromSDCard(File path) {
		// TODO Auto-generated method stub
		if( path.exists() ) {
		      File[] files = path.listFiles();
		      if (files == null) {
		          return true;
		      }
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		        	 deleteUserFilesFromSDCard(files[i]);
		         }
		         else {
		           files[i].delete();
		         }
		      }
		    }
		    return( path.delete() );
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void renderKGWallFragment(String nameOfGarden,String nodeId){
		SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("custom_garden_id", nodeId);
		editor.commit();
		customGardenName=nameOfGarden;
		displayView(1);
	}

	public class ConfirmationDialog extends DialogFragment {

		public ConfirmationDialog( ) {

		}

		@Override
		public void onStart() {
			super.onStart();

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			return new AlertDialog.Builder(getActivity())
			// Set Dialog Icon
			.setIcon(android.R.drawable.ic_dialog_alert)
			// Set Dialog Title
			.setTitle("Exit Confirmation")
			// Set Dialog Message
			.setMessage("Are you sure, you want to exit ?")

			// Positive button
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Do something else
					dismiss();
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			})

			// Negative Button
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,    int which) {
					// Do something else
					dismiss();
				}
			}).create();
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		ConfirmationDialog confDlg= new ConfirmationDialog();
		confDlg.setCancelable(true);
		confDlg.show(getSupportFragmentManager(), "");
	}
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}
}
