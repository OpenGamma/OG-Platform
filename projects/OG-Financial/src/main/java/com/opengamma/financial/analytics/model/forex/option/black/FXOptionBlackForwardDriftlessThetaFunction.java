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
import com.opengamma.analytics.financial.forex.calculator.ForwardBlackDriftlessThetaForexCalculator;
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
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.black.BlackDiscountingForwardDriftlessThetaFXOptionFunction;
import com.opengamma.financial.currency.CurrencyPair;

/**
 * The function to compute the forward driftless theta of Forex options in the Black model.
 * @deprecated Use {@link BlackDiscountingForwardDriftlessThetaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackForwardDriftlessThetaFunction extends FXOptionBlackMultiValuedFunction {
  /** The default number of days per year */
  private static final double DEFAULT_DAYS_PER_YEAR = 365.25;

  /**
   * Sets the value requirement to {@link ValueRequirementNames#FORWARD_DRIFTLESS_THETA}
   */
  public FXOptionBlackForwardDriftlessThetaFunction() {
    super(ValueRequirementNames.FORWARD_DRIFTLESS_THETA);
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
      final double result = forex.accept(ForwardBlackDriftlessThetaForexCalculator.getInstance(), data);
      return Collections.singleton(new ComputedValue(spec, result / scale));
    }
    throw new OpenGammaRuntimeException("Can only calculate forward driftless theta for surfaces with smiles");
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    if (constraints.getValues(PROPERTY_DAYS_PER_YEAR) == null || constraints.getValues(PROPERTY_DAYS_PER_YEAR).size() != 1) {
      final ValueProperties newConstraints = constraints.copy()
          .withoutAny(PROPERTY_DAYS_PER_YEAR)
          .with(PROPERTY_DAYS_PER_YEAR, Double.toString(DEFAULT_DAYS_PER_YEAR))
          .get();
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.FORWARD_DRIFTLESS_THETA, target.toSpecification(), newConstraints));
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final Map.Entry<ValueSpecification, ValueRequirement> entry = Iterables.getOnlyElement(inputs.entrySet());
      if (ValueRequirementNames.FORWARD_DRIFTLESS_THETA.equals(entry.getKey().getValueName())) {
        return Collections.singleton(entry.getKey());
      }
    }
    return super.getResults(context, target, inputs);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return super.getResultProperties(target)
        .withAny(CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    return super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair,
        optionalProperties)
        .withAny(PROPERTY_DAYS_PER_YEAR);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final Set<String> daysPerYear = desiredValue.getConstraints().getValues(CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair);
    if (daysPerYear == null || daysPerYear.isEmpty()) {
      return properties.with(CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR, Double.toString(DEFAULT_DAYS_PER_YEAR));
    }
    return properties.with(CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR, daysPerYear);
  }

}
