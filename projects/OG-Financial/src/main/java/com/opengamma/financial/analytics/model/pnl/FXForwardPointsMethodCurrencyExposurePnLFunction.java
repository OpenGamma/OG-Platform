/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardPointsMethodCurrencyExposurePnLFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final CurrencyPairs _currencyPairs;

    public Compiled(final CurrencyPairs currencyPairs) {
      _currencyPairs = currencyPairs;
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.POSITION;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), ValueProperties.all()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
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
      final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.FORWARD_CURVE_NAME);
      if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
        return null;
      }
      final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
      final ValueRequirement fxCurrencyExposureRequirement = new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(target.getPosition().getSecurity()),
          ValueProperties.builder()
          .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS)
          .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
          .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigs.iterator().next())
          .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
          .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigs.iterator().next())
          .with(ValuePropertyNames.FORWARD_CURVE_NAME, forwardCurveNames.iterator().next())
          .get());
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());

      final ValueProperties fxSpotConstraints = desiredValue.getConstraints().copy()
          .withoutAny(ValuePropertyNames.PAY_CURVE)
          .withoutAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .withoutAny(ValuePropertyNames.RECEIVE_CURVE)
          .withoutAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
          .withoutAny(ValuePropertyNames.CALCULATION_METHOD)
          .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
          .withoutAny(ValuePropertyNames.FORWARD_CURVE_NAME)
          .get();
      final ComputationTargetSpecification fxSpotReturnSeriesSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
      final ValueRequirement fxSpotReturnSeriesRequirement = new ValueRequirement(ValueRequirementNames.RETURN_SERIES, fxSpotReturnSeriesSpec, fxSpotConstraints);
      return ImmutableSet.of(fxCurrencyExposureRequirement, fxSpotReturnSeriesRequirement);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
      if (currencyPair == null) {
        return null;
      }
      final Currency currencyBase = currencyPair.getBase();

      final ValueProperties.Builder builder = createValueProperties();
      for (final ValueSpecification inputSpec : inputs.keySet()) {
        for (final String propertyName : inputSpec.getProperties().getProperties()) {
          if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
            continue;
          }
          final Set<String> values = inputSpec.getProperties().getValues(propertyName);
          if (values == null || values.isEmpty()) {
            builder.withAny(propertyName);
          } else {
            builder.with(propertyName, values);
          }
        }
      }
      builder.withoutAny(ValuePropertyNames.CURRENCY)
          .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
          .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE);

      return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get()));
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final Position position = target.getPosition();
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
      final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
      final Currency payCurrency = security.getPayCurrency();
      final Currency receiveCurrency = security.getReceiveCurrency();
      final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
      final double exposure = mca.getAmount(currencyNonBase);

      final LocalDateDoubleTimeSeries fxSpotReturnSeries = (LocalDateDoubleTimeSeries) inputs.getValue(ValueRequirementNames.RETURN_SERIES);
      final LocalDateDoubleTimeSeries pnlSeries = fxSpotReturnSeries.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(spec, pnlSeries));
    }

  }

}
