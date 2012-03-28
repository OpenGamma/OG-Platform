/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.forex.method.ForexOptionDigitalCallSpreadBlackMethod;
import com.opengamma.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the volatility sensitivity for Forex digital options using vanilla call spread with Black formula for underlying vanilla.
 */
public class PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The methods used by the different instruments.
   */
  private final ForexOptionDigitalCallSpreadBlackMethod _methodFxOptionDigital;

  /**
   * Private constructor.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator(final double spread) {
    _methodFxOptionDigital = new ForexOptionDigitalCallSpreadBlackMethod(spread);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return _methodFxOptionDigital.presentValueVolatilitySensitivity(derivative, data);
  }

}
