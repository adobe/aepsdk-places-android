/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.places;

/**
 * Listens for {@link EventType#PLACES}, {@link EventSource#REQUEST_CONTENT} events and kicks processing for parent {@link PlacesExtension}
 * module.
 */
class PlacesListenerPlacesRequestContent extends ModuleEventListener<PlacesExtension> {

	/**
	 * Constructs a new instance of {@code PlacesListenerPlacesRequestContent}.
	 *
	 * @param module {@link PlacesExtension} module that owns this listener
	 * @param type {@link EventType} that this listener will hear
	 * @param source {@link EventSource} that this listener will hear
	 */
	PlacesListenerPlacesRequestContent(final PlacesExtension module, final EventType type, final EventSource source) {
		super(module, type, source);
	}

	@Override
	public void hear(final Event event) {
		if (event == null) {
			Log.warning(PlacesConstants.LOG_TAG, "Null event listened by PlacesListenerConfigurationResponseContent");
			return;
		}

		EventData eventData = event.getData();

		if (eventData == null || eventData.isEmpty()) {
			Log.debug(PlacesConstants.LOG_TAG, "Places request content event's eventData is empty, Ignoring event");
			return;
		}

		// do not queue the getter events, it waits for the callback to be called
		String requestType = eventData.optString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE, null);

		if (!StringUtils.isNullOrEmpty(requestType)) {
			if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES.equals(requestType)) {
				parentModule.getExecutor().execute(new Runnable() {
					@Override
					public void run() {
						parentModule.handleGetUserWithinPOIsEvent(event);
					}
				});
			}

			else if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION.equals(requestType)) {
				parentModule.getExecutor().execute(new Runnable() {
					@Override
					public void run() {
						parentModule.handleGetLastKnownLocation(event);
					}
				});
			}

			else if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_RESET.equals(requestType)) {
				parentModule.reset();
			}

			else if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES.equals(requestType)) {
				parentModule.saveLastKnownLocation(event);
				queueEvent(event);
			}

			else if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT.equals(requestType)) {
				queueEvent(event);
			}

			else if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS.equals(requestType)) {
				parentModule.handleSetAuthorizationStatusEvent(event);
			}

			else {
				Log.debug(PlacesConstants.LOG_TAG, "Unknown request type, Ignoring places request content event. For details refer to "
						  + PlacesConstants.DocsLinks.EVENT_REF);
			}
		}
	}

	private void queueEvent(final Event event) {
		parentModule.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				parentModule.queueEvent(event);
			}
		});
	}
}
