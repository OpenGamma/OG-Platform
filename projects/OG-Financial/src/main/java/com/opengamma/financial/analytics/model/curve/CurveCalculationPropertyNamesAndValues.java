/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

/**
 *
 */
public class CurveCalculationPropertyNamesAndValues {

  /**
   * Property name indicating the type of the curves (e.g. discounting or forward).
   */
  public static final String PROPERTY_CURVE_TYPE = "CurveType";

  /**
   * Property value indicating that the curve calculation method was root-finding.
   */
  public static final String ROOT_FINDING = "RootFinding";

  /**
   * Property value indicating that any yield curves are discounting curves and that forward rates
   * are computed as ratios of discount factors.
   */
  public static final String DISCOUNTING = "Discounting";

  /**
   * Property value indicating that the forward rates are calculated directly.
   */
  public static final String FORWARD = "Forward";

  /**
   * Property value indicating that any yield curves are discounting curves and that forward
   * rates are computed as ratios of discount factors, with a convexity adjustment applied
   * using the Hull-White one factor method.
   */
  public static final String HULL_WHITE_DISCOUNTING = "Hull-White Discounting";

  /**
   * The property indicating the name of a set of Hull-White parameters
   */
  public static final String PROPERTY_HULL_WHITE_PARAMETERS = "HullWhiteOneFactorParameters";

  /**
   * The property indicating the currency for which the Hull-White parameters apply.
   */
  public static final String PROPERTY_HULL_WHITE_CURRENCY = "HullWhiteCurrency";

  /**
   * The property indicating the name of a set of G2++ parameters
   */
  public static final String PROPERTY_G2PP_PARAMETERS = "G2ppParameters";

  /**
   * The mean reversion property
   */
  public static final String PROPERTY_HW_MEAN_REVERSION = "HullWhiteOneFactorMeanReversion";

  /**
   * The volatilities property
   */
  public static final String PROPERTY_HW_VOLATILITIES = "HullWhiteOneFactorVolatilities";

  /**
   * The volatility times property
   */
  public static final String PROPERTY_HW_TIMES = "HullWhiteOneFactorTimes";
}
