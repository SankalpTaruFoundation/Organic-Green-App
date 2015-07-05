package org.sankalptaru.kg.community;

import android.os.AsyncTask;

public class CSRFToken extends AsyncTask<String, String, String> {
	public CSRFToken() {
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}
	@Override
	protected String doInBackground(String... arg0) {
		String resString=AppUtil.getResponse(arg0[0]);
		return resString;
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
	}


}