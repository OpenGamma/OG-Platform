/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.OptionThetaBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the theoretical theta of Forex options in the Black model.
 */
public class FXOptionBlackThetaFunction extends FXOptionBlackSingleValuedFunction {
  private static final String OPTION_THETA = "OptionTheta";
  /**
   * The calculator to compute the theoretical theta value.
   */
  private static final OptionThetaBlackForexCalculator CALCULATOR = OptionThetaBlackForexCalculator.getInstance();
  private static final double DAYS_PER_YEAR = 252; //TODO get rid of this hard-coding

  public FXOptionBlackThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      return Collections.singleton(new ComputedValue(spec, result.getAmount() / DAYS_PER_YEAR));
    }
    throw new OpenGammaRuntimeException("Can only calculate theta for surfaces with smiles");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    return properties.with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    final ValueProperties.Builder properties = super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair);
    return properties.with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue);
    return properties.with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA);
  }
}
