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
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * Calculator of the present value volatility sensitivity for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public final class PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator extends
    InstrumentDerivativeVisitorAdapter<SmileDeltaTermStructureDataBundle, PresentValueForexBlackVolatilityNodeSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator INSTANCE = new PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTION = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXOPTIONBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackMethod METHOD_NDO = ForexNonDeliverableOptionBlackMethod.getInstance();
  private static final ForexOptionDigitalBlackMethod METHOD_FXOPTIONDIGITAL = ForexOptionDigitalBlackMethod.getInstance();

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionVanilla(final ForexOptionVanilla option, final SmileDeltaTermStructureDataBundle data) {
    return METHOD_FXOPTION.presentValueBlackVolatilityNodeSensitivity(option, data);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final SmileDeltaTermStructureDataBundle data) {
    return METHOD_FXOPTIONBARRIER.presentValueBlackVolatilityNodeSensitivity(option, data);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final SmileDeltaTermStructureDataBundle data) {
    return METHOD_NDO.presentValueVolatilityNodeSensitivity(option, data);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionDigital(final ForexOptionDigital option, final SmileDeltaTermStructureDataBundle data) {
    return METHOD_FXOPTIONDIGITAL.presentValueBlackVolatilityNodeSensitivity(option, data);
  }

}
