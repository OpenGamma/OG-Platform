/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using the Black formula.
 */
public final class PresentValueBlackSwaptionSensitivityBlackCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueBlackSwaptionSensitivity> {

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

  @Override
  public PresentValueBlackSwaptionSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(derivative, "derivative");
    return derivative.accept(this, curves);
  }

  @Override
  public PresentValueBlackSwaptionSensitivity visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_CASH.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException("The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public PresentValueBlackSwaptionSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException(
        "The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

}
