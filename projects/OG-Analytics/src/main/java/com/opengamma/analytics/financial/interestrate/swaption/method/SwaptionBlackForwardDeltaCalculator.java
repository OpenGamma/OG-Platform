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
 * Calculates the theoretical forward delta of swaptions using the Black model.
 * @deprecated {@link YieldCurveBundle} is deprecated.
 */
@Deprecated
public class SwaptionBlackForwardDeltaCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final SwaptionBlackForwardDeltaCalculator INSTANCE = new SwaptionBlackForwardDeltaCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwaptionBlackForwardDeltaCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  SwaptionBlackForwardDeltaCalculator() {
  }

  /** Physically-settled swaption methods */
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
      return CASH_SWAPTION.forwardDeltaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardDeltaCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_SWAPTION.forwardDeltaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardDeltaCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_COMPOUNDED_SWAPTION.forwardDeltaTheoretical(swaption, curvesBlack);
    }
    throw new UnsupportedOperationException("The SwaptionBlackForwardDeltaCalculator visitor visitSwaptionPhysicalFixedCompoundedONCompounded " +
        "requires a YieldCurveWithBlackSwaptionBundle as data.");
  }
}
