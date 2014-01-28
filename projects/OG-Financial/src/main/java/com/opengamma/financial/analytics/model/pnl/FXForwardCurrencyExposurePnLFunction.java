/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_FX_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.RETURN_SERIES;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Function that calculates the P&L for an FX forward due to movements in the underlying spot rate.
 */
public class FXForwardCurrencyExposurePnLFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurrencyExposurePnLFunction.class);

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  /**
   * Compiled function that calculates the P&L for an FX forward due to movements in the underlying spot rate.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {
    /** The currency pairs */
    private final CurrencyPairs _currencyPairs;

    /**
     * @param currencyPairs The currency pairs, not null
     */
    public Compiled(final CurrencyPairs currencyPairs) {
      ArgumentChecker.notNull(currencyPairs, "currency pairs");
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
      final Set<String> calculationMethods = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
      if (calculationMethods == null || calculationMethods.size() != 1) {
        final ValueProperties newConstraints = constraints.copy()
            .withoutAny(ValuePropertyNames.CALCULATION_METHOD)
            .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), newConstraints));
      }
      final Set<ValueRequirement> requirements = new HashSet<>();
      final String calculationMethod = Iterables.getOnlyElement(calculationMethods);
      final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
      if (CalculationPropertyNamesAndValues.DISCOUNTING.equals(calculationMethod)) {
        requirements.add(new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(target.getPosition().getSecurity()),
          ValueProperties.builder()
            .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
            .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
            .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigs.iterator().next())
            .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
            .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigs.iterator().next()).get()));
      } else if (CalculationPropertyNamesAndValues.FORWARD_POINTS.equals(calculationMethod)) {
        final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.FORWARD_CURVE_NAME);
        if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
          return null;
        }
        final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
        requirements.add(new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(target.getPosition().getSecurity()),
          ValueProperties.builder()
            .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS)
            .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
            .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigs.iterator().next())
            .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
            .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigs.iterator().next())
            .with(ValuePropertyNames.FORWARD_CURVE_NAME, forwardCurveName).get()));
      } else {
        return null;
      }
      final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      final String resultCurrency;
      final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency baseCurrency = baseQuotePair.getBase();
      final Currency nonBaseCurrency = baseQuotePair.getCounter();
      if (resultCurrencies != null && resultCurrencies.size() == 1) {
        final Currency ccy = Currency.of(Iterables.getOnlyElement(resultCurrencies));
        if (!(ccy.equals(payCurrency) || ccy.equals(receiveCurrency))) {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(baseCurrency, ccy)));
          resultCurrency = ccy.getCode();
        } else if (ccy.equals(nonBaseCurrency)) {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(nonBaseCurrency, baseCurrency)));
          resultCurrency = nonBaseCurrency.getCode();
        } else {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(baseCurrency, nonBaseCurrency)));
          resultCurrency = baseCurrency.getCode();
        }
      } else {
        resultCurrency = baseCurrency.getCode();
      }
      final ValueProperties fxSpotConstraints = desiredValue.getConstraints().copy()
          .withoutAny(ValuePropertyNames.PAY_CURVE)
          .withoutAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .withoutAny(ValuePropertyNames.RECEIVE_CURVE)
          .withoutAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
          .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
          .withoutAny(ValuePropertyNames.CALCULATION_METHOD)
          .withoutAny(ValuePropertyNames.FORWARD_CURVE_NAME)
          .with(CURRENCY, resultCurrency).withOptional(CURRENCY)
          .get();
      final ComputationTargetSpecification fxSpotReturnSeriesSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
      requirements.add(new ValueRequirement(ValueRequirementNames.RETURN_SERIES, fxSpotReturnSeriesSpec, fxSpotConstraints));
      return requirements;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      if (inputs.size() == 1) {
        final ValueSpecification input = Iterables.getOnlyElement(inputs.keySet());
        if (ValueRequirementNames.PNL_SERIES.equals(input.getValueName())) {
          return Collections.singleton(input);
        }
      }
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
      if (currencyPair == null) {
        return null;
      }
      final Currency currencyBase = currencyPair.getBase();
      String resultCurrency = null;
      final ValueProperties.Builder builder = createValueProperties();
      for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
        final ValueSpecification inputSpec = entry.getKey();
        final ValueRequirement inputReq = entry.getValue();
        if (inputReq.getValueName().equals(RETURN_SERIES)) {
          final Set<String> resultCurrencies = inputReq.getConstraints().getValues(CURRENCY);
          if (resultCurrencies != null && resultCurrencies.size() == 1) {
            resultCurrency = inputReq.getConstraint(CURRENCY);
          } else {
            resultCurrency = currencyBase.getCode();
          }
        }
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
      if (resultCurrency == null) {
        return null;
      }
      builder.with(ValuePropertyNames.CURRENCY, resultCurrency)
             .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
      return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get()));
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final Position position = target.getPosition();
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
      final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
      final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
      final Currency payCurrency = security.getPayCurrency();
      final Currency receiveCurrency = security.getReceiveCurrency();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency baseCurrency = currencyPair.getBase();
      final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
      final double exposure = mca.getAmount(currencyNonBase);

      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      if (resultCurrencies == null || resultCurrencies.size() != 1) {
        s_logger.warn("No Currency property - returning result in base currency");
        final LocalDateDoubleTimeSeries fxSpotReturnSeries = (LocalDateDoubleTimeSeries) inputs.getValue(ValueRequirementNames.RETURN_SERIES);
        final LocalDateDoubleTimeSeries pnlSeries = fxSpotReturnSeries.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
        return Collections.singleton(new ComputedValue(spec, pnlSeries));
      }
      final Currency resultCurrency = Currency.of(Iterables.getOnlyElement(resultCurrencies));
      final LocalDateDoubleTimeSeries conversionTS = (LocalDateDoubleTimeSeries) inputs.getValue(HISTORICAL_FX_TIME_SERIES);
      if (conversionTS == null) {
        throw new OpenGammaRuntimeException("Asked for result in " + resultCurrency + " but could not get " + baseCurrency + "/" + resultCurrency + " conversion series");
      }
      if (resultCurrency.equals(baseCurrency)) {
        final LocalDateDoubleTimeSeries fxSpotReturnSeries = (LocalDateDoubleTimeSeries) inputs.getValue(ValueRequirementNames.RETURN_SERIES);
        final LocalDateDoubleTimeSeries pnlSeries = fxSpotReturnSeries.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
        return Collections.singleton(new ComputedValue(spec, pnlSeries));
      }
      final LocalDateDoubleTimeSeries fxSpotReturnSeries = (LocalDateDoubleTimeSeries) inputs.getValue(ValueRequirementNames.RETURN_SERIES);
      final LocalDateDoubleTimeSeries convertedSeries = conversionTS.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
      final LocalDateDoubleTimeSeries pnlSeries = convertedSeries.multiply(fxSpotReturnSeries);
      return Collections.singleton(new ComputedValue(spec, pnlSeries));
    }

  }

}
