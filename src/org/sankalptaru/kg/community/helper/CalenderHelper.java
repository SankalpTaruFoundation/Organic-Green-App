package org.sankalptaru.kg.community.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.sankalptaru.kg.community.AppUtil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.provider.CalendarContract.Reminders;
//import android.util.Log;
import android.widget.Toast;

public class CalenderHelper {

	private Context context;

	private String google_account_name;

	private long calID;

	public long getCalID() {
		return calID;
	}
	public void setCalID(long calID) {
		this.calID = calID;
	}
	public CalenderHelper(Context context){
		this.context=context;
		SharedPreferences myPrefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		google_account_name= myPrefs.getString("Google_Email", "none");
	}

	public static final String[] EVENT_PROJECTION = new String[] {
		Calendars._ID, // 0
		Calendars.ACCOUNT_NAME, // 1
		Calendars.CALENDAR_DISPLAY_NAME // 2
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;


	public String createCalender(){
		ContentValues values = new ContentValues();
		Calendar calendar = new GregorianCalendar();
		String timeZone = calendar.getTimeZone().getID();
		values.put(
				Calendars.ACCOUNT_NAME,
				"Sankalptaru");
		values.put(
				Calendars.ACCOUNT_TYPE,
				CalendarContract.ACCOUNT_TYPE_LOCAL);
		values.put(
				Calendars.NAME,
				"SankalpTaru Organic Greens Calendar");
		values.put(
				Calendars.CALENDAR_DISPLAY_NAME,
				"SankalpTaru Organic Greens Calendar");
		values.put(
				Calendars.CALENDAR_COLOR,
				0xff99FF00);
		values.put(
				Calendars.CALENDAR_ACCESS_LEVEL,
				Calendars.CAL_ACCESS_OWNER);
		values.put(
				Calendars.OWNER_ACCOUNT,
				getGoogle_account_name());
		values.put(
				Calendars.CALENDAR_TIME_ZONE,
				timeZone);
		values.put(
				Calendars.SYNC_EVENTS,
				1);
		Uri.Builder builder =
				CalendarContract.Calendars.CONTENT_URI.buildUpon();
		builder.appendQueryParameter(
				Calendars.ACCOUNT_NAME,
				"SankalpTaru");
		builder.appendQueryParameter(
				Calendars.ACCOUNT_TYPE,
				CalendarContract.ACCOUNT_TYPE_LOCAL);
		builder.appendQueryParameter(
				CalendarContract.CALLER_IS_SYNCADAPTER,
				"true");
		Uri uri =
				context.getContentResolver().insert(builder.build(), values);
		return uri.getLastPathSegment();
	}

	public void createEvent(String titString,String desString,int year, int month, int day, int hour, int mint, int sec,int milisec,long start,long end, String freqString){
		SharedPreferences myPrefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		long calId = myPrefs.getLong("calId", -1);
		if (calId == -1) {
			// no calendar account; react meaningfully
			return;
		}
		Calendar cal = new GregorianCalendar(year, month, day);
		cal.setTimeZone(cal.getTimeZone());
		cal.set(Calendar.HOUR, hour);
		cal.set(Calendar.MINUTE, mint);
		cal.set(Calendar.SECOND, sec);
		cal.set(Calendar.MILLISECOND, milisec);

		ContentValues values = new ContentValues();
		values.put(Events.DTSTART,start);

		values.put(Events.DTEND, end);
		if(null!=freqString)
			//			values.put(Events.RRULE,"FREQ=DAILY;COUNT=1;BYDAY=MO,TU,WE,TH,FR;WKST=MO");
			values.put(Events.TITLE, titString);
		values.put(Events.EVENT_LOCATION, cal.getTimeZone().getID());
		values.put(Events.CALENDAR_ID, calId);
		values.put(Events.EVENT_TIMEZONE, "UTC/GMT +5:30 hours");
		values.put(Events.DESCRIPTION,
				desString+"\nUTC: "+start/1000);
		// reasonable defaults exist:
		values.put(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
		values.put(Events.SELF_ATTENDEE_STATUS,
				Events.STATUS_CONFIRMED);
		values.put(Events.ALL_DAY, 1);
		values.put(Events.ORGANIZER, getGoogle_account_name());
		values.put(Events.GUESTS_CAN_INVITE_OTHERS, 1);
		values.put(Events.GUESTS_CAN_MODIFY, 1);
		values.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
		Uri uri =
				context.getContentResolver().
				insert(Events.CONTENT_URI, values);

		long eventId = new Long(uri.getLastPathSegment());
		if(null!=freqString)
			//			addReminder(eventId);
			Toast.makeText(context, "Event created.", 1000).show();
	}

	public long getCalendarId() {
		String[] projection = new String[]{Calendars._ID};
		String selection =
				Calendars.ACCOUNT_NAME +
				" = ? " +
				Calendars.ACCOUNT_TYPE +
				" = ? ";
		// use the same values as above:
		String[] selArgs =
				new String[]{
				"Sankalptaru",
				CalendarContract.ACCOUNT_TYPE_LOCAL};
		Cursor cursor =
				context.getContentResolver().
				query(
						Calendars.CONTENT_URI,
						projection,
						selection,
						selArgs,
						null);
		if (cursor.moveToFirst()) {
			return cursor.getLong(0);
		}
		return -1;
	}

	public void queryCalendar() {

		String[] projection =
				new String[]{
				Calendars._ID,
				Calendars.NAME,
				Calendars.ACCOUNT_NAME,
				Calendars.ACCOUNT_TYPE};
		Cursor calCursor =
				context.getContentResolver().
				query(Calendars.CONTENT_URI,
						projection,
						Calendars.VISIBLE + " = 1",
						null,
						Calendars._ID + " ASC");
		if (calCursor.moveToFirst()) {
			do {
				long id = calCursor.getLong(0);
				String displayName = calCursor.getString(1);
//				Log.e("Calender Details", "calID:"+id+" Displayname: "+displayName);
			} while (calCursor.moveToNext());
		}
	}

	public HashMap<String, ArrayList<String>> readSTCalendarEvent() {
		ArrayList<String> nameOfEvent = new ArrayList<String>();
		ArrayList<String> startDates = new ArrayList<String>();
		ArrayList<String> endDates = new ArrayList<String>();
		ArrayList<String> descriptions = new ArrayList<String>();
		ArrayList<String> eventIDList=new ArrayList<String>();
		SharedPreferences myPrefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		long calId = myPrefs.getLong("calId", -1);
		Cursor cursor = context.getContentResolver()
				.query(
						Uri.parse("content://com.android.calendar/events"),
						new String[] { "calendar_id", "title", "description",
							"dtstart", "dtend","_id", "eventLocation"}, null,
							null, null);
		cursor.moveToFirst();
		// fetching calendars name
		String CNames[] = new String[cursor.getCount()];
		nameOfEvent.clear();
		startDates.clear();
		endDates.clear();
		descriptions.clear();
		eventIDList.clear();
		// fetching calendars id
		for (int i = 0; i < CNames.length; i++) {
			if(cursor.getString(0).equals(""+calId)){
				nameOfEvent.add(cursor.getString(1));
				startDates.add(""+Long.parseLong(cursor.getString(3)));
				endDates.add(""+Long.parseLong(cursor.getString(4)));
				descriptions.add(cursor.getString(2));
				eventIDList.add(cursor.getString(5));				
			}
			CNames[i] = cursor.getString(1);
			cursor.moveToNext();
		}

		HashMap<String, ArrayList<String>> st_calender_EventDetails=new HashMap<String, ArrayList<String>>();
		st_calender_EventDetails.put("nameOfEvent", nameOfEvent);
		st_calender_EventDetails.put("startDates", startDates);
		st_calender_EventDetails.put("endDates", endDates);
		st_calender_EventDetails.put("descriptions", descriptions);
		st_calender_EventDetails.put("eventIds", eventIDList);

		return st_calender_EventDetails;
	}

	public String getDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd/MM/yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	public void getAllEvents(long beginTime,long endTime){
		long begin = beginTime;
		long end =endTime;
		String [] proj =new String[]{
				Instances._ID,
				Instances.BEGIN,
				Instances.END,
				Instances.EVENT_ID,
				Instances.CALENDAR_ID,
				Instances.RDATE};
		Cursor cursor =
				Instances.query(context.getContentResolver(), proj, begin, end);
		if (cursor.getCount() > 0) {
			// deal with conflict
		}
		if (cursor.moveToFirst()) {
			do {
//				Log.e("event details with in range", cursor.getString(5)+" "+AppUtil.getDate(Long.parseLong(cursor.getString(1))/1000));
			}while (cursor.moveToNext());
		}
	}

	public void getSpecificEventData(long eventId){
		long selectedEventId = eventId;
		String[] proj =
				new String[]{
				Events._ID,
				Events.DTSTART,
				Events.DTEND,
				Events.RRULE,
				Events.TITLE};
		Cursor cursor =
				context.getContentResolver().
				query(
						Events.CONTENT_URI,
						proj,
						Events._ID + " = ? ",
						new String[]{Long.toString(selectedEventId)},
						null);
		if (cursor.moveToFirst()) {
			// read event data
//			Log.e("event data", cursor.getString(1)+" "+cursor.getString(4));
		}
	}

	public void deleteEvent(long eventId){
		String[] selArgs =
				new String[]{Long.toString(eventId)};
		int deleted =
				context.getContentResolver().
				delete(
						Events.CONTENT_URI,
						Events._ID + " =? ",
						selArgs);
		Toast.makeText(context, "Event Deleted.", 1000).show();
	}

	public void deleteCalender(long calId){
		String[] selArgs =
				new String[]{Long.toString(calId)};
		int deleted =
				context.getContentResolver().
				delete(
						Calendars.CONTENT_URI,
						Calendars._ID + " =? ",
						selArgs);
	}

	public void addReminder(long eventId){
//		Log.e("eventID", ""+eventId);
		ContentValues values = new ContentValues();
		values.put(Reminders.EVENT_ID, eventId);
		values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		values.put(Reminders.MINUTES, 5);
		Uri uri =context.getContentResolver().insert(Reminders.CONTENT_URI, values);
		Toast.makeText(context, "Reminder Added."+uri.getLastPathSegment(), 1000).show();
	}

	public void addAttendee(long eventId,String attendeeName, String attendeeEmail){
		ContentValues values = new ContentValues();
		values.put(Attendees.EVENT_ID, eventId);
		values.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
		values.put(Attendees.ATTENDEE_NAME, attendeeName);
		values.put(Attendees.ATTENDEE_EMAIL, attendeeEmail);
		context.getContentResolver().insert(Attendees.CONTENT_URI, values);
		Toast.makeText(context, "Attendee Added.", 1000).show();
	}

	public String getGoogle_account_name() {
		return google_account_name;
	}

	public void setGoogle_account_name(String google_account_name) {
		this.google_account_name = google_account_name;
	}

	public void pushAppointmentsToCalender( String title, String addInfo, int status, long startDate,long enddate, boolean needReminder, boolean needMailService, String freqString) {
		/***************** Event: note(without alert) *******************/
		SharedPreferences myPrefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
		long calId = myPrefs.getLong("calId", -1);
		if (calId == -1) {
			// no calendar account; react meaningfully
			return;
		}
		String eventUriString = "content://com.android.calendar/events";
		ContentValues eventValues = new ContentValues();

		eventValues.put("calendar_id", calId); // id, We need to choose from
		// our mobile for primary
		// its 1
		eventValues.put("title", title);
		Date d =new Date();
		eventValues.put("description", addInfo+"\nUTC: "+(d.getTime())/1000);
//		eventValues.put("eventLocation", place);

		long endDate = startDate + 1000 * 60 * 60; // For next 1hr

		eventValues.put("dtstart", startDate);
		if(null!=freqString)
			eventValues.put(Events.RRULE,"FREQ="+freqString);
		eventValues.put("dtend", enddate);

		eventValues.put(Events.EVENT_TIMEZONE, "UTC/GMT +5:30 hours");

		// values.put("allDay", 1); 
		//If it is bithday alarm or such
		// kind (which should remind me for whole day) 0 for false, 1
		// for true
		eventValues.put("eventStatus", status); // This information is
		// sufficient for most
		// entries tentative (0),
		// confirmed (1) or canceled
		// (2):

		/*Comment below visibility and transparency  column to avoid java.lang.IllegalArgumentException column visibility is invalid error */

		/*eventValues.put("visibility", 3); // visibility to default (0),
	                                        // confidential (1), private
	                                        // (2), or public (3):
	    eventValues.put("transparency", 0); // You can control whether
	                                        // an event consumes time
	                                        // opaque (0) or transparent
	                                        // (1).
		 */

		eventValues.put("hasAlarm", 1); // 0 for false, 1 for true

		Uri eventUri = context.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
		long eventID = Long.parseLong(eventUri.getLastPathSegment());

		if (needReminder) {
			/***************** Event: Reminder(with alert) Adding reminder to event *******************/
			String reminderUriString = "content://com.android.calendar/reminders";

			ContentValues reminderValues = new ContentValues();

			reminderValues.put("event_id", eventID);
			reminderValues.put("minutes", 5); // Default value of the
			// system. Minutes is a
			// integer
			reminderValues.put("method", 1); // Alert Methods: Default(0),
			// Alert(1), Email(2),
			// SMS(3)
			Uri reminderUri = context.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
		}
		/***************** Event: Meeting(without alert) Adding Attendies to the meeting *******************/
		if (needMailService) {
			String attendeuesesUriString = "content://com.android.calendar/attendees";
			/********
			 * To add multiple attendees need to insert ContentValues multiple
			 * times
			 ***********/
			ContentValues attendeesValues = new ContentValues();
			attendeesValues.put("event_id", eventID);
			//			attendeesValues.put("attendeeName", "Gaurav Chawla"); // Attendees name
			//			attendeesValues.put("attendeeEmail", "gaurav@sankalptaru.org");// Attendee
			// E
			// mail
			// id
			//			attendeesValues.put("attendeeRelationship", 0); // Relationship_Attendee(1),
			// Relationship_None(0),
			// Organizer(2),
			// Performer(3),
			// Speaker(4)
			attendeesValues.put("attendeeType", 0); // None(0), Optional(1),
			// Required(2), Resource(3)
			attendeesValues.put("attendeeStatus", 0); // NOne(0), Accepted(1),
			// Decline(2),
			// Invited(3),
			// Tentative(4)
			Uri attendeuesesUri = context.getApplicationContext().getContentResolver().insert(Uri.parse(attendeuesesUriString), attendeesValues);
		}
		Toast.makeText(context, "Reminder added successfully.", 1000).show();
	}
}
