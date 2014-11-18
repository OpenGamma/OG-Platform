/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Rolldown function creating a new PriceIndexCurve from an existing one by a time shift of <i>T</i>.
 * The new PriceIndexCurve returns at a time <i>t</i> the same estimated price index as the original curve
 * at the time <i>t + T</i>.
 */
public final class ConstantTimeShiftPriceIndexCurveRolldownFunction implements RolldownFunction<PriceIndexCurve> {
  
  /** The singleton instance */
  private static final ConstantTimeShiftPriceIndexCurveRolldownFunction INSTANCE = 
      new ConstantTimeShiftPriceIndexCurveRolldownFunction();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ConstantTimeShiftPriceIndexCurveRolldownFunction getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ConstantTimeShiftPriceIndexCurveRolldownFunction() {
  }

  @Override
  public PriceIndexCurve rollDown(final PriceIndexCurve priceIndexCurve, final double timeShift) {
    ArgumentChecker.notNull(priceIndexCurve, "price index curve");
    

    final Function1D<Double, Double> timeShiftedFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return priceIndexCurve.getPriceIndex(t + timeShift);
      }

    };
    
    return new PriceIndexCurveSimple(FunctionalDoublesCurve.from(timeShiftedFunction));
  }

}
