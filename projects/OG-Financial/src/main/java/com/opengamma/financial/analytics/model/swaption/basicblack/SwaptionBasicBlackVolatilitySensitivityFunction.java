/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.ConstantBlackDiscountingValueVegaSwaptionFunction;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the value vega of a swaption using the Black method with no volatility modelling assumptions.
 * The implied volatility is read directly from the market data system.
 * <p>
 * Produces a result for {@link ValueRequirementNames#VALUE_VEGA} using {@link PresentValueBlackSwaptionSensitivityBlackCalculator}.
 * @deprecated Use {@link ConstantBlackDiscountingValueVegaSwaptionFunction}
 */
@Deprecated
public class SwaptionBasicBlackVolatilitySensitivityFunction extends SwaptionBasicBlackFunction {
  /** The calculator */
  private static final PresentValueBlackSwaptionSensitivityBlackCalculator CALCULATOR = PresentValueBlackSwaptionSensitivityBlackCalculator.getInstance();

  /**
   * Sets {@link ValueRequirementNames#VALUE_VEGA} as the result.
   */
  public SwaptionBasicBlackVolatilitySensitivityFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final PresentValueSwaptionSurfaceSensitivity sensitivities = swaption.accept(CALCULATOR, data);
    final HashMap<DoublesPair, Double> result = sensitivities.getSensitivity().getMap();
    if (result.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one result for Black value vega");
    }
    return Collections.singleton(new ComputedValue(spec, result.values().iterator().next()));
  }
}
