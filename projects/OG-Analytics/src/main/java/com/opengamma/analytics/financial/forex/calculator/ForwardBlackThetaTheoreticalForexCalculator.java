/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated.
 */
@Deprecated
public class ForwardBlackThetaTheoreticalForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ForwardBlackThetaTheoreticalForexCalculator INSTANCE = new ForwardBlackThetaTheoreticalForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ForwardBlackThetaTheoreticalForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ForwardBlackThetaTheoreticalForexCalculator() {
  }

  /** Vanilla option calculator */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();
  /** Digital option calculator */
  private static final ForexOptionDigitalBlackMethod METHOD_FXDIGITAL = ForexOptionDigitalBlackMethod.getInstance();
  /** Barrier option calculator */
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    ArgumentChecker.isTrue(data instanceof SmileDeltaTermStructureDataBundle, "Must have data bundle with volatility data");
    return METHOD_FXOPTIONVANILLA.thetaTheoretical(derivative, data);
  }

  @Override
  public Double visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    ArgumentChecker.isTrue(data instanceof SmileDeltaTermStructureDataBundle, "Must have data bundle with volatility data");
    return METHOD_FXDIGITAL.thetaTheoretical(derivative, data);
  }

  @Override
  public Double visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    ArgumentChecker.isTrue(data instanceof SmileDeltaTermStructureDataBundle, "Must have data bundle with volatility data");
    return METHOD_FXBARRIER.thetaTheoretical(derivative, data);
  }
}
