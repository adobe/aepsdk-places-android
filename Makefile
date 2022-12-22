checkstyle:
		(./code/gradlew -p code/android-places-library checkstyle)
		
check-format:
		(./code/gradlew -p code/android-places-library ktlintCheck)
		
format:
		(./code/gradlew -p code/android-places-library ktlintFormat)

clean:
	  (./code/gradlew -p code clean)
		
format-license:
		(./code/gradlew -p code licenseFormat)

unit-test:
		(./code/gradlew -p code/android-places-library testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/android-places-library createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/android-places-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-places-library connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/android-places-library createPhoneDebugAndroidTestCoverageReport)

javadoc:
	(mkdir -p ci/javadoc)
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) javadocPublic)
	(cp -r ./code/$(EXTENSION-LIBRARY-FOLDER-NAME)/build ./ci/javadoc)

publish:
		(./code/gradlew -p code/android-places-library publishReleasePublicationToSonatypeRepository)

assemble-phone:
		(./code/gradlew -p code/android-places-library assemblePhone)
		
assemble-phone-release:
		(./code/gradlew -p code/android-places-library assemblePhoneRelease)

assemble-app:
		(./code/gradlew -p code/testapp  assemble)

ci-publish-staging: clean assemble-phone-release
		(./code/gradlew -p code/android-places-library publishReleasePublicationToSonatypeRepository --stacktrace)

ci-publish-main: clean assemble-phone-release
		(./code/gradlew -p code/android-places-library publishReleasePublicationToSonatypeRepository -Prelease)