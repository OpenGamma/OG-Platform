/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalCallSpreadBlackMethod;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the volatility sensitivity for Forex digital options using vanilla call spread with Black formula for underlying vanilla.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The methods used by the different instruments.
   */
  private final ForexOptionDigitalCallSpreadBlackMethod _methodFxOptionDigital;

  /**
   * Private constructor.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator(final double spread) {
    _methodFxOptionDigital = new ForexOptionDigitalCallSpreadBlackMethod(spread);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return _methodFxOptionDigital.presentValueBlackVolatilitySensitivity(derivative, data);
  }

}
