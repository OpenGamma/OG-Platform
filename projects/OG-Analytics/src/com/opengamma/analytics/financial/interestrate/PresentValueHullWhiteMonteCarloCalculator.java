/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Present value calculator for interest rate instruments using a Hull-White one factor model with Monte Carlo simulations.
 * The random number generator is a NormalRandomNumberGenerator with MersenneTwister() random engine (with default seed).
 */
public class PresentValueHullWhiteMonteCarloCalculator extends PresentValueCalculator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PresentValueHullWhiteMonteCarloCalculator.class);
  /**
   * The default number of path in the Monte Carlo simulation.
   */
  private static final int DEFAULT_NB_PATH = 50000; // 12500;

  /**
   * The calculator unique instance.
   */
  private static final PresentValueHullWhiteMonteCarloCalculator INSTANCE = new PresentValueHullWhiteMonteCarloCalculator();

  public static PresentValueHullWhiteMonteCarloCalculator getInstance() {
    return INSTANCE;
  }

  PresentValueHullWhiteMonteCarloCalculator() {
  }

  @Override
  /**
   * The calculator is for test purposes only! It prices a swaption in the Hull-White model by Monte Carlo. The explicit formula should be used for normal purposes.
   * Do not use this calculator in production.
   */
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    LOGGER.warn("This calculator should be used for test purposes only, not in production!");
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (!(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle)) {
      throw new UnsupportedOperationException("The PresentValueHullWhiteMonteCarloCalculator visitor visitSwaptionPhysicalFixedIbor requires a HullWhiteOneFactorPiecewiseConstantDataBundle as data.");
    }
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    CurrencyAmount pvMC = methodMC
        .presentValue(swaption, swaption.getCurrency(), swaption.getUnderlyingSwap().getFirstLeg().getDiscountCurve(), (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
    return pvMC.getAmount();
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    Validate.notNull(annuity);
    Validate.notNull(curves);
    if (!(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle)) {
      throw new UnsupportedOperationException("The PresentValueHullWhiteMonteCarloCalculator visitor visitSwaptionPhysicalFixedIbor requires a HullWhiteOneFactorPiecewiseConstantDataBundle as data.");
    }
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    CurrencyAmount pvMC = methodMC.presentValue(annuity, annuity.getCurrency(), annuity.getDiscountCurve(), (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
    return pvMC.getAmount();
  }

}
