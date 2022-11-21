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

public class MockPlacesConfiguration extends PlacesConfiguration {
	private String mockedPath;
	private String mockEndPoint;
	private String mockQueryLibString;
	private Boolean mockIsValid;

	public MockPlacesConfiguration(final String queryLibString, final String endPoint, final String path,
								   final Boolean isValid) {
		super( null);
		this.mockedPath = path;
		this.mockEndPoint = endPoint;
		this.mockQueryLibString = queryLibString;
		this.mockIsValid = isValid;
	}
	
	@Override
	public String getLibrariesQueryString() {
		return mockQueryLibString;
	}

	@Override
	public String getEndpoint() {
		return mockEndPoint;
	}

	@Override
	public boolean isValid() {
		return mockIsValid;
	}
}
