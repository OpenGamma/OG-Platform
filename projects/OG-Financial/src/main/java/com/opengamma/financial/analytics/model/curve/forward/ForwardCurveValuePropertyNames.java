/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

/**
 *
 */
public class ForwardCurveValuePropertyNames {
  /** Property name for the forward curve interpolator */
  public static final String PROPERTY_FORWARD_CURVE_INTERPOLATOR = "ForwardCurveInterpolator";
  /** Property name for the forward curve left extrapolator */
  public static final String PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR = "ForwardCurveLeftExtrapolator";
  /** Property name for the forward curve right extrapolator */
  public static final String PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR = "ForwardCurveRightExtrapolator";
  /** Property name for the forward curve calculation method */
  public static final String PROPERTY_FORWARD_CURVE_CALCULATION_METHOD = "ForwardCurveCalculationMethod";
  /** Name indicating that the forward curve was constructed from yield curves and a spot rate */
  public static final String PROPERTY_YIELD_CURVE_IMPLIED_METHOD = "YieldCurveImplied";
  /** Name indicating that the forward curve was constructed from market quotes */
  public static final String PROPERTY_MARKET_QUOTES_METHOD = "MarketQuotes";
  /** Name indicating that the forward curve was constructed from a future curve*/
  public static final String PROPERTY_FUTURE_PRICE_METHOD = "FuturePriceMethod";
  /** Property name for the forward curve name */
  public static final String PROPERTY_FORWARD_CURVE_NAME = "ForwardCurveName";

}
