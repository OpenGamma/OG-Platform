/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.remote;

/**
 * Utility class for tests
 */
public final class RemoteTestUtils {

  private RemoteTestUtils() { /* private constructor */ }

  /**
   * Config name
   */
  public static final String CONFIG_NAME = "Remote view";
  /**
   * Default server url
   */
  public static final String LOCALHOST = "http://localhost:8080/jax/";
  /**
   * USD/GBP Exposure function name
   */
  public static final String USD_GBP_FF_EXPOSURE = "USD-GBP-FF-1";
  /**
   * Currency matrix name
   */
  public static final String CURRENCY_MATRIX = "BBG-Matrix";
  /**
   * USD/GBP snapshot name
   */
  public static final String USD_GBP_SNAPSHOT = "USD_GBP_XCcy_Integration";
  /**
   * OIS Exposure function name
   */
  public static final String OIS_EXPOSURE = "GBP_SO_DSCONISCCY-OIS";
  /**
   * UK GOV exposure function name
   */
  public static final String UK_GOV_EXPOSURE = "GBP_SO_DSCON-OIS_ISCCY-UKGVT";

}
