package org.sankalptaru.kg.community;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sankalptaru.kg.community.helper.CalenderHelper;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

@SuppressLint("SimpleDateFormat")
public class CalenderActivity extends FragmentActivity {
	private Spinner fromDateSpinner;
	private Spinner toDateSpinner;
	private Spinner fromTimeSpinner;
	private Spinner toTimeSpinner;
	private boolean undo = false;
	private CaldroidFragment caldroidFragment;
	private CaldroidFragment dialogCaldroidFragment;
	private ArrayList<String> startDates;
	private ArrayList<String> descriptions;
	private ArrayList<String> endDates;
	private ArrayList<String> nameOfEvent;
	private ArrayList<String> eventIdList;
	private CalenderHelper calenderHelper;
	private android.app.DialogFragment eventsDialog;
	private SimpleDateFormat formatter;
	private ActionBar actionBar;
	private int currentSpinnerID;
	private int currentTimeSpinnerID;
	private String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private android.app.DialogFragment addEventWithReminderDialog;
	private void setCustomResourceForDates(long startDate) {
		Date greenDate = new Date(startDate);

		if (caldroidFragment != null) {
			caldroidFragment.setBackgroundResourceForDate(android.R.color.holo_orange_light,
					greenDate);
			caldroidFragment.setTextColorForDate(android.R.color.white, greenDate);
		}
		caldroidFragment.refreshView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			return true;

		case R.id.action_add_reminder:
			openAddReminderDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openAddReminderDialog() {
		// TODO Auto-generated method stub
		addEventWithReminderDialog=new AddEventWithReminderDialog();
		addEventWithReminderDialog.setCancelable(true);
		addEventWithReminderDialog.show(getFragmentManager(), "");
	}


	private class AddEventWithReminderDialog extends android.app.DialogFragment{
		public AddEventWithReminderDialog() {
			// TODO Auto-generated constructor stub

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_events_reminder_dialog, container,
					false);
			Window window = getDialog().getWindow();

			getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
			window.setBackgroundDrawableResource(android.R.color.transparent);

			fromDateSpinner=(Spinner)rootView.findViewById(R.id.fromDateSpinner);
			fromDateSpinner.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					currentSpinnerID=1;
					showDialog(999);
					return true;
				}
			});


			toDateSpinner=(Spinner)rootView.findViewById(R.id.toDateSpinner);
			toDateSpinner.setOnTouchListener(new OnTouchListener() {



				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					currentSpinnerID=2;
					showDialog(999);
					return true;
				}
			});


			fromTimeSpinner=(Spinner)rootView.findViewById(R.id.fromTimeSpinner);
			fromTimeSpinner.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					currentTimeSpinnerID=1;
					showDialog(1000);
					return true;
				}
			});

			toTimeSpinner=(Spinner)rootView.findViewById(R.id.toTimeSpinner);
			toTimeSpinner.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					currentTimeSpinnerID=2;
					showDialog(1000);
					return true;
				}
			});


			final Spinner reminderSpinner=(Spinner)rootView.findViewById(R.id.reminderSpinner);


			ArrayList<String> list=new ArrayList<String>();
			list.add("NONE");
			list.add("DAILY");
			list.add("WEEKLY");
			list.add("MONTHLY");
			list.add("YEARLY");

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CalenderActivity.this, android.R.layout.simple_spinner_item,list);

			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			reminderSpinner.setAdapter(dataAdapter);

			final EditText eventNameTxt=(EditText)rootView.findViewById(R.id.eventNameTxt);

			final EditText eventDescTxt=(EditText)rootView.findViewById(R.id.eventDescTxt);

			Button submitReminderBtn=(Button)rootView.findViewById(R.id.submitReminderBtn);

			submitReminderBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//					if(eventDesc.toString().length()>0&&eventName.toString().length()>0){

					String fromdate = null;
					if(null!=fromDateSpinner.getTag())
						fromdate= fromDateSpinner.getTag().toString();

					String todate = null;
					if(null!=toDateSpinner.getTag())
						todate=toDateSpinner.getTag().toString();


					String fromTime = null;
					if(null!=fromTimeSpinner.getTag())
						fromTime=fromTimeSpinner.getTag().toString();

					String toTime = null;
					if(null!=toTimeSpinner.getTag())
						toTime=toTimeSpinner.getTag().toString();


					if(eventNameTxt.getText().length()>0 && eventDescTxt.getText().length()>0 ){
						if(null!=fromdate && null!=fromTime && null!=todate && null!=toTime){
							int year=Integer.parseInt(fromdate.split(":")[2]);
							int month=Integer.parseInt(fromdate.split(":")[1]);
							int day=Integer.parseInt(fromdate.split(":")[0]);
							int hr=Integer.parseInt(fromTime.split(":")[0]);
							int min=Integer.parseInt(fromTime.split(":")[1]);

							long startTime=AppUtil.getDateInUNIXTimeStamp(year,month,day,hr,min, 0, 0);

							long endTime=AppUtil.getDateInUNIXTimeStamp(Integer.parseInt(todate.split(":")[2]), Integer.parseInt(todate.split(":")[1]),Integer.parseInt(todate.split(":")[0]), Integer.parseInt(toTime.split(":")[0]), Integer.parseInt(toTime.split(":")[1]), 0, 0);

							String freqString = reminderSpinner.getSelectedItem().toString();

							if(freqString.equals("NONE"))
								freqString=null;

							if(startTime<endTime){
								calenderHelper=new CalenderHelper(CalenderActivity.this);
								calenderHelper.pushAppointmentsToCalender(eventNameTxt.getText().toString(), eventDescTxt.getText().toString(), 1, startTime,endTime, true, true,freqString);
								addEventWithReminderDialog.dismiss();
								Intent intent = getIntent();
							    finish();
							    startActivity(intent);
							}
							else{
								Toast.makeText(CalenderActivity.this, "Reminder TO date should be greater than FROM date.", 2000).show();
							}
						}else{
							Toast.makeText(CalenderActivity.this, "All fields are mandatory.", 2000).show();
						}
					}else{
						Toast.makeText(CalenderActivity.this, "All fields are mandatory.", 2000).show();
					}
				}
			});


			//			 showDialog(999);
			return rootView;
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		Calendar calendar = Calendar.getInstance();
		if (id == 999) {
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this, myDateListener, year, month, day);
		}
		if(id==1000){
			return new TimePickerDialog(this, myTimeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
		}
		return null;
	}

	private TimePickerDialog.OnTimeSetListener myTimeListener= new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			ArrayList<String> list=new ArrayList<String>();

			String meridianString;

			int orignalhr=hourOfDay;

			if(hourOfDay>12){
				meridianString="PM";
				hourOfDay=hourOfDay-12;
			}else{
				meridianString="AM";
			}

			String min = null;
			if(minute<10){
				min="0"+minute;
			}else{
				min=""+minute;
			}



			if(currentTimeSpinnerID==1){
				list.add(hourOfDay+" : "+min+" "+meridianString);

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CalenderActivity.this, android.R.layout.simple_spinner_item,list);

				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				fromTimeSpinner.setAdapter(dataAdapter);
				fromTimeSpinner.setTag(orignalhr+":"+minute);
			}

			else if (currentTimeSpinnerID==2) {
				list.add(hourOfDay+" : "+min+" "+meridianString);

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CalenderActivity.this, android.R.layout.simple_spinner_item,list);

				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				toTimeSpinner.setAdapter(dataAdapter);
				toTimeSpinner.setTag(orignalhr+":"+minute);
			}
		}
	};



	private DatePickerDialog.OnDateSetListener myDateListener
	= new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker datepicker, int year, int month, int day) {
			// TODO Auto-generated method stub
			// arg1 = year
			// arg2 = month
			// arg3 = day
			//	      showDate(arg1, arg2+1, arg3);


			ArrayList<String> list=new ArrayList<String>();
			if(currentSpinnerID==1){
				list.add(datepicker.getDayOfMonth()+"-"+MONTHS[datepicker.getMonth()]+"-"+datepicker.getYear());

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CalenderActivity.this, android.R.layout.simple_spinner_item,list);

				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				fromDateSpinner.setAdapter(dataAdapter);
				fromDateSpinner.setTag(day+":"+month+":"+year);
			}

			else if (currentSpinnerID==2) {
				list.add(datepicker.getDayOfMonth()+"-"+MONTHS[datepicker.getMonth()]+"-"+datepicker.getYear());

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CalenderActivity.this, android.R.layout.simple_spinner_item,list);

				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				toDateSpinner.setAdapter(dataAdapter);
				toDateSpinner.setTag(day+":"+month+":"+year);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calender_layout);
		actionBar = getActionBar();
		actionBar.setTitle("Scheduler");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#508cbf26")));
		// Hide the action bar title
		actionBar.setDisplayShowTitleEnabled(true);

		formatter = new SimpleDateFormat("dd MMM yyyy");

		// Setup caldroid fragment
		// **** If you want normal CaldroidFragment, use below line ****
		caldroidFragment = new CaldroidFragment();

		// //////////////////////////////////////////////////////////////////////
		// **** This is to show customized fragment. If you want customized
		// version, uncomment below line ****
		//		 caldroidFragment = new CaldroidSampleCustomFragment();

		// Setup arguments

		// If Activity is created after rotation
		if (savedInstanceState != null) {
			caldroidFragment.restoreStatesFromKey(savedInstanceState,
					"CALDROID_SAVED_STATE");
		}
		// If activity is created from fresh
		else {
			Bundle args = new Bundle();
			Calendar cal = Calendar.getInstance();
			args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
			args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
			args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
			args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

			//			 Uncomment this to customize startDayOfWeek
			args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
					CaldroidFragment.MONDAY); // Monday

			//			 Uncomment this line to use Caldroid in compact mode
			//			args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

			caldroidFragment.setArguments(args);
		}

		//		setCustomResourceForDates();

		// Attach to the activity
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.calendar1, caldroidFragment);
		t.commit();

		// Setup listener
		final CaldroidListener listener = new CaldroidListener() {

			@Override
			public void onSelectDate(Date date, View view) {
				checkForEventForDate(date);
			}

			@Override
			public void onChangeMonth(int month, int year) {

			}

			@Override
			public void onLongClickDate(Date date, View view) {

			}

			@Override
			public void onCaldroidViewCreated() {
				if (caldroidFragment.getLeftArrowButton() != null) {
					/*Toast.makeText(getApplicationContext(),
							"Caldroid view is created", Toast.LENGTH_SHORT)
							.show();*/
				}
			}

		};

		// Setup Caldroid
		caldroidFragment.setCaldroidListener(listener);
		doImportantCalenderOperations();

		final TextView textView = (TextView) findViewById(R.id.textview);

		final Button customizeButton = (Button) findViewById(R.id.customize_button);
		customizeButton.setVisibility(View.GONE);
		// Customize the calendar
		customizeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (undo) {
					customizeButton.setText("Customize");
					textView.setText("");

					// Reset calendar
					caldroidFragment.clearDisableDates();
					caldroidFragment.clearSelectedDates();
					caldroidFragment.setMinDate(null);
					caldroidFragment.setMaxDate(null);
					caldroidFragment.setShowNavigationArrows(true);
					caldroidFragment.setEnableSwipe(true);
					caldroidFragment.refreshView();
					undo = false;
					return;
				}

				// Else
				undo = true;
				customizeButton.setText("Undo");
				Calendar cal = Calendar.getInstance();

				// Min date is last 7 days
				cal.add(Calendar.DATE, -7);
				Date minDate = cal.getTime();

				// Max date is next 7 days
				cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 14);
				Date maxDate = cal.getTime();

				// Set selected dates
				// From Date
				cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 2);
				Date fromDate = cal.getTime();

				// To Date
				cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 3);
				Date toDate = cal.getTime();

				// Set disabled dates
				ArrayList<Date> disabledDates = new ArrayList<Date>();
				for (int i = 5; i < 8; i++) {
					cal = Calendar.getInstance();
					cal.add(Calendar.DATE, i);
					disabledDates.add(cal.getTime());
				}

				// Customize
				caldroidFragment.setMinDate(minDate);
				caldroidFragment.setMaxDate(maxDate);
				caldroidFragment.setDisableDates(disabledDates);
				caldroidFragment.setSelectedDates(fromDate, toDate);
				caldroidFragment.setShowNavigationArrows(false);
				caldroidFragment.setEnableSwipe(false);

				caldroidFragment.refreshView();

				// Move to date
				// cal = Calendar.getInstance();
				// cal.add(Calendar.MONTH, 12);
				// caldroidFragment.moveToDate(cal.getTime());

				String text = "Today: " + formatter.format(new Date()) + "\n";
				text += "Min Date: " + formatter.format(minDate) + "\n";
				text += "Max Date: " + formatter.format(maxDate) + "\n";
				text += "Select From Date: " + formatter.format(fromDate)
						+ "\n";
				text += "Select To Date: " + formatter.format(toDate) + "\n";
				for (Date date : disabledDates) {
					text += "Disabled Date: " + formatter.format(date) + "\n";
				}

				textView.setText(text);
			}
		});

		Button showDialogButton = (Button) findViewById(R.id.show_dialog_button);
		showDialogButton.setVisibility(View.GONE);
		final Bundle state = savedInstanceState;
		showDialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				CalenderHelper ch=new CalenderHelper(getApplicationContext());
				Date d=new Date();
				long start= d.getTime();
				Calendar c = Calendar.getInstance(); 
				c.setTime(d); 
				c.add(Calendar.YEAR, 1);

				ch.getAllEvents(start, c.getTime().getTime());*/
			}
		});
	}

	public void checkForEventForDate(Date date) {
		// TODO Auto-generated method stub
		ArrayList<Integer> startDateIndexInList=new ArrayList<Integer>();
		for (int i = 0; i < startDates.size(); i++) {
			String clickDate=calenderHelper.getDate(date.getTime());
			String eventStartDate=calenderHelper.getDate(Long.parseLong(startDates.get(i)));
			if(eventStartDate.equals(clickDate)){
				startDateIndexInList.add(i);
			}
		}
		if(startDateIndexInList.size()>0)
			openDialogToShowEvents(startDateIndexInList,formatter.format(date));
		else
			Toast.makeText(CalenderActivity.this, "No Events for "+formatter.format(date), 1000).show();
	}

	private void doImportantCalenderOperations() {
		// TODO Auto-generated method stub
		calenderHelper=new CalenderHelper(getApplicationContext());

		HashMap<String, ArrayList<String>> map = calenderHelper.readSTCalendarEvent();

		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			populateListsWithData((ArrayList<String>) pairs.getValue(),(String) pairs.getKey());
			it.remove(); // avoids a ConcurrentModificationException
		}
		for (int j = 0; j < startDates.size(); j++) {
			setCustomResourceForDates(Long.parseLong(startDates.get(j)));
		}
	}

	protected void openDialogToShowEvents(ArrayList<Integer> startDateIndexInList, String date) {
		// TODO Auto-generated method stub
		eventsDialog=new EventsShowFragment(startDateIndexInList,date);
		eventsDialog.setCancelable(true);
		eventsDialog.show(getFragmentManager(), "");
	}

	private class EventsShowFragment extends android.app.DialogFragment{
		String dateClicked;
		ArrayList<Integer> startDateIndexInList;
		public EventsShowFragment(ArrayList<Integer> startDateIndexInList, String date) {
			// TODO Auto-generated constructor stub
			this.startDateIndexInList=startDateIndexInList;
			dateClicked=date;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_events_dialog, container,
					false);
			getDialog().setTitle("Activities on "+dateClicked);

			ListView eventsList=(ListView)rootView.findViewById(R.id.eventsList);
			eventsList.setAdapter(new EventsAdapter(startDateIndexInList));

			return rootView;
		}
	}

	private class EventsAdapter extends BaseAdapter{

		private ArrayList<Integer> startDateIndexInList;
		private LayoutInflater inflater;
		public EventsAdapter(ArrayList<Integer> startDateIndexInList) {
			// TODO Auto-generated constructor stub
			this.startDateIndexInList=startDateIndexInList;
			inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return startDateIndexInList.size();
		}

		@Override
		public Object getItem(int paramInt) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int paramInt) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View vi=convertView;
			if(convertView==null)
				vi = inflater.inflate(R.layout.event_list_item, null);

			final int commonIndex=startDateIndexInList.get(position);

			TextView timeCreated=(TextView)vi.findViewById(R.id.profileUserName);
			String time=descriptions.get(commonIndex).replaceAll(" ","").split("UTC:")[1];
			timeCreated.setText(AppUtil.getDate(Long.parseLong(time))+"\n"+AppUtil.getTimeDifference(Long.parseLong(time)));

			TextView eventSubject=(TextView)vi.findViewById(R.id.profilePostTitle);
			eventSubject.setText(nameOfEvent.get(commonIndex));

			TextView eventDesc=(TextView)vi.findViewById(R.id.profilePostDescrip);
			eventDesc.setText(descriptions.get(commonIndex).split("UTC:")[0]);

			ImageView deleteEvent=(ImageView)vi.findViewById(R.id.deleteEvent);
			deleteEvent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View paramView) {
					// TODO Auto-generated method stub
					ConfirmationDialog confDlg= new ConfirmationDialog("Delete Confirmation", "Are you sure,you want to delete this event?", "Yes", "No",eventIdList.get(commonIndex));
					confDlg.setCancelable(true);
					confDlg.show(getSupportFragmentManager(), "");
				}
			});
			return vi;
		}

	}
	private void populateListsWithData(ArrayList<String> value, String key) {
		// TODO Auto-generated method stub
		Log.e("events details", value.toString()+" "+key);
		if(key.equals("startDates")){
			if(null==startDates)
				startDates = new ArrayList<String>();
			else
				startDates.clear();
			startDates=value;
		}else if (key.equals("descriptions")) {
			if(null==descriptions)
				descriptions = new ArrayList<String>();
			else
				descriptions.clear();
			descriptions = new ArrayList<String>();
			descriptions=value;
		}else if (key.equals("endDates")) {
			if(null==endDates)
				endDates = new ArrayList<String>();
			else
				endDates.clear();
			endDates = new ArrayList<String>();
			endDates=value;
		}else if (key.equals("nameOfEvent")) {
			if(null==nameOfEvent)
				nameOfEvent = new ArrayList<String>();
			else
				nameOfEvent.clear();
			nameOfEvent = new ArrayList<String>();
			nameOfEvent=value;
		}else if (key.equals("eventIds")) {
			if(null==eventIdList)
				eventIdList = new ArrayList<String>();
			else
				eventIdList.clear();
			eventIdList = new ArrayList<String>();
			eventIdList=value;
		}
	}

	/**
	 * Save current states of the Caldroid here
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

		if (caldroidFragment != null) {
			caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
		}

		if (dialogCaldroidFragment != null) {
			dialogCaldroidFragment.saveStatesToKey(outState,
					"DIALOG_CALDROID_SAVED_STATE");
		}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}
	public class ConfirmationDialog extends DialogFragment {


		private CharSequence title;
		private CharSequence message;
		private CharSequence positivetxt;
		private CharSequence negativeTxt;
		private CharSequence  eventID;

		public ConfirmationDialog( CharSequence title,
				CharSequence message,
				CharSequence positivetxt,
				CharSequence negativeTxt, String eventID) {
			// TODO Auto-generated constructor stub
			this.title=title;
			this.negativeTxt=negativeTxt;
			this.positivetxt=positivetxt;
			this.negativeTxt=negativeTxt;
			this.eventID=eventID;
			this.message=message;
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
			.setTitle(title)
			// Set Dialog Message
			.setMessage(message)

			// Positive button
			.setPositiveButton(positivetxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Do something else
					deleteEvent(eventID);
					dismiss();
				}
			})

			// Negative Button
			.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,    int which) {
					// Do something else
					dismiss();
				}
			}).create();
		}
	}

	protected void deleteEvent(CharSequence eventID2) {
		// TODO Auto-generated method stub
		calenderHelper.deleteEvent(Long.parseLong((String) eventID2));
		eventsDialog.dismiss();
		doImportantCalenderOperations();
		caldroidFragment.refreshView();
		checkForEventForDate(new Date());
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_calender_actionbar, menu);	
		return super.onCreateOptionsMenu(menu);
	}
}
