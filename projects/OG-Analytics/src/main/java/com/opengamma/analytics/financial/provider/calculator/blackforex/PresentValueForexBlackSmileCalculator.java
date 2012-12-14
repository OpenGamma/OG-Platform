/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableOptionBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueForexBlackSmileCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<BlackForexSmileProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexBlackSmileCalculator INSTANCE = new PresentValueForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FX_VAN = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackSmileMethod METHOD_NDO = ForexNonDeliverableOptionBlackSmileMethod.getInstance();
  private static final ForexOptionDigitalBlackSmileMethod METHOD_DIG = ForexOptionDigitalBlackSmileMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_BARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final BlackForexSmileProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_FX_VAN.presentValue(option, blackSmile);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_NDO.presentValue(option, blackSmile);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_DIG.presentValue(option, blackSmile);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_BARRIER.presentValue(option, blackSmile);
  }

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
