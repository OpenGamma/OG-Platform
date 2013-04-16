/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardCurveFromYieldCurvesFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(baseQuotePairs);
  }

  /**
   * Compiled form.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final CurrencyPairs _baseQuotePairs;

    public Compiled(final CurrencyPairs baseQuotePairs) {
      _baseQuotePairs = baseQuotePairs;
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties properties = createValueProperties()
          .withAny(ValuePropertyNames.CURVE)
          .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
          .withAny(ValuePropertyNames.PAY_CURVE)
          .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .withAny(ValuePropertyNames.RECEIVE_CURVE)
          .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
      return Collections.singleton(spec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> fxForwardCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
      if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
        return null;
      }
      final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
      if (payCurveNames == null || payCurveNames.size() != 1) {
        return null;
      }
      final Set<String> payCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      if (payCurveCalculationConfigs == null || payCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
      if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
      if (receiveCurveCalculationConfigs == null || receiveCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      final ValueProperties payCurveProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, Iterables.getOnlyElement(payCurveNames))
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, Iterables.getOnlyElement(payCurveCalculationConfigs))
          .withOptional(ValuePropertyNames.PAY_CURVE)
          .get();
      final ValueProperties receiveCurveProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, Iterables.getOnlyElement(receiveCurveNames))
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, Iterables.getOnlyElement(receiveCurveCalculationConfigs))
          .withOptional(ValuePropertyNames.RECEIVE_CURVE)
          .get();
      final UnorderedCurrencyPair ccyPair = target.getValue(ComputationTargetType.UNORDERED_CURRENCY_PAIR);
      Currency payCurrency;
      Currency receiveCurrency;
      final CurrencyPair baseQuotePair = _baseQuotePairs.getCurrencyPair(ccyPair.getFirstCurrency(), ccyPair.getSecondCurrency());
      if (baseQuotePair == null) {
        throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair " + ccyPair);
      }
      if (baseQuotePair.getBase().equals(ccyPair.getFirstCurrency())) {
        payCurrency = baseQuotePair.getBase();
        receiveCurrency = baseQuotePair.getCounter();
      } else {
        payCurrency = baseQuotePair.getCounter();
        receiveCurrency = baseQuotePair.getBase();
      }
      result.add(ConventionBasedFXRateFunction.getSpotRateRequirement(ccyPair));
      result.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(payCurrency), payCurveProperties));
      result.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(receiveCurrency), receiveCurveProperties));
      return result;
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      Object payCurveObject = null;
      Object receiveCurveObject = null;
      Set<String> payCurveNames = null;
      Set<String> payCurveCalculationConfigs = null;
      Set<String> receiveCurveNames = null;
      Set<String> receiveCurveCalculationConfigs = null;
      final UnorderedCurrencyPair ccyPair = UnorderedCurrencyPair.of(target.getUniqueId());
      Currency payCurrency;
      Currency receiveCurrency;
      final CurrencyPair baseQuotePair = _baseQuotePairs.getCurrencyPair(ccyPair.getFirstCurrency(), ccyPair.getSecondCurrency());
      if (baseQuotePair == null) {
        throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair " + ccyPair);
      }
      if (baseQuotePair.getBase().equals(ccyPair.getFirstCurrency())) {
        payCurrency = baseQuotePair.getBase();
        receiveCurrency = baseQuotePair.getCounter();
      } else {
        payCurrency = baseQuotePair.getCounter();
        receiveCurrency = baseQuotePair.getBase();
      }
      final Double spot = (Double) inputs.getValue(ValueRequirementNames.SPOT_RATE);
      for (final ComputedValue input : inputs.getAllValues()) {
        final ValueSpecification spec = input.getSpecification();
        final ValueProperties properties = spec.getProperties();
        if (spec.getTargetSpecification().getUniqueId().equals(payCurrency.getUniqueId())) {
          payCurveObject = input.getValue();
          payCurveNames = properties.getValues(ValuePropertyNames.CURVE);
          payCurveCalculationConfigs = properties.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        } else if (spec.getTargetSpecification().getUniqueId().equals(receiveCurrency.getUniqueId())) {
          receiveCurveObject = input.getValue();
          receiveCurveNames = properties.getValues(ValuePropertyNames.CURVE);
          receiveCurveCalculationConfigs = properties.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        }
      }
      if (payCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get pay curve");
      }
      if (receiveCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get receive curve");
      }
      if (payCurveNames == null || payCurveNames.size() != 1) {
        throw new OpenGammaRuntimeException("Null or non-unique curve name: " + payCurveNames);
      }
      if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
        throw new OpenGammaRuntimeException("Null or non-unique curve name: " + receiveCurveNames);
      }
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final Set<String> fxForwardCurveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
        throw new OpenGammaRuntimeException("Null or non-unique FX forward curve names: " + fxForwardCurveNames);
      }
      final YieldCurve payCurve = (YieldCurve) payCurveObject;
      final YieldCurve receiveCurve = (YieldCurve) receiveCurveObject;
      final String fxForwardCurveName = fxForwardCurveNames.iterator().next();
      final ForwardCurve fxForwardCurve = new ForwardCurveYieldImplied(spot, payCurve, receiveCurve);
      final ValueProperties properties = createValueProperties()
          .with(ValuePropertyNames.CURVE, fxForwardCurveName)
          .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
          .with(ValuePropertyNames.PAY_CURVE, Iterables.getOnlyElement(payCurveNames))
          .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, Iterables.getOnlyElement(payCurveCalculationConfigs))
          .with(ValuePropertyNames.RECEIVE_CURVE, Iterables.getOnlyElement(receiveCurveNames))
          .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, Iterables.getOnlyElement(receiveCurveCalculationConfigs))
          .get();
      final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
      return Collections.singleton(new ComputedValue(resultSpec, fxForwardCurve));
    }

  }

}
