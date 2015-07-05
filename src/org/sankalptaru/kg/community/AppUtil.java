package org.sankalptaru.kg.community;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;

public class AppUtil {
	private static Dialog activityDialog;
	private static AnimationDrawable animationDrawable;
	private static Activity activity;
	public static final String JSON_FILE_PATH="/Organic Greens/Util/";
	//	static PendingIntent SuperRestartServiceIntent;

	public static long getDateInUNIXTimeStamp(int year,int month,int day,int hr,int min,int sec,int milli){

//		Log.e("data in AppUtil", "Y "+year+" M "+month+" D "+day+" hr "+hr+" m "+min);
		
		Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hr);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, milli);
        return cal.getTimeInMillis();
	}
	
	public static String getUserPrimaryAccountDetails(Context context){
		String userEmail=null;
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				userEmail = account.name;
				break;
			}
		}
		Log.e("userEmail", userEmail);
		return userEmail;
	}
	
	public static String getGalleryPath(Intent data,Context ac){
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = ac.getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}

	public static String doPost(String url,JSONObject jsonObject,String CSRFToken,boolean isComplexPost,String cookie){
		HttpClient httpclient = new DefaultHttpClient();   

		HttpPost httppost = new HttpPost(url);  
		String responseString = null;
		try{

			// json.put("api_token",settings.getString("api_token", "")); 
			StringEntity se = new StringEntity(jsonObject.toString()); 
			httppost.setEntity(se); 
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");
			httppost.addHeader("X-CSRF-Token", CSRFToken);
			if(isComplexPost)
				httppost.setHeader("Cookie", cookie);

			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json")); 


			HttpResponse response = httpclient.execute(httppost);   
			HttpEntity responseEntity =response.getEntity(); 
			responseString= EntityUtils.toString(responseEntity).trim();

			Log.d("Response", responseString); 
		} catch (UnsupportedEncodingException UnsupportedEncodingException) { 
			// TODO Auto-generated catch block 
			Log.e("UnsupportedEncodingException",UnsupportedEncodingException.getMessage()); 
		} catch (ClientProtocolException ClientProtocolException) { 
			// TODO Auto-generated catch block 
			Log.e("ClientProtocolException", ClientProtocolException.getMessage()); 
		} catch (IOException IOException) { 
			// TODO Auto-generated catch block 
			Log.e("IOException", IOException.getMessage()); 
		}
		return responseString; 
	}


	public static String doPutJsonObject(String url,JSONObject jsonObject,String CSRFToken,boolean isComplexPost,String cookie){
		Log.e("doPutJsonObject", "doPutJsonObject");
		HttpClient httpclient = new DefaultHttpClient();   
		HttpPut httpput = new HttpPut(url);  
		String responseString = null;
		try{ 

			// json.put("api_token",settings.getString("api_token", "")); 
			StringEntity se = new StringEntity(jsonObject.toString()); 
			httpput.setEntity(se); 
			httpput.setHeader("Accept", "application/json");
			httpput.setHeader("Content-type", "application/json");
			httpput.addHeader("X-CSRF-Token", CSRFToken);
			if(isComplexPost)
				httpput.setHeader("Cookie", cookie);
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json")); 


			HttpResponse response = httpclient.execute(httpput);   
			HttpEntity responseEntity =response.getEntity(); 
			responseString= EntityUtils.toString(responseEntity).trim();

			Log.d("Response", responseString); 
		} catch (UnsupportedEncodingException UnsupportedEncodingException) { 
			// TODO Auto-generated catch block 
			Log.e("UnsupportedEncodingException",UnsupportedEncodingException.getMessage()); 
		} catch (ClientProtocolException ClientProtocolException) { 
			// TODO Auto-generated catch block 
			Log.e("ClientProtocolException", ClientProtocolException.getMessage()); 
		} catch (IOException IOException) { 
			// TODO Auto-generated catch block 
			Log.e("IOException", IOException.getMessage()); 
		}
		return responseString; 

	}

	public static boolean isEmailValid(String email) { 
		boolean isValid = false; 

		String expression = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}"; 
		CharSequence inputStr = email; 

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE); 
		Matcher matcher = pattern.matcher(inputStr); 
		if (matcher.matches()) { 
			isValid = true; 
		} 
		return isValid; 
	}
	public static String getResponse(String data){
		String downloadedData = null;

		Log.e("getResponse", data);
		try {
			URL downloadURL = new URL(data);
			InputStream inputStream = (InputStream) downloadURL.getContent();
			if (null != inputStream) {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[512];
				int readCounter = inputStream.read(buffer);
				while (readCounter != -1) {
					byteArrayOutputStream.write(buffer, 0, readCounter);
					readCounter = inputStream.read(buffer);
				}
				downloadedData = new String(
						byteArrayOutputStream.toByteArray());
				/*if (null != downloadedData && !"".equals(downloadedData)) {
						downloadedJson = new JSONObject(downloadedData);
					}*/
			}else{
				Log.e("getResponse", "Response is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return downloadedData;
	}


	public static void initializeProgressDialog(Activity ac,String textToShow,Dialog progressDialog){
		if (progressDialog == null)
		{
			progressDialog = new Dialog(ac, R.style.Theme_Dialog_Translucent);
			progressDialog.requestWindowFeature(1);
			progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			activityDialog=progressDialog;
		}
		progressDialog.setContentView(R.layout.loading_progress);
		progressDialog.setCancelable(false);
		if ((progressDialog != null) && (progressDialog.isShowing()))
			progressDialog.dismiss();
		progressDialog.show();
		ImageView localImageView = (ImageView)progressDialog.findViewById(R.id.imgeView);
		TextView localTextView = (TextView)progressDialog.findViewById(R.id.tvLoading);
		localTextView.setText(textToShow);
		/* if (!this.strrMsg.equalsIgnoreCase(""))
	          localTextView.setText(this.strrMsg);*/
		localImageView.setBackgroundResource(R.anim.frame);
		animationDrawable = ((AnimationDrawable)localImageView.getBackground());
		if (animationDrawable != null)
			animationDrawable.start();

	}
	public static void cancelProgressDialog(){
		activityDialog.cancel();
	}

	public static Editor getEditor(){
		SharedPreferences pref = activity.getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
		return editor;
	}

	public static String getDate(long unixSeconds){
		Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd-MM-yyyy hh:mm a"); // the format of your date
		//		sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public static String getTimeDifference(long secs){

		long diff = new java.util.Date().getTime() - new Date(secs*1000L).getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffDays = diff / (24 * 60 * 60 * 1000);


		String finalTime="";
		String days="days";
		String hrs="hrs";
		String mins="mins";
		String seconds="secs";

		if(diffDays==1)
			days="day";

		if(diffMinutes==1)
			mins="min";

		if(diffHours==1)
			hrs="hr";

		if(diffSeconds==1)
			seconds="sec";

		if(diffDays>0){
			finalTime=finalTime+diffDays+" "+days+" "+diffHours+" "+hrs+" ago";
			return finalTime;
		}

		if(diffHours>0){
			finalTime=finalTime+diffHours+" "+hrs+" "+diffMinutes+" "+mins+" ago" ;
			return finalTime;
		}

		if(diffMinutes>0){
			finalTime=finalTime+diffMinutes+" "+mins+" "+diffSeconds+" "+seconds+" ago" ;
			return finalTime;
		}

		if(diffSeconds>0){
			finalTime=finalTime+diffSeconds+" "+seconds+" ago" ;
			return finalTime;
		}

		return finalTime;
	}

	public static double getTimeDifferenceForLogin(long secs){

		long diff = new java.util.Date().getTime() - new Date(secs*1000L).getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffDays = diff / (24 * 60 * 60 * 1000);

		Log.e("login was:", diffHours+" h "+diffMinutes+" m "+diffSeconds+"s");
		
		Double actualTimeToLogin= Double.parseDouble(diffHours+"."+diffMinutes);
		
		return actualTimeToLogin;
	}

	public static String getBase64StringOfFile(File fileName){
		String encodedString=null;
		try {
			InputStream inputStream = new FileInputStream(fileName);//You can get an inputStream using any IO API
			byte[] bytes;
			byte[] buffer = new byte[8192];
			int bytesRead;
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			bytes = output.toByteArray();
			encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodedString;
	}
	
	public static void createFolderInSDCard() {
		File direct = new File(Environment.getExternalStorageDirectory(),JSON_FILE_PATH);

		if(!direct.exists())
		{
			if(direct.mkdirs()) 
			{
				Log.e("created", "created");
			}
		}
	}

	public static void createFile(String filepath){
		File historyFile=new File(Environment.getExternalStorageDirectory(),filepath);
		try {
			if(historyFile.exists()){
				historyFile.delete();
				historyFile.createNewFile();
			}else{
				historyFile.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeToFile(String content,String filepath) {
		File historyJsonFile=new File(Environment.getExternalStorageDirectory(),filepath);
		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(historyJsonFile,true));
			try {
				output.write(content);
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public static String readFile(File file) {
		String jsonStr = null;
		try {
//			File yourFile = new File(Environment.getExternalStorageDirectory(),filepath);
			FileInputStream stream = new FileInputStream(file);
			try {
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

				jsonStr = Charset.defaultCharset().decode(bb).toString();
			}
			finally {
				stream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	
}
