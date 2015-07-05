package org.sankalptaru.kg.community;

import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;

class PostImage extends AsyncTask<String, Integer, JSONObject>{

	JSONObject js;
	SharedPreferences myPrefs;
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
	}
	public PostImage(JSONObject js, SharedPreferences myPrefs) {
		// TODO Auto-generated constructor stub
		this.js=js;
		this.myPrefs=myPrefs;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		// TODO Auto-generated method stub
		String sessionDetails = myPrefs.getString("session_name", "")+"="+myPrefs.getString("sessid", "");

		JSONObject jsResponse = null;
		try {
			String s =AppUtil.doPost(params[0], js,myPrefs.getString("token", ""),true,sessionDetails);
			jsResponse = new JSONObject(s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsResponse;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
		
	}
}