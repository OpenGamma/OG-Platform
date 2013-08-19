/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableOptionBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.calculator.blackforex.CurrencyExposureForexBlackSmileCalculator;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the currency exposure for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 * To compute the currency exposure, the Black volatility is kept constant; the volatility is not recomputed for spot and forward changes.
 * @deprecated Calculators that use and populate {@link YieldCurveBundle}s are deprecated.
 * Use {@link CurrencyExposureForexBlackSmileCalculator}.
 */
@Deprecated
public final class CurrencyExposureBlackSmileForexCalculator extends CurrencyExposureForexCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureBlackSmileForexCalculator s_instance = new CurrencyExposureBlackSmileForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureBlackSmileForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  private CurrencyExposureBlackSmileForexCalculator() {
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
    return METHOD_FXOPTION.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONBARRIER.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final YieldCurveBundle data) {
    return METHOD_NDO.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONDIGITAL.currencyExposure(derivative, data);
  }

}
