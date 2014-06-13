/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

/**
 * Constants for the TestNG groups facility.
 */
public final class TestGroup {

  /**
   * TestNG 'unit; group.
   * Unit tests do not use external dependencies, except the file system.
   */
  public static final String UNIT = "unit";
  /**
   * TestNG 'unitslow' group.
   * Unit tests that are quite slow, typically longer than 5 seconds.
   */
  public static final String UNIT_SLOW = "unitslow";
  /**
   * TestNG 'unitdb' group.
   * Database tests written in the style of unit tests.
   * These setup/teardown the database before/after the tests and use a private database.
   */
  public static final String UNIT_DB = "unitdb";
  /**
   * TestNG 'integration' group.
   * Integration tests are complex tests linking different parts of the system.
   */
  public static final String INTEGRATION = "integration";
  /**
   * TestNG 'regression' group.
   * Regression tests compare the output for a whole view against a dataset that is known to be correct.
   */
  public static final String REGRESSION = "regression";

  /**
   * Restricted constructor.
   */
  private TestGroup() {
    throw new UnsupportedOperationException("No instances");
  }

}
