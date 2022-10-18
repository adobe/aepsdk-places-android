/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Inc.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Inc. and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Inc..
 **************************************************************************/

package com.adobe.marketing.mobile;

public class MockPlacesConfigurartion extends PlacesConfiguration {
	private String mockedPath;
	private String mockEndPoint;
	private String mockQueryLibString;
	private Boolean mockIsValid;

	public MockPlacesConfigurartion(final String queryLibString, final String endPoint, final String path,
									final Boolean isValid) {
		super(null, null);
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
