package com.adobe.placestestapp;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.adobe.marketing.mobile.Places;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

	private static final String CHANNEL_ID = "channel_01";

	public GeofenceTransitionsIntentService() {
		super("GeofenceTransitionsIntentService");
	}

	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		// Call Adobe API to process information
		Places.processGeofenceEvent(geofencingEvent);


		// Send notification for event.
		if (geofencingEvent.hasError()) {
			Log.e("Geofence Intent", "Error in the geofennce event");
			return;
		}

		// Get the transition type.
		int geofenceTransition = geofencingEvent.getGeofenceTransition();

		// Test that the reported transition was of interest.
		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
				geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

			// Get the geofences that were triggered. A single event can trigger
			// multiple geofences.
			List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

			// Get the transition details as a String.
			String geofenceTransitionDetails = getGeofenceTransitionDetails(
												   geofenceTransition,
												   triggeringGeofences
											   );

			String message = new String();

			switch (geofenceTransition) {
				case Geofence.GEOFENCE_TRANSITION_ENTER:
					message = "Welcome to Starbucks, Magic happens over a coffee";
					break;

				case Geofence.GEOFENCE_TRANSITION_EXIT:
					message =  "I know you will comeback for more coffee";
					break;

				default:
					message =  "Welcome Again";
			}

			// Send notification and log the transition details.
			sendNotification(geofenceTransitionDetails, message);
		} else {
			// Not known event
		}

	}


	private String getGeofenceTransitionDetails(
		int geofenceTransition,
		List<Geofence> triggeringGeofences) {

		String geofenceTransitionString = getTransitionString(geofenceTransition);

		// Get the Ids of each geofence that was triggered.
		ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();

		for (Geofence geofence : triggeringGeofences) {
			triggeringGeofencesIdsList.add(geofence.getRequestId());
		}

		String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

		return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
	}


	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 *
	 * @param transitionType    A transition type constant defined in Geofence
	 * @return                  A String indicating the type of transition
	 */
	private String getTransitionString(int transitionType) {
		switch (transitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return "Geofence entered";

			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return "Geofence exited";

			default:
				return "";
		}
	}


	/**
	 * Posts a notification in the notification bar when a transition is detected.
	 * If the user clicks the notification, control goes to the MainActivity.
	 */
	private void sendNotification(final String notificationTitle, final String notificationMessage) {
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
			(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Android O requires a Notification Channel.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.app_name);
			// Create the channel for the notification
			NotificationChannel mChannel =
				new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

			// Set the Notification Channel for the Notification Manager.
			mNotificationManager.createNotificationChannel(mChannel);
		}

		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		// Define the notification settings.
		builder.setSmallIcon(R.drawable.ic_launcher_foreground)
		// In a real app, you may want to use a library like Volley
		// to decode the Bitmap.
		.setLargeIcon(BitmapFactory.decodeResource(getResources(),
					  R.drawable.ic_launcher_foreground))
		.setColor(Color.RED)
		.setContentTitle(notificationTitle)
		.setContentText(notificationMessage);

		// Set the Channel ID for Android O.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(CHANNEL_ID); // Channel ID
		}

		// Dismiss notification once the user touches it.
		builder.setAutoCancel(true);

		// Issue the notification
		mNotificationManager.notify(0, builder.build());
	}

}




