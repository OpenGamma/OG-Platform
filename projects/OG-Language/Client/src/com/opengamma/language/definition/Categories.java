/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * The standard categories for grouping OpenGamma objects. Local implementations should use
 * these where possible (or extend generically if necessary), with a filter defined that
 * translates to corresponding names suitable for a bound language. 
 */
public final class Categories {

  // Fragments for composing full categories

  /**
   * Anything for debugging or testing purposes (probably available in debug builds only).
   */
  public static final String DEBUG = "Debug";

  /**
   * Anything for diagnostic purposes.
   */
  public static final String DIAGNOSTIC = "Diagnostic";

  /**
   * Anything concerned with security objects.
   */
  public static final String SECURITY = "Security";

  /**
   * Anything concerned with views.
   */
  public static final String VIEW = "View";

  /**
   * Anything concerned with object construction.
   */
  public static final String CONSTRUCTION = "Construction";

  /**
   * Anything concerned with object querying. 
   */
  public static final String QUERY = "Query";

  /**
   * Anything concerned with object modification.
   */
  public static final String MODIFICATION = "Modification";

  /**
   * Anything concerned with object destruction.
   */
  public static final String DESTRUCTION = "Destruction";

  // Composite categories

  /**
   * Security construction.
   */
  public static final String SECURITY_CONSTRUCTION = SECURITY + CONSTRUCTION;

  /**
   * Security search and query.
   */
  public static final String SECURITY_QUERY = SECURITY + QUERY;

  /**
   * View construction.
   */
  public static final String VIEW_CONSTRUCTION = VIEW + CONSTRUCTION;

  /**
   * View interrogation.
   */
  public static final String VIEW_QUERY = VIEW + QUERY;

  /**
   * Prevent instantiation.
   */
  private Categories() {
  }

}
