package com.epfl.android.aac_speech.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

/**
 * A test suite containing all tests for my application.
 */
public class TestRunner extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(TestRunner.class)
				.includeAllPackagesUnderHere().build();
	}
}
