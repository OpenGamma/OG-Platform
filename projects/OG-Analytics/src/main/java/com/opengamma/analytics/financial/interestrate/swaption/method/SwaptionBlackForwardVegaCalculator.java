/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the forward vega of swaptions using the Black model.
 * @deprecated {@link YieldCurveBundle} is deprecated
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

  /** Physical swaption methods */
  private static final SwaptionPhysicalFixedIborBlackMethod PHYSICAL_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /** Cash-settled swaption methods */
  private static final SwaptionCashFixedIborBlackMethod CASH_SWAPTION = SwaptionCashFixedIborBlackMethod.getInstance();
  /** Physical fixed compounded / overnight compounded methods */
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod PHYSICAL_COMPOUNDED_SWAPTION = SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance();

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

  @Override
  public Double visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_COMPOUNDED_SWAPTION.forwardVegaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardVegaCalculator visitor SwaptionPhysicalFixedCompoundedONCompounded requires a YieldCurveWithBlackSwaptionBundle as data.");
  }
}
