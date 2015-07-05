package org.sankalptaru.kg.community;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;

class PostComment extends AsyncTask<String, Integer, JSONObject>{

		JSONObject jsonObject;
		SharedPreferences myPrefs;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
//			AppUtil.initializeProgressDialog(getActivity(), "Finalizing post...", progressDialog);
		}

		public PostComment(JSONObject jsObject, SharedPreferences myPrefs) {
			// TODO Auto-generated constructor stub
			this.jsonObject=jsObject;
			this.myPrefs=myPrefs;
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			String sessionDetails = myPrefs.getString("session_name", "")+"="+myPrefs.getString("sessid", "");

			JSONObject jsResponse = null;
			try {
				String s =AppUtil.doPost(params[0], jsonObject,myPrefs.getString("token", ""),true,sessionDetails);
				jsResponse = new JSONObject(s);
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
//			AppUtil.cancelProgressDialog();
			
		}
	}