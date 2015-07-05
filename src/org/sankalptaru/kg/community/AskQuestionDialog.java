package org.sankalptaru.kg.community;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sankalptaru.kg.community.helper.CalenderHelper;
import org.sankalptaru.kg.community.helper.ConnectionDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class AskQuestionDialog extends DialogFragment{

	private static final int RESULT_LOAD_POST_IMAGE_QUEST = 5;
	private static final int REQUEST_CODE_QUEST_CLICK_IMAGE = 6;
	private String sharedImagePath;
	private Dialog progressDialog;
	private String sub;
	private String des;
	private int selectedCategoryIndex;
	private SharedPreferences myPrefs;
	private ConnectionDetector cd;
	private AlertDialogManager alert=new AlertDialogManager();
	private Context context;
	private String cameraImagePath=null;

	public AskQuestionDialog(Context applicationContext) {
		// TODO Auto-generated constructor stub
		context=applicationContext;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.ask_question_layout, container,
				false);
		getDialog().setTitle("Ask Question"); 
//		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		myPrefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		Window window = getDialog().getWindow();
		window.setBackgroundDrawableResource(android.R.color.white);
		final Spinner category=(Spinner)rootView.findViewById(R.id.categorySpinner);

		ArrayList<String> list = new ArrayList<String>();
		list.add("Share Your Experiences");
		list.add("Problems & Help");
		list.add("Do's & Dont's");
		list.add("Pest Control");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,list);

		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		category.setAdapter(dataAdapter);
		final EditText subject=(EditText)rootView.findViewById(R.id.statusSubjectTxt);
		final EditText description=(EditText)rootView.findViewById(R.id.statusCommentTxt);

		ImageView addPostCamImage =(ImageView)rootView.findViewById(R.id.addPostCamImage);

		addPostCamImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveCameraImage(false);
			}
		});

		ImageView addImage=(ImageView)rootView.findViewById(R.id.addPostImage);
		addImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_POST_IMAGE_QUEST);
			}
		});

		ImageView postQuestion=(ImageView)rootView.findViewById(R.id.postStatus);
		postQuestion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sub=subject.getText().toString().trim();
				des=description.getText().toString().trim();

				if(sub.length()>0&&des.length()>0){
					selectedCategoryIndex=category.getSelectedItemPosition();
					if(sharedImagePath==null){
						AlertQuestionFragment dialog=new AlertQuestionFragment("Info","Image not added to post.\n\nNote: Sharing your image with your status is recommended.","Proceed Anyway","Cancel",false,"not_added");
						dialog.setCancelable(true);
						dialog.show(getFragmentManager(), "postQuestion");
					}
					else{
						uploadImageAndPost();
					}
				}else{
					AlertQuestionFragment dialog=new AlertQuestionFragment("Warning","All fields are mandatory to be filled.","OK","Proceed",true,"field_check");
					dialog.setCancelable(true);
					dialog.show(getFragmentManager(), "fieldQuestion");
				}
			}
		});

		return rootView;
	}
	protected void uploadImageAndPost() {
		// TODO Auto-generated method stub
		JSONObject js= new JSONObject();

		File imageFile = new File(sharedImagePath);
		Random rand=new Random();
		if(imageFile.exists()){
			String fileName=System.currentTimeMillis()+""+rand.nextInt()+".jpeg";

			try {
				Log.e("filename", ""+fileName);
				js.put("filename", fileName);
				js.put("uid",myPrefs.getInt("uid", -1));
				js.put("file", AppUtil.getBase64StringOfFile(imageFile));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cd = new ConnectionDetector(context);

			// Check if Internet present
			if (!cd.isConnectingToInternet()) {
				// Internet Connection is not present
				alert.showAlertDialog(getActivity(),
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				// stop executing code by return
				return;
			}else{
				new PostImage(js,myPrefs){


					protected void onPreExecute() {
						AppUtil.initializeProgressDialog(getActivity(), "Uploading Image...", progressDialog);
					};

					protected void onPostExecute(JSONObject result) {
						if(null!=result){
							AppUtil.cancelProgressDialog();
							try {
								doPostQuestion(result.optString("fid"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				}.execute("http://www1.kitchengardens.in/svc/file/");
			}
		}

	}
	protected void saveCameraImage(boolean isCover) {
		// TODO Auto-generated method stub
		File newdir =new File(Environment.getExternalStorageDirectory().getPath(), "/Organic Greens/Images");
		if(!newdir.exists())
			newdir.mkdirs();

		cameraImagePath=Environment.getExternalStorageDirectory().getPath()+ "/Organic Greens/Images/"+new Date().getTime()+".jpeg";
		File newfile = new File(cameraImagePath);
		try {
			newfile.createNewFile();
		} catch (IOException e) {}       

		Uri outputFileUri = Uri.fromFile(newfile);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, REQUEST_CODE_QUEST_CLICK_IMAGE);

	}

	protected void doPostQuestion(String fid) throws JSONException {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("uid", myPrefs.getInt("uid", -1));
		jsonObject.put("type", "discussion");
		jsonObject.put("title_field", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", sub))));
		
		SharedPreferences myPrefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE); 
		String gardenName=myPrefs.getString("discussion_user", "none");
		
		jsonObject.put("field_garden_name", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", gardenName))));
		jsonObject.put("body", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("value", des))));
		jsonObject.put("field_discussion_category",  new JSONObject().put("und", getCategory(selectedCategoryIndex)));
		if(null!=fid)
			jsonObject.put("field_image", new JSONObject().put("und", new JSONArray().put(0, new JSONObject().put("fid", fid))));

		cd = new ConnectionDetector(context);

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(getActivity(),
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}else{
			Log.e("selectedCategoryIndex", ""+selectedCategoryIndex+" "+jsonObject);

			new PostComment(jsonObject,myPrefs){

				protected void onPreExecute() {
					AppUtil.initializeProgressDialog(getActivity(), "Finalizing post...", progressDialog);
				};

				protected void onPostExecute(JSONObject result) {
					AppUtil.cancelProgressDialog();
					if(result!=null){
						CalenderHelper calenderHelper=new CalenderHelper(context);
						Calendar cal=Calendar.getInstance();
						Date date=new Date();
						calenderHelper.createEvent("Question asked under "+getCategory(selectedCategoryIndex)+" category", "Question: "+sub+"\n\nDescription:"+des, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND),cal.get(Calendar.MILLISECOND),date.getTime(),date.getTime(),null);

						AlertQuestionFragment dialog=new AlertQuestionFragment("Success","Questions has been posted successfully.","OK","Proceed",true,"field_check");
						dialog.setCancelable(true);
						dialog.show(getFragmentManager(), "");
						getDialog().dismiss();
					}
					sharedImagePath=null;
				};

			}.execute("http://www1.kitchengardens.in/svc/node/");
		}
	}
	private String getCategory(int selectedCategory) {
		// TODO Auto-generated method stub
		String category=null;

		switch (selectedCategory) {
		case 0:
			category="experience";
			break;

		case 1:
			category="problems";
			break;

		case 2:
			category="do";
			break;

		case 3:
			category="pest";
			break;
		}

		return category;
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_LOAD_POST_IMAGE_QUEST && resultCode == getActivity().RESULT_OK && null != data){
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			compressCamBitmap(AppUtil.getGalleryPath(data,getActivity()));
		}else if(requestCode == REQUEST_CODE_QUEST_CLICK_IMAGE  && resultCode == getActivity().RESULT_OK){
			Toast.makeText(getActivity(), "Picture sucessfully added.", 2000).show();
			compressCamBitmap(cameraImagePath);
		}
	}
	private void compressCamBitmap(String imagePath) {
		// TODO Auto-generated method stub
		new ImageCompressionAsyncTask(true,getActivity()){
			protected void onPostExecute(String result) {
				sharedImagePath=result;
				if(cameraImagePath!=null){
					File tempFile=new File(cameraImagePath);
					tempFile.delete();
					cameraImagePath=null;
				}
			};

		}.execute(imagePath);
	}

	public class AlertQuestionFragment extends DialogFragment {

		private String title; 
		private String message; 
		private String positivetxt;
		private String negativeTxt;
		private boolean setNegativeInvisible;
		private String positiveBtnLogicalHub;

		public AlertQuestionFragment( String title, String message, String positivetxt,String negativeTxt, boolean setNegativeInvisible, String positiveBtnLogicalHub) {
			// TODO Auto-generated constructor stub
			this.title=title;
			this.message=message;
			this.positivetxt=positivetxt;
			this.negativeTxt=negativeTxt;
			this.setNegativeInvisible=setNegativeInvisible;
			this.positiveBtnLogicalHub=positiveBtnLogicalHub;
		}

		@Override
		public void onStart() {
			super.onStart();
			if(setNegativeInvisible){
				AlertDialog d = (AlertDialog) getDialog();
				Button negativeBtn = (Button)d.getButton(Dialog.BUTTON_NEGATIVE);
				negativeBtn.setVisibility(View.GONE);
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			return new AlertDialog.Builder(getActivity())
			// Set Dialog Icon
			.setIcon(android.R.drawable.ic_dialog_alert)
			// Set Dialog Title
			.setTitle(title)
			// Set Dialog Message
			.setMessage(message)

			// Positive button
			.setPositiveButton(positivetxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(positiveBtnLogicalHub.equals("not_added")){
						try {
							doPostQuestion(null);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			})

			// Negative Button
			.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,    int which) {
					// Do something else

				}
			}).create();
		}

	}
}
