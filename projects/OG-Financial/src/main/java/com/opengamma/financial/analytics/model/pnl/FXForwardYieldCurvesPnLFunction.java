/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Produces the aggregated P&L series across both curves for an FX Forward.
 */
public class FXForwardYieldCurvesPnLFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  /**
   * The compiled form.
   */
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
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), ValueProperties.all().withoutAny(ValuePropertyNames.CURVE_CURRENCY)));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      String resultCurrency;
      if (resultCurrencies == null || resultCurrencies.size() != 1) {
        final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
        final Currency baseCurrency = baseQuotePair.getBase();
        resultCurrency = baseCurrency.getCode();
      } else {
        resultCurrency = Iterables.getOnlyElement(resultCurrencies);
      }
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      final ValueRequirement payPnLSeriesRequirement = getPnLSeriesRequirement(payCurrency, targetSpec, constraints);
      final ValueRequirement receivePnLSeriesRequirement = getPnLSeriesRequirement(receiveCurrency, targetSpec, constraints);
      return ImmutableSet.of(payPnLSeriesRequirement, receivePnLSeriesRequirement);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
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
      return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get()));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final Position position = target.getPosition();
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());

      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency currencyBase = currencyPair.getBase();
      LocalDateDoubleTimeSeries payPnLSeries = null;
      LocalDateDoubleTimeSeries receivePnLSeries = null;
      for (final ComputedValue input : inputs.getAllValues()) {
        if (!ValueRequirementNames.PNL_SERIES.equals(input.getSpecification().getValueName())) {
          continue;
        }
        final LocalDateDoubleTimeSeries pnlSeries = (LocalDateDoubleTimeSeries) input.getValue();
        final Currency pnlSeriesCurveCurrency = Currency.of(input.getSpecification().getProperty(ValuePropertyNames.CURVE_CURRENCY));
        if (pnlSeriesCurveCurrency.equals(payCurrency)) {
          payPnLSeries = pnlSeries;
        } else if (pnlSeriesCurveCurrency.equals(receiveCurrency)) {
          receivePnLSeries = pnlSeries;
        }
      }
      if (payPnLSeries == null || receivePnLSeries == null) {
        throw new OpenGammaRuntimeException("Unable to find both pay and receive curve P&L series in inputs");
      }
      final DoubleTimeSeries<?> result = payPnLSeries.add(receivePnLSeries);
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(resultSpec, result));
    }

    private ValueRequirement getPnLSeriesRequirement(final Currency currency, final ComputationTargetSpecification targetSpec, final ValueProperties constraints) {
      final ValueProperties newConstraints = constraints.copy()
          .with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode())
          .get();
      return new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, newConstraints);
    }
  }

}
