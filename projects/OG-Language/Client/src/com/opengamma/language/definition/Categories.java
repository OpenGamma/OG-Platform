/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * The standard categories for grouping OpenGamma language entities. Local implementations
 * should use these where possible (or extend generically if necessary), with a filter
 * defined that translates to corresponding names suitable for a bound language. 
 */
public final class Categories {

  // TODO: add other category dimensions (like the code used to be), so that it can be sliced in whichever way the language binding chooses
  // (e.g. to group by function, such as database access, rather than by object type)

  /**
   * Anything concerned with currency manipulation.
   */
  public static final String CURRENCY = "Currency";

  /**
   * Anything concerned with curve objects.
   */
  public static final String CURVE = "Curve";

  /**
   * Anything for debugging or testing purposes (probably available in debug builds only).
   */
  public static final String DEBUG = "Debug";

  /**
   * Anything for diagnostic purposes.
   */
  public static final String DIAGNOSTIC = "Diagnostic";

  /**
   * Anything concerned with identifier manipulation.
   */
  public static final String IDENTIFIER = "Identifier";

  /**
   * Anything concerned with market data (live or snapshot)
   */
  public static final String MARKET_DATA = "MarketData";

  /**
   * Miscellaneous value/data manipulation.
   */
  public static final String MISC = "Misc";

  /**
   * Anything concerned with position and portfolio objects.
   */
  public static final String POSITION = "Position";

  /**
   * Anything concerned with security objects.
   */
  public static final String SECURITY = "Security";

  /**
   * Anything concerned with time-series objects.
   */
  public static final String TIMESERIES = "TimeSeries";

  /**
   * Anything concerned with the engine's value referencing scheme (e.g. value requirements, specifications, properties).
   */
  public static final String VALUE = "Value";

  /**
   * Anything concerned with views.
   */
  public static final String VIEW = "View";

  /**
   * Prevent instantiation.
   */
  private Categories() {
  }

}
