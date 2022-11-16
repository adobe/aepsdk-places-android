package com.adobe.placestestapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Places;
import com.adobe.marketing.mobile.places.PlacesAuthorizationStatus;
import com.adobe.marketing.mobile.places.PlacesPOI;
import com.adobe.marketing.mobile.places.PlacesRequestError;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

	private final String LOG_TAG = "Main Activity";
	private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

	private FusedLocationProviderClient mFusedLocationClient;
	private GeofencingClient mGeofencingClient;
	private PendingIntent mGeofencePendingIntent;
	private SettingsClient mSettingsClient;
	private LocationRequest mLocationRequest;
	private LocationSettingsRequest mLocationSettingsRequest;
	private LocationCallback mLocationCallback;

	// map related variables
	private final float DEFAULT_ZOOM = 16f;
	private GoogleMap googleMap;
	private Location currentLocation;
	private Marker currentLocationMarker;
	private Boolean isRequestingLocationUpdates = false;
	private List<PlacesPOI> nearByPOIs;


	private List<Circle> fenceCircles;
	private List<Marker> poiMarkers;
	private TextView statusView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusView = (TextView)findViewById(R.id.statusView);

		mGeofencingClient = LocationServices.getGeofencingClient(this);
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mSettingsClient = LocationServices.getSettingsClient(this);
		statusView.setText("Places version " + Places.extensionVersion());

		fenceCircles = new ArrayList<>();

		createLocationCallback();
		createLocationRequest();
		buildLocationSettingsRequest();
		initMap();

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkPermissions()) {
			startLocationUpdates();
		} else {
			requestPermissions();
		}
	}

	/**
	 * Dispatch onPause() to fragments.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	void initMap() {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}


	// =======================================================================
	// button click listeners
	// =======================================================================

	public void getLocation(View view) {
		if (currentLocationMarker != null) {
			// Showing the current location in Google Map
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocationMarker.getPosition()));

			// Zoom in the Google Map
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
	}

	public void getNearByPlaces(View view) {
		if (this.currentLocation == null) {
			Log.d(LOG_TAG, "Don't know the current location cannot get the near by places");
			return;
		}

		Places.getNearbyPointsOfInterest(currentLocation, 10, new AdobeCallback<List<PlacesPOI>>() {
			@Override
			public void call(List<PlacesPOI> pois) {
				Log.d(LOG_TAG, "Callback Called");
				plotPlacesPOI(pois);
				nearByPOIs = new ArrayList<PlacesPOI>(pois);
			}
		}, new AdobeCallback<PlacesRequestError>() {
			@Override
			public void call(PlacesRequestError placesRequestError) {
				Log.d(LOG_TAG, placesRequestError.toString());
			}
		});

	}

	public void monitorRegions(View view) {
		startMonitoringFences();
	}

	public void clearAll(View view) {
		Places.clear();
		stopMonitoringFences();
		googleMap.clear();
	}

	public void getCurrentPOI(View view) {
		Places.getCurrentPointsOfInterest(new AdobeCallback<List<PlacesPOI>>() {
			@Override
			public void call(List<PlacesPOI> placesPOIS) {
				if (placesPOIS != null && placesPOIS.size() > 0) {
					statusView.setText("User is Inside \n");
					for (final PlacesPOI eachPOI : placesPOIS) {
						statusView.append( eachPOI.getName() + " \n");
					}
				} else {
					statusView.setText("User is not inside any POI");
				}
			}
		});
	}

	public void getLastKnownLocation(View view) {
		Places.getLastKnownLocation(new AdobeCallback<Location>() {
			@Override
			public void call(Location location) {
				if (location != null) {
					statusView.setText("Latitude : " + location.getLatitude() + "  Longitude : " + location.getLongitude());
				} else {
					statusView.setText("Location is null");
				}
			}
		});
	}

	public void setAuthorizationStatus(View view) {
		Places.setAuthorizationStatus(PlacesAuthorizationStatus.WHEN_IN_USE);
	}


	// =======================================================================
	// delegated callback methods
	// =======================================================================



	@Override
	public void onMapReady(final GoogleMap googleMap) {
		this.googleMap = googleMap;
		LatLng latLng = new LatLng(37.3307703, -121.8962838);
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
		googleMap.moveCamera(cameraUpdate);


		// Setting a click event handler for the map
		this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng latLng) {
				Log.d(LOG_TAG, "Long Clicked on Map adding marker");
			}
		});
	}


	// =======================================================================
	// private helper methods
	// =======================================================================

	private void plotPlacesPOI(final List<PlacesPOI> pois) {
		if (pois == null || pois.isEmpty()) {
			return;
		}

		runOnUiThread(new Runnable() {
			List<Marker> markers = new ArrayList<>();
			public void run() {
				for (PlacesPOI poi : pois) {
					LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
					MarkerOptions markerOptions = new MarkerOptions();
					CircleOptions circleOptions = new CircleOptions();

					markerOptions.position(latLng);
					markerOptions.title(poi.getName());
					circleOptions.center(latLng);
					circleOptions.radius(poi.getRadius());
					markers.add(googleMap.addMarker(markerOptions));
					fenceCircles.add(googleMap.addCircle(circleOptions));
				}

				LatLngBounds.Builder builder = new LatLngBounds.Builder();

				for (Marker marker : markers) {
					builder.include(marker.getPosition());
				}

				LatLngBounds bounds = builder.build();
				int padding = 0; // offset from edges of the map in pixels
				CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
				googleMap.animateCamera(cu);
			}
		});
	}

	private void startMonitoringFences() {
		if (nearByPOIs == null || nearByPOIs.isEmpty()) {
			return;
		}

		for (Circle circle : fenceCircles) {
			circle.setFillColor(getColorWithAlpha(Color.RED, 0.15f));
		}

		List<Geofence> geofences = new ArrayList<>();

		for (PlacesPOI poi : nearByPOIs) {
			final Geofence fence = new Geofence.Builder()
			.setRequestId(poi.getIdentifier())
			.setCircularRegion(poi.getLatitude(), poi.getLongitude(), poi.getRadius())
			.setExpirationDuration(Geofence.NEVER_EXPIRE)
			.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
								Geofence.GEOFENCE_TRANSITION_EXIT)
			.build();
			geofences.add(fence);
		}

		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		builder.addGeofences(geofences);
		builder.build();

		try {
			if (checkPermissions()) {
				mGeofencingClient.addGeofences(builder.build(), getGeofencePendingIntent())
				.addOnSuccessListener(this, new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d(LOG_TAG, "Successfully added fences for monitoring");
					}
				})
				.addOnFailureListener(this, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(LOG_TAG, "Error in adding fences for monitoring " + e.getMessage());

					}
				});
			}
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
		}
	}

	private void stopMonitoringFences() {
		mGeofencingClient.removeGeofences(getGeofencePendingIntent())
		.addOnSuccessListener(this, new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Log.d(LOG_TAG, "Successfully removed geofences");
			}
		})
		.addOnFailureListener(this, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Log.d(LOG_TAG, "Failed to remove geofences");
			}
		});
	}

	private PendingIntent getGeofencePendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mGeofencePendingIntent != null) {
			return mGeofencePendingIntent;
		}

		Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
		// calling addGeofences() and removeGeofences().
		mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
								 FLAG_UPDATE_CURRENT);
		return mGeofencePendingIntent;
	}


	private void updateMyLocationMarker(final LatLng latLng) {
		if (currentLocationMarker != null) {
			currentLocationMarker.remove();
		}

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.icon(BitmapDescriptorFactory
						   .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
		currentLocationMarker = googleMap.addMarker(markerOptions);
	}


	/**
	 * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
	 * runtime permission has been granted.
	 */
	private void startLocationUpdates() {
		// Begin by checking if the device has the necessary location settings.
		mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
		.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				Log.i(LOG_TAG, "All location settings are satisfied.");

				//noinspection MissingPermission
				mFusedLocationClient.requestLocationUpdates(mLocationRequest,
						mLocationCallback, Looper.myLooper());

			}
		})
		.addOnFailureListener(this, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				int statusCode = ((ApiException) e).getStatusCode();

				switch (statusCode) {
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						Log.i(LOG_TAG, "Location settings are not satisfied. Attempting to upgrade " +
							  "location settings ");

						try {
							// Show the dialog by calling startResolutionForResult(), and check the
							// result in onActivityResult().
							ResolvableApiException rae = (ResolvableApiException) e;
							rae.startResolutionForResult(MainActivity.this, 0x1);
						} catch (IntentSender.SendIntentException sie) {
							Log.i(LOG_TAG, "PendingIntent unable to execute request.");
						}

						break;

					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						String errorMessage = "Location settings are inadequate, and cannot be " +
											  "fixed here. Fix in Settings.";
						Log.e(LOG_TAG, errorMessage);
						Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
						isRequestingLocationUpdates = false;
				}

				//updateUI();
			}
		});
	}


	/**
	 * Creates a callback for receiving location events.
	 */
	private void createLocationCallback() {
		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				super.onLocationResult(locationResult);

				currentLocation = locationResult.getLastLocation();
				updateMyLocationMarker(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
			}
		};
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(3000);
		mLocationRequest.setFastestInterval(1500);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void buildLocationSettingsRequest() {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
		builder.addLocationRequest(mLocationRequest);
		mLocationSettingsRequest = builder.build();
	}


	/**
	 * Removes location updates from the FusedLocationApi.
	 */
	private void stopLocationUpdates() {
		if (!isRequestingLocationUpdates) {
			Log.d(LOG_TAG, "stopLocationUpdates: updates never requested, no-op.");
			return;
		}

		mFusedLocationClient.removeLocationUpdates(mLocationCallback)
		.addOnCompleteListener(this, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				isRequestingLocationUpdates = false;
				//setButtonsEnabledState();
			}
		});
	}


	/**
	 * Return the current state of the permissions needed.
	 */
	private boolean checkPermissions() {
		int permissionState = ActivityCompat.checkSelfPermission(this,
							  FINE_LOCATION);
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}

	private void requestPermissions() {
		boolean shouldProvideRationale =
			ActivityCompat.shouldShowRequestPermissionRationale(this,
					FINE_LOCATION);

		// Provide an additional rationale to the user. This would happen if the user denied the
		// request previously, but didn't check the "Don't ask again" checkbox.
		if (shouldProvideRationale) {
			Log.i(LOG_TAG, "Permission not granted to provide location");
		} else {
			Log.i(LOG_TAG, "Requesting permission");
			// Request permission. It's possible this can be auto answered if device policy
			// sets the permission in a given state or the user denied the permission
			// previously and checked "Never ask again".
			ActivityCompat.requestPermissions(MainActivity.this,
											  new String[] {FINE_LOCATION},
											  REQUEST_PERMISSIONS_REQUEST_CODE);
		}
	}

	/**
	 * Callback received when a permissions request has been completed.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.i(LOG_TAG, "onRequestPermissionResult");

		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.length <= 0) {
				// If user interaction was interrupted, the permission request is cancelled and you
				// receive empty arrays.
				Log.i(LOG_TAG, "User interaction was cancelled.");
			} else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


				boolean backgroundLocationPermissionApproved =
						ActivityCompat.checkSelfPermission(this,
								Manifest.permission.ACCESS_BACKGROUND_LOCATION)
								== PackageManager.PERMISSION_GRANTED;

				if (backgroundLocationPermissionApproved) {
					Places.setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS);
				} else {
					Places.setAuthorizationStatus(PlacesAuthorizationStatus.WHEN_IN_USE);
				}

				if (isRequestingLocationUpdates) {
					Log.i(LOG_TAG, "Permission granted, updates requested, starting location updates");
					startLocationUpdates();
				}
			} else {
				// Permission denied.
				Places.setAuthorizationStatus(PlacesAuthorizationStatus.DENIED);
				Log.i(LOG_TAG, "Permission denied");
			}
		}
	}

	public static int getColorWithAlpha(int color, float ratio) {
		int newColor = 0;
		int alpha = Math.round(Color.alpha(color) * ratio);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		newColor = Color.argb(alpha, r, g, b);
		return newColor;
	}


}
