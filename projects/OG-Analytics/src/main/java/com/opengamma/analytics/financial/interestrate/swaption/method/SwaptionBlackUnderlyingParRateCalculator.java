/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the theoretical forward delta of swaptions using the Black model.
 * @deprecated {@link YieldCurveBundle} is deprecated.
 */
@Deprecated
public class SwaptionBlackUnderlyingParRateCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final SwaptionBlackUnderlyingParRateCalculator INSTANCE = new SwaptionBlackUnderlyingParRateCalculator();

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PAR_RATE = ParRateCalculator.getInstance();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwaptionBlackUnderlyingParRateCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  SwaptionBlackUnderlyingParRateCalculator() {
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    return swaption.getUnderlyingSwap().accept(PAR_RATE, curves);
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    return swaption.getUnderlyingSwap().accept(PAR_RATE, curves);
  }

  @Override
  public Double visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    return swaption.getUnderlyingSwap().accept(PAR_RATE, curves);
  }
}
