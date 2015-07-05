package org.sankalptaru.kg.community;

import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;

public class UpdateLike extends AsyncTask<String, Integer, String> {

	private JSONObject jsonObject;
	private SharedPreferences myPrefs;
	public UpdateLike(JSONObject jsonObject, SharedPreferences myPrefs) {
		// TODO Auto-generated constructor stub
		this.jsonObject=jsonObject;
		this.myPrefs=myPrefs;
	}
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}
	@Override
	protected String doInBackground(String... params) {
		String sessionDetails = myPrefs.getString("session_name", "")+"="+myPrefs.getString("sessid", "");

		String jsResponse = null;
		jsResponse =AppUtil.doPutJsonObject(params[0], jsonObject,myPrefs.getString("token", ""),true,sessionDetails);

		return jsResponse;
	}
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
}
