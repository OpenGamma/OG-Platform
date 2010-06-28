/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class SwapRateCalculator {
  private final AnnuityCalculator _annuityCalculator = new AnnuityCalculator();
  private final FloatingLegCalculator _floatLegCalculator = new FloatingLegCalculator();

  /**
   * 
   * @param forwardCurve The curve used to calculate forward LIBOR rates
   * @param fundingCurve The curve used to calculate discount factors
   * @param swap The swap 
   * @return the swap rate
   */
  public double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, Swap swap) {
    final double annuity = _annuityCalculator.getAnnuity(fundingCurve, swap);
    final double floating = _floatLegCalculator.getFloatLeg(forwardCurve, fundingCurve, swap);
    return floating / annuity;
  }
}
