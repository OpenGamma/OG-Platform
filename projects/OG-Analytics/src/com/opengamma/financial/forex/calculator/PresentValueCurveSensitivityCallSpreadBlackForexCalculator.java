/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.forex.method.ForexOptionDigitalCallSpreadBlackMethod;
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the present value curve sensitivity for Forex digital options using vanilla call spread with Black formula for underlying vanilla.
 * To compute the curve sensitivity, the Black volatility is kept constant; the volatility is not recomputed for curve and forward changes.
 */
public class PresentValueCurveSensitivityCallSpreadBlackForexCalculator extends PresentValueCurveSensitivityForexCalculator {

  /**
   * The methods used by the different instruments.
   */
  private final ForexOptionDigitalCallSpreadBlackMethod _methodFxOptionDigital;

  /**
   * Private constructor.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public PresentValueCurveSensitivityCallSpreadBlackForexCalculator(final double spread) {
    _methodFxOptionDigital = new ForexOptionDigitalCallSpreadBlackMethod(spread);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return _methodFxOptionDigital.presentValueCurveSensitivity(derivative, data);
  }

}
