package org.sankalptaru.kg.community;

import org.sankalptaru.kg.community.adapter.TabsPagerAdapter;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

public class LoginFragment extends FragmentActivity implements
ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Login", "Register","Reset Password" };
	private ProgressDialog progressDialog;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home:
			backToMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void hideSoftKeyboard(){
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.login_fragment);
		
		String pagename=(String) getIntent().getExtras().get("pagename");

		/*AppUtil.SuperRestartServiceIntent = PendingIntent.getActivity(getBaseContext(), 0,
				new Intent(getIntent()), getIntent().getFlags());*/
		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(),LoginFragment.this,getApplicationContext());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));



		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		
		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		if(pagename.equals("register"))
			changeToRegisterPage();
		else if (pagename.equals("reset_pswd")) {
			changeToResetPasswordPage();
		}
	}


	private void changeToResetPasswordPage() {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(2);
	}
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public void backToMenu(){
		Intent in=new Intent(LoginFragment.this, MainActivity.class);
		startActivity(in);
		finish();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
	}

	public void changePageToLogin(){
		viewPager.setCurrentItem(0);
	}
	
	public void changeToRegisterPage(){
		viewPager.setCurrentItem(1);
	}
	
	public void initializeProgressBar(String textToShow){
		AppUtil.initializeProgressDialog(LoginFragment.this, textToShow, progressDialog);
	}
	
	public void cancelProgressBar(){
		AppUtil.cancelProgressDialog();
	}
}
