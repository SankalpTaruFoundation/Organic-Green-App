package org.sankalptaru.kg.community;

import android.app.AlarmManager;
import android.app.Application;
import android.util.Log;

public class CommunityApplication extends Application {

	private Thread.UncaughtExceptionHandler mOnRuntimeError;

	@Override
	public void onCreate() {
		super.onCreate();

		mOnRuntimeError = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.e("Organic Greens Application","crash caught");
				AlarmManager mgr = (AlarmManager) getSystemService(
						getApplicationContext().ALARM_SERVICE);
				/*mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, 
						AppUtil.SuperRestartServiceIntent);*/
				ex.printStackTrace();
				System.exit(2);
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(mOnRuntimeError);
	}
}
