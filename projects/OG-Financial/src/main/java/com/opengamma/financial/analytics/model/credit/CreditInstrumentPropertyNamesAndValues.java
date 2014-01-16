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

  /** Property name for the spread curve */
  public static final String PROPERTY_SPREAD_CURVE = "CreditSpreadCurve";
  /** Property name for shifts of the spread curve */
  public static final String PROPERTY_SPREAD_CURVE_SHIFT = "CreditSpreadCurveShift";
  /** Property name for the credit spread curve shift type */
  public static final String PROPERTY_SPREAD_CURVE_SHIFT_TYPE = "CreditSpreadCurveShiftType";
  /** Property name for an additive credit spread curve shift */
  public static final String ADDITIVE_SPREAD_CURVE_SHIFT = "Additive";
  /** Property name for a multiplicative credit spread curve shift */
  public static final String MULTIPLICATIVE_SPREAD_CURVE_SHIFT = "Multiplicative";

  /** Property name for number of integration points to use when valuing a CDS */
  public static final String PROPERTY_N_INTEGRATION_POINTS = "IntegrationPoints";

  /** Property name for the bump to use for spread curves */
  public static final String PROPERTY_SPREAD_CURVE_BUMP = "SpreadCurveBump";
  /** Property name for the spread bump type */
  public static final String PROPERTY_SPREAD_BUMP_TYPE = "SpreadCurveBumpType";
  /** Property name for the bump to use for interest rate curves */
  public static final String PROPERTY_INTEREST_RATE_CURVE_BUMP = "InterestRateCurveBump";
  /** Property name for the spread bump type */
  public static final String PROPERTY_INTEREST_RATE_BUMP_TYPE = "InterestRateCurveBumpType";
  /** Property name for the bump to use for the recovery rate */
  public static final String PROPERTY_RECOVERY_RATE_CURVE_BUMP = "RecoveryRateCurveBump";
  /** Property name for the recovery rate bump type */
  public static final String PROPERTY_RECOVERY_RATE_BUMP_TYPE = "RecoveryRateCurveBumpType";

  /** Property value indicating that CDX are to be priced as a single-name CDS */
  public static final String CDX_AS_SINGLE_NAME_ISDA = "AsSingleNameISDA";
}
