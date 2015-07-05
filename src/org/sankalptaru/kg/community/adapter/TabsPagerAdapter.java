package org.sankalptaru.kg.community.adapter;

import org.sankalptaru.kg.community.LoginFragment;
import org.sankalptaru.kg.community.LoginSubFragment;
import org.sankalptaru.kg.community.RegisterFragment;
import org.sankalptaru.kg.community.ResetPasswordFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	FragmentManager fm;
	LoginFragment loginFragment;
	Context context;

	public TabsPagerAdapter(FragmentManager fm, LoginFragment loginFragment, Context context) {
		super(fm);
		this.fm=fm;
		this.loginFragment=loginFragment;
		this.context=context;
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			return new LoginSubFragment(fm,loginFragment,context);
		case 1:
			return new RegisterFragment(fm,loginFragment,context);
		case 2:
			return new ResetPasswordFragment(fm,loginFragment,context);
		}
		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
