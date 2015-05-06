/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSensitivityBlackSwaptionCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value sensitivity to volatility for interest rate instruments using the Black formula.
 * @deprecated {@link YieldCurveBundle} is deprecated. Use classes like
 * {@link PresentValueBlackSensitivityBlackSwaptionCalculator}
 */
@Deprecated
public final class PresentValueBlackSwaptionSensitivityBlackCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, PresentValueSwaptionSurfaceSensitivity> {

  /**
   * The method unique instance.
   */
  private static final PresentValueBlackSwaptionSensitivityBlackCalculator INSTANCE = new PresentValueBlackSwaptionSensitivityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueBlackSwaptionSensitivityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackSwaptionSensitivityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod METHOD_SWAPTION_PHYSICAL_COMPOUNDED = SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance();

  @Override
  public PresentValueSwaptionSurfaceSensitivity visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_CASH.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException("The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public PresentValueSwaptionSurfaceSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException(
        "The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public PresentValueSwaptionSurfaceSensitivity visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL_COMPOUNDED.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException(
        "The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedCompoundedONCompounded requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

}
