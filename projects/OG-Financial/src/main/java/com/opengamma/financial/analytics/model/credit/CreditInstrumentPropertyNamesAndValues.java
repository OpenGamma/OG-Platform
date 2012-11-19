/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

/**
 *
 */
public class CreditInstrumentPropertyNamesAndValues {
  /** Property name for the discounting curve name */
  public static final String PROPERTY_YIELD_CURVE = "YieldCurve";
  /** Property name for the discounting curve calculation configuration name */
  public static final String PROPERTY_YIELD_CURVE_CALCULATION_CONFIG = "YieldCurveCalculationConfig";
  /** Property name for calculation method of the yield curve */
  public static final String PROPERTY_YIELD_CURVE_CALCULATION_METHOD = "YieldCurveCalculationMethod";

  /** Property name for the hazard rate curve name */
  public static final String PROPERTY_HAZARD_RATE_CURVE = "HazardRateCurve";
  /** Property name for the calculation method of the hazard rate curve */
  public static final String PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD = "HazardRateCurveCalculationMethod";
  /** Property name for the maximum number of iterations when fitting the hazard rate curve */
  public static final String PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS = "HazardRateCurveIterations";
  /** Property name for the tolerance to use when fitting the hazard rate curve */
  public static final String PROPERTY_HAZARD_RATE_CURVE_TOLERANCE = "HazardRateCurveTolerance";
  /** Property name for the range multiplier to use when fitting the hazard rate curve */
  public static final String PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER = "HazardRateCurveRangeMultiplier";

  /** Property name for number of integration points to use when valuing a CDS */
  public static final String PROPERTY_N_INTEGRATION_POINTS = "IntegrationPoints";
}
