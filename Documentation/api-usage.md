# Adobe Places API reference

## Prerequisites

Refer to the [Getting started guide](./getting-started.md)

## API reference

* [clear](#clear)
* [extensionVersion](#extensionversion)
* [getCurrentPointsOfInterest](#getcurrentpointsofinterest)
* [getLastKnownLocation](#getlastknownlocation)
* [getNearbyPointsOfInterest](#getnearbypointsofinterest)
* [processGeofence](#processgeofence)
* [processGeofenceEvent](#processgeofenceevent)
* [registerExtension](#registerextension)
* [setAuthorizationStatus](#setauthorizationstatus)

------

## Public classes/enums

* [PlacesPOI](#placespoi)
* [PlacesAuthorizationStatus](#placesauthorizationstatus)

------

## clear

Clears out the client-side data for the Places extension in the shared state, local storage, and in-memory.

#### Java

```java
Places.clear();
```

#### Kotlin

```kotlin
Places.clear()
```

## extensionVersion

Returns the running version of the Places extension.

#### Java

```java
String placesExtensionVersion = Places.extensionVersion();
```

#### Kotlin

```kotlin
val placesExtensionVersion: String = Places.extensionVersion()
```

## getCurrentPointsOfInterest

Requests a list of POIs in which the device is currently known to be in and returns them in a callback.

#### Java

```java
Places.getCurrentPointsOfInterest(new AdobeCallback<List<PlacesPOI>>() {
    @Override
    public void call(List<PlacesPOI> pois) {
        // use the obtained POIs that the device is within
        processUserWithinPois(pois);
    }
});
```

#### Kotlin

```kotlin
Places.getCurrentPointsOfInterest() { pois -> 
    // use the obtained POIs that the device is within
    processUserWithinPois(pois)
}
```

## getLastKnownLocation

Requests the location of the device, as previously known, by the Places extension.

> **Info**
> The Places extension only knows about locations that were provided to it via calls to `getNearbyPointsOfInterest`.

#### Java

```java
Places.getLastKnownLocation(new AdobeCallback<Location>() {
    @Override
    public void call(Location lastLocation) {
        // do something with the last known location
        processLastKnownLocation(lastLocation);
    }
});
```

#### Kotlin

```kotlin
Places.getLastKnownLocation() { lastLocation -> 
    // do something with the last known location
    processLastKnownLocation(lastLocation)
}
```

## getNearbyPointsOfInterest

Returns an ordered list of nearby POIs in a callback. An overloaded version of this method returns an error code if something went wrong with the resulting network call.

#### Syntax

```java
public static void getNearbyPointsOfInterest(@NonNull final Location location,
    final int limit,
    @NonNull final AdobeCallback<List<PlacesPOI>> successCallback,
    @NonNull final AdobeCallback<PlacesRequestError> errorCallback);
```

#### Java

```java
Places.getNearbyPointsOfInterest(currentLocation, 10,
    new AdobeCallback<List<PlacesPOI>>() {
        @Override
        public void call(List<PlacesPOI> pois) {
            // do required processing with the returned nearbyPoi array
            startMonitoringPois(pois);
        }
    }, new AdobeCallback<PlacesRequestError>() {
        @Override
        public void call(PlacesRequestError placesRequestError) {
            // look for the placesRequestError and handle the error accordingly
            handleError(placesRequestError);
        }
    }
);
```

#### Kotlin

```kotlin
Places.getNearbyPointsOfInterest(currentLocation, 10, { pois -> 
    // do required processing with the returned nearbyPoi array
    startMonitoringPois(pois);
}, { error -> 
    // look for the placesRequestError and handle the error accordingly
    handleError(placesRequestError);
})
```

## processGeofence

When a device crosses one of your app’s pre-defined Places Service region boundaries, the region and event type are passed to the SDK for processing.

Process a Geofence region event for the provided transitionType.

Pass the transitionType from `GeofencingEvent.getGeofenceTransition()`. Currently `Geofence.GEOFENCE_TRANSITION_ENTER` and `Geofence.GEOFENCE_TRANSITION_EXIT` are supported.

#### Syntax

```java
public static void processGeofence(final Geofence geofence, final int transitionType);
```

#### Java

```java
public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

        if (geofences.size() > 0) {
            // Call the Places API to process information
            Places.processGeofence(geofences.get(0), geofencingEvent.getGeofenceTransition());
        }
    }
}
```

#### Kotlin

```kotlin
```

## processGeofenceEvent

Process all Geofences in the GeofencingEvent at the same time.

#### Syntax

```java
public static void processGeofenceEvent(@NonNull final GeofencingEvent geofencingEvent);
```

#### Java

```java
public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Call the Places API to process information
        Places.processGeofenceEvent(geofencingEvent);
    }
}
```

#### Kotlin

```kotlin
```

## registerExtension

> **Warning**
> Deprecated as of 2.0.0. Use the [MobileCore.registerExtensions API](https://github.com/adobe/aepsdk-core-android) instead.

Registers the Places extension with the Mobile Core.

#### Java

```java
Places.registerExtension();
```

#### Kotlin

```kotlin
Places.registerExtension()
```

## setAuthorizationStatus

Sets the authorization status in the Places extension.

The status provided is stored in the Places shared state, and is for reference only.
Calling this method does not impact the actual location authorization status for this device.

#### Syntax

```java
public static void setAuthorizationStatus(final PlacesAuthorizationStatus status);
```

#### Java

```java
Places.setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS);
```

#### Kotlin

```kotlin
Places.setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS)
```

------

## PlacesPOI

Represents a Point of Interest retrieved from the Adobe Places Service.

```java
public class PlacesPOI {

    public String getIdentifier();
    public String getName();
    public boolean containsUser();
    public void setUserIsWithin(final boolean userIsWithin);
    public double getLatitude();
    public double getLongitude();
    public int getRadius();
    public Map<String, String> getMetadata();
    public int getWeight();
    public String getLibrary();

}
```


## PlacesAuthorizationStatus

Represents the device's authorization status for access to use location on the device.

```java 
public enum PlacesAuthorizationStatus {
    /**
     * DENIED - The app can never use your location, even when you’re using the app.
     */
    DENIED("denied"),

    /**
     * ALWAYS - The app can use your location at any time.
     */
    ALWAYS("always"),

    /**
     * UNKNOWN - The location authorization status is unknown. This is the default authorization status.
     */
    UNKNOWN("unknown"),

    /**
     * RESTRICTED - The location authorization status is restricted.
     */
    RESTRICTED("restricted"),

    /**
     * WHEN_IN_USE - The app can use your location only when you're using that app.
     */
    WHEN_IN_USE("wheninuse");
}
```