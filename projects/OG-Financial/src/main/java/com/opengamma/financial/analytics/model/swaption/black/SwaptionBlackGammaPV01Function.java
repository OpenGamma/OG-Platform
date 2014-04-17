/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.GammaPV01Calculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the gamma PV01 of a swaption using the Black method. This functions produces
 * the change in PV01 when all curves to which the swaption is sensitive are parallel-shifted
 * by +1 bp.
 * @deprecated The parent of this function is deprecated
 */
@Deprecated
public class SwaptionBlackGammaPV01Function extends SwaptionBlackFunction {
  /** The calculator */
  private static final GammaPV01Calculator CALCULATOR = new GammaPV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  /**
   * Sets the value requirement to {@link ValueRequirementNames#GAMMA_PV01}
   */
  public SwaptionBlackGammaPV01Function() {
    super(ValueRequirementNames.GAMMA_PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double gammaPV01 = CALCULATOR.visit(swaption, data);
    return Collections.singleton(new ComputedValue(spec, gammaPV01 / 10000.0));
  }
}
