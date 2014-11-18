/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;

/**
 * Simple set of data related to inflation to be used in tests.
 */
public class SimpleDataSetsInflationUsd {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, 
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D EXP_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.EXPONENTIAL, 
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Currency USD = Currency.USD;
  private static final double[] DSC_TIME = 
      new double[] {0.0, 0.25, 0.50, 1.00, 2.00, 3.00, 4.00, 5.00, 10.0, 30.00 };
  private static final double[] DSC_RATE = 
      new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140, 0.0150, 0.0150, 0.0150, 0.0150 };
  private static final String DSC_NAME = "USD-DSCON";
  private static final YieldAndDiscountCurve DSC = 
      new YieldCurve(DSC_NAME, new InterpolatedDoublesCurve(DSC_TIME, DSC_RATE, LINEAR_FLAT, true, DSC_NAME));

  private static final String NAME_PRICE_INDEX = "US CPI-U";
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME_PRICE_INDEX, USD);
  private static final double[] INDEX_VALUE_USD = 
      new double[] {230, 230 * 1.02, 230 * Math.pow(1.02, 5), 230 * Math.pow(1.02, 10), 230 * Math.pow(1.02, 50) }; // Jan 2013
  private static final double[] TIME_VALUE_USD = new double[] {0.00, 1.00, 5.00, 10.00, 50.00 };
  private static final InterpolatedDoublesCurve CURVE = 
      InterpolatedDoublesCurve.from(TIME_VALUE_USD, INDEX_VALUE_USD, EXP_FLAT, NAME_PRICE_INDEX);
  private static final PriceIndexCurveSimple PRICE_INDEX_CURVE = new PriceIndexCurveSimple(CURVE);
  
  private static final InflationProviderDiscount INFLATION = new InflationProviderDiscount();
  static {
    INFLATION.setCurve(USD, DSC);
    INFLATION.setCurve(PRICE_INDEX, PRICE_INDEX_CURVE);
  }
  
  /**
   * Returns an inflation provider with one currency (USD), and one inflation (US CPI-U).
   * @return The market.
   */
  public static InflationProviderDiscount getProvider() {
    return INFLATION;
  }
  
  /**
   * Returns the price index for the provider.
   * @return The index.
   */
  public static IndexPrice getPriceIndex() {
    return PRICE_INDEX;
  }
  

}
