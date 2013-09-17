/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.OptionThetaBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueThetaFXOptionFunction;
import com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the theoretical theta of Forex options in the Black model.
 * @deprecated Use {@link BlackDiscountingValueThetaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackThetaFunction extends FXOptionBlackSingleValuedFunction {
  /** The property name for the theta calculation method */
  private static final String OPTION_THETA = "OptionTheta";
  /**
   * The calculator to compute the theoretical theta value.
   */
  private static final OptionThetaBlackForexCalculator CALCULATOR = OptionThetaBlackForexCalculator.getInstance();
  /** The default number of days per year */
  private static final double DEFAULT_DAYS_PER_YEAR = 365.25;

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_THETA}
   */
  public FXOptionBlackThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties constraints = desiredValue.getConstraints();
    final ValueProperties.Builder resultProperties = constraints.copy();
    final double scale;
    final Set<String> scaleFactors = constraints.getValues(PROPERTY_DAYS_PER_YEAR);
    if (scaleFactors.isEmpty()) {
      scale = DEFAULT_DAYS_PER_YEAR;
      resultProperties
        .withoutAny(PROPERTY_DAYS_PER_YEAR)
        .with(PROPERTY_DAYS_PER_YEAR, Double.toString(DEFAULT_DAYS_PER_YEAR));
    } else {
      scale = Double.parseDouble(scaleFactors.iterator().next());
    }
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      return Collections.singleton(new ComputedValue(spec, result.getAmount() / scale));
    }
    throw new OpenGammaRuntimeException("Can only calculate theta for surfaces with smiles");
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    if (constraints.getValues(PROPERTY_DAYS_PER_YEAR) == null || constraints.getValues(PROPERTY_DAYS_PER_YEAR).size() != 1) {
      final ValueProperties newConstraints = constraints.copy()
          .withoutAny(PROPERTY_DAYS_PER_YEAR)
          .with(PROPERTY_DAYS_PER_YEAR, Double.toString(DEFAULT_DAYS_PER_YEAR))
          .get();
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.VALUE_THETA, target.toSpecification(), newConstraints));
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final Map.Entry<ValueSpecification, ValueRequirement> entry = Iterables.getOnlyElement(inputs.entrySet());
      if (ValueRequirementNames.VALUE_THETA.equals(entry.getKey().getValueName())) {
        return Collections.singleton(entry.getKey());
      }
    }
    return super.getResults(context, target, inputs);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return super.getResultProperties(target)
        .with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA)
        .withAny(PROPERTY_DAYS_PER_YEAR);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    return super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair, optionalProperties)
        .with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA)
        .withAny(PROPERTY_DAYS_PER_YEAR);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String daysPerYear = desiredValue.getConstraint(PROPERTY_DAYS_PER_YEAR);
    return super.getResultProperties(target, desiredValue, baseQuotePair)
        .with(ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA)
        .with(PROPERTY_DAYS_PER_YEAR, daysPerYear);
  }
}
