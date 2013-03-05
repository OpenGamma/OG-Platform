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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityForexBlackSmileCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<BlackForexSmileProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexBlackSmileCalculator INSTANCE = new PresentValueCurveSensitivityForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexBlackSmileCalculator getInstance() {
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
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative, final BlackForexSmileProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_FX_VAN.presentValueCurveSensitivity(option, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_NDO.presentValueCurveSensitivity(option, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_DIG.presentValueCurveSensitivity(option, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface blackSmile) {
    return METHOD_BARRIER.presentValueCurveSensitivity(option, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
