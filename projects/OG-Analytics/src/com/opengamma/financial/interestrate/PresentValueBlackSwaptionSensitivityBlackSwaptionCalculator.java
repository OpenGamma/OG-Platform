/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueBlackSwaptionSensitivity> {

  /**
   * The unique instance of the SABR sensitivity calculator.
   */
  private static final PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator s_instance = new PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator() {
  }

  /**
   * Methods.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public PresentValueBlackSwaptionSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public PresentValueBlackSwaptionSensitivity visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_CASH.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException("The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public PresentValueBlackSwaptionSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL.presentValueBlackSensitivity(swaption, curveBlack);
    }
    throw new UnsupportedOperationException(
        "The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

}
