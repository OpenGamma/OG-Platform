/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

/**
 * 
 */
public class MultiYieldCurvePropertiesAndDefaults {
  /** Root finder absolute tolerance property name */
  public static final String PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE = "RootFinderAbsoluteTolerance";
  /** Relative tolerance property name */
  public static final String PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE = "RootFinderRelativeTolerance";
  /** Absolute tolerance property name */
  public static final String PROPERTY_ROOT_FINDER_MAX_ITERATIONS = "RootFinderMaximumIterations";
  /** Matrix decomposition property name */
  public static final String PROPERTY_DECOMPOSITION = "MatrixDecomposition";
  /** Property name denoting whether or not to use finite difference for sensitivity calculations */
  public static final String PROPERTY_USE_FINITE_DIFFERENCE = "UseFiniteDifferenceSensitivities";
  /** Label setting this function to use the par rate of the instruments in root-finding */
  public static final String PAR_RATE_STRING = "ParRate";
  /** Label setting this function to use the present value of the instruments in root-finding */
  public static final String PRESENT_VALUE_STRING = "PresentValue";

}
