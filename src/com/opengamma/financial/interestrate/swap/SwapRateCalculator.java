/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class SwapRateCalculator {
  private final AnnuityCalculator _annuityCalculator = new AnnuityCalculator();
  private final FloatingLegCalculator _floatLegCalculator = new FloatingLegCalculator();

  public double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final InterestRateDerivative derivative) {
    if (derivative instanceof Swap) {
      return getRateFromSwap(forwardCurve, fundingCurve, (Swap) derivative);
    } else if (derivative instanceof Cash) {
      return getRateFromCash(fundingCurve, (Cash) derivative);
    } else if (derivative instanceof ForwardRateAgreement) {
      return getRateFromFRA(forwardCurve, fundingCurve, (ForwardRateAgreement) derivative);
    } else if (derivative instanceof InterestRateFuture) {
      return getRateFromIRFuture(fundingCurve, (InterestRateFuture) derivative);
    } else if (derivative instanceof Libor) {
      return getRateFromLibor(forwardCurve, (Libor) derivative);
    }
    throw new IllegalArgumentException("Unhandled InterestRateDerivative type");
  }

  /**
   * 
   * @param forwardCurve The curve used to calculate forward LIBOR rates
   * @param fundingCurve The curve used to calculate discount factors
   * @param swap The swap 
   * @return the swap rate
   */
  private double getRateFromSwap(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final Swap swap) {
    Validate.notNull(forwardCurve);
    Validate.notNull(fundingCurve);
    Validate.notNull(swap);
    final double annuity = _annuityCalculator.getAnnuity(fundingCurve, swap);
    final double floating = _floatLegCalculator.getFloatLeg(forwardCurve, fundingCurve, swap);
    return floating / annuity;
  }

  private double getRateFromCash(final YieldAndDiscountCurve fundingCurve, final Cash cash) {
    Validate.notNull(fundingCurve);
    Validate.notNull(cash);
    return fundingCurve.getInterestRate(cash.getPaymentTime());
  }

  private double getRateFromFRA(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final ForwardRateAgreement fra) {
    Validate.notNull(forwardCurve);
    Validate.notNull(fundingCurve);
    Validate.notNull(fra);
    final double delta = fra.getEndTime() - fra.getStartTime();
    return (forwardCurve.getDiscountFactor(fra.getStartTime()) / forwardCurve.getDiscountFactor(fra.getEndTime()) - 1) / delta;
  }

  private double getRateFromIRFuture(final YieldAndDiscountCurve fundingCurve, final InterestRateFuture interestRateFuture) {
    Validate.notNull(fundingCurve);
    Validate.notNull(interestRateFuture);
    final double delta = interestRateFuture.getEndTime() - interestRateFuture.getStartTime();
    return (fundingCurve.getDiscountFactor(interestRateFuture.getStartTime()) / fundingCurve.getDiscountFactor(interestRateFuture.getEndTime()) - 1) / delta;
  }

  private double getRateFromLibor(final YieldAndDiscountCurve forwardCurve, final Libor libor) {
    Validate.notNull(libor);
    Validate.notNull(forwardCurve);
    return (1. / forwardCurve.getDiscountFactor(libor.getPaymentTime()) - 1) / libor.getPaymentTime();
  }
}
