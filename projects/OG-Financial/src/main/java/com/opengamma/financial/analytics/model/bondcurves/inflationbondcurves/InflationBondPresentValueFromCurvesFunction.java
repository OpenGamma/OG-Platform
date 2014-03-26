/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class InflationBondPresentValueFromCurvesFunction extends InflationBondFromCurvesFunction<InflationProviderInterface, Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE} and
   * the calculator to {@link PresentValueFromCurvesCalculator}.
   */
  public InflationBondPresentValueFromCurvesFunction() {
    super(PRESENT_VALUE, PresentValueFromCurvesCalculator.getInstance());
  }

}
