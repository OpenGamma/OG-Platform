/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableOptionBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public final class PresentValueBlackSmileForexCalculator extends PresentValueMCACalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSmileForexCalculator s_instance = new PresentValueBlackSmileForexCalculator();

  /**
   * Get the unique calculator instance.
   * @return The instance.
   */
  public static PresentValueBlackSmileForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueBlackSmileForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTION = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXOPTIONBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackMethod METHOD_NDO = ForexNonDeliverableOptionBlackMethod.getInstance();
  private static final ForexOptionDigitalBlackMethod METHOD_FXOPTIONDIGITAL = ForexOptionDigitalBlackMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONBARRIER.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final YieldCurveBundle data) {
    return METHOD_NDO.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONDIGITAL.presentValue(derivative, data);
  }

}
