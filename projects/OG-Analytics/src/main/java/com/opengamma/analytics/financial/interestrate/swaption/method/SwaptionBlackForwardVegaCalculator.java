/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
@Deprecated
public class SwaptionBlackForwardVegaCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final SwaptionBlackForwardVegaCalculator INSTANCE = new SwaptionBlackForwardVegaCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwaptionBlackForwardVegaCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  SwaptionBlackForwardVegaCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod PHYSICAL_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod CASH_SWAPTION = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return CASH_SWAPTION.forwardVegaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardVegaCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_SWAPTION.forwardVegaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardVegaCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }
}
