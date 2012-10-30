/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

/**
 *
 */
public class PDEPropertyNamesAndValues {
  /** Property name for the PDE direction */
  public static final String PROPERTY_PDE_DIRECTION = "PDEDirection";
  /** Forward PDE */
  public static final String FORWARDS = "Forward";
  /** Backward PDE */
  public static final String BACKWARDS = "Backwards";

  /** Property name for the theta value for the PDE */
  public static final String PROPERTY_THETA = "Theta";
  /** Property name for the number of time steps to be used */
  public static final String PROPERTY_NUMBER_TIME_STEPS = "TimeStepsNumber";
  /** Property name for the number of space steps to be used */
  public static final String PROPERTY_NUMBER_SPACE_STEPS = "SpaceStepsNumber";
  /** Property name for the bunching parameter for the time mesh */
  public static final String PROPERTY_TIME_STEP_BUNCHING = "TimeStepsBunching";
  /** Property name for the bunching parameter for the space mesh */
  public static final String PROPERTY_SPACE_STEPS_BUNCHING = "SpaceStepsBunching";
  /** Property name for the interpolator to be used to get values that are between space points on the PDE grid */
  public static final String PROPERTY_SPACE_DIRECTION_INTERPOLATOR = "SpaceDirectionInterpolator";
  /** Property name for the discounting curve to be used to discount forward values (produced by backwards PDEs */
  public static final String PROPERTY_DISCOUNTING_CURVE_NAME = "DiscountingCurveName";

  //Forward PDE-specific properties
  /** Property name for the maximum proxy delta to be used in the space direction for forward PDEs */
  public static final String PROPERTY_MAX_PROXY_DELTA = "MaxProxyDelta";
  /** Property name for the central value of moneyness for the grid for forward PDEs */
  public static final String PROPERTY_CENTRE_MONEYNESS = "CentreMoneyness";

  //Backward PDE-specific properties
  /** Property name for the maximum moneyness scale to be used in the space direction for backwards PDEs */
  public static final String PROPERTY_MAX_MONEYNESS = "MaxMoneyness";
}
