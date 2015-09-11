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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of forex exotic options instruments by static replication with vanilla products.
 */
public final class PresentValueForexStaticReplicationSmileCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<BlackForexSmileProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexStaticReplicationSmileCalculator INSTANCE =
      new PresentValueForexStaticReplicationSmileCalculator();

  private PresentValueForexStaticReplicationSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexStaticReplicationSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionDigitalCallSpreadBlackSmileMethod METHOD_DIG =
      new ForexOptionDigitalCallSpreadBlackSmileMethod();

  @Override
  public MultipleCurrencyAmount visit(InstrumentDerivative derivative,
      BlackForexSmileProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  @Override
  public MultipleCurrencyAmount visit(InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital option,
      final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_DIG.presentValue(option, blackSmile);
  }

}
