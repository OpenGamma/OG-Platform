/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalCallSpreadBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value curve sensitivity of forex exotic options instruments by static replication 
 * with vanilla products.
 */
public final class PresentValueCurveSensitivityForexStaticReplicationSmileCalculator
    extends InstrumentDerivativeVisitorSameMethodAdapter<
    BlackForexSmileProviderInterface,
    MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexStaticReplicationSmileCalculator INSTANCE =
      new PresentValueCurveSensitivityForexStaticReplicationSmileCalculator();

  private PresentValueCurveSensitivityForexStaticReplicationSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexStaticReplicationSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionDigitalCallSpreadBlackSmileMethod METHOD_DIG =
      new ForexOptionDigitalCallSpreadBlackSmileMethod();

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(InstrumentDerivative derivative,
      BlackForexSmileProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionDigital(final ForexOptionDigital option,
      final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_DIG.presentValueCurveSensitivity(option, blackSmile);
  }

}
