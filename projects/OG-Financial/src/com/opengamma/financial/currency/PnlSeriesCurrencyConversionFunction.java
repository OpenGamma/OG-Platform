/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixed;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

// TODO jonathan 2012-02-01 -- added this for immediate demo needs. Need to revisit and fully implement.

/**
 *
 */
public abstract class PnlSeriesCurrencyConversionFunction extends AbstractFunction.NonCompiledInvoker {

  private static final String CONVERSION_CCY_PROPERTY = "ConversionCurrency";

  private final String _currencyMatrixName;

  private CurrencyMatrix _currencyMatrix;

  public PnlSeriesCurrencyConversionFunction(final String currencyMatrixName) {
    _currencyMatrixName = currencyMatrixName;
  }

  protected CurrencyMatrix getCurrencyMatrix() {
    return _currencyMatrix;
  }

  protected void setCurrencyMatrix(final CurrencyMatrix currencyMatrix) {
    _currencyMatrix = currencyMatrix;
  }

  protected String getCurrencyMatrixName() {
    return _currencyMatrixName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    final CurrencyMatrix matrix = OpenGammaCompilationContext.getCurrencyMatrixSource(context).getCurrencyMatrix(getCurrencyMatrixName());
    setCurrencyMatrix(matrix);
    if (matrix != null) {
      if (matrix.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(this, matrix.getUniqueId());
      }
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue originalPnlSeriesComputedValue = inputs.getComputedValue(ValueRequirementNames.PNL_SERIES);
    final HistoricalTimeSeries hts = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    LocalDateDoubleTimeSeries conversionTS = hts.getTimeSeries();
    final LocalDateDoubleTimeSeries originalPnlSeries = (LocalDateDoubleTimeSeries) originalPnlSeriesComputedValue.getValue();
    final Currency originalCurrency = Currency.of(originalPnlSeriesComputedValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY));
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String desiredCurrencyCode = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(OpenGammaExecutionContext.getConfigSource(executionContext));
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Currency desiredCurrency = Currency.of(desiredCurrencyCode);
    if (currencyPairs.getCurrencyPair(originalCurrency, desiredCurrency).getBase().equals(desiredCurrency)) {
      conversionTS = conversionTS.reciprocal().toLocalDateDoubleTimeSeries();
    }
    final LocalDateDoubleTimeSeries fxSeries = convertSeries(originalPnlSeries, conversionTS, originalCurrency, desiredCurrency);
    return ImmutableSet.of(new ComputedValue(getValueSpec(originalPnlSeriesComputedValue.getSpecification(), desiredCurrencyCode), fxSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return getCurrencyMatrix() != null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> desiredCurrencyValues = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencyValues == null || desiredCurrencyValues.size() != 1) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY).withAny(ValuePropertyNames.CURRENCY)
        .with(CONVERSION_CCY_PROPERTY, Iterables.getOnlyElement(desiredCurrencyValues))
        .withOptional(CONVERSION_CCY_PROPERTY).get();
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), constraints));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueRequirement inputRequirement = Iterables.getOnlyElement(inputs.values());
    final ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    return ImmutableSet.of(getValueSpec(inputSpec, inputRequirement.getConstraint(CONVERSION_CCY_PROPERTY)));
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    try {
      final ValueSpecification input = inputs.iterator().next(); // only one input, so must be the P&L Series
      final Currency originalCurrency = Currency.of(input.getProperty(ValuePropertyNames.CURRENCY));
      final ValueSpecification output = outputs.iterator().next(); // only one output, so must be the result P&L Series
      final Currency desiredCurrency = Currency.of(output.getProperty(ValuePropertyNames.CURRENCY));
      return getCurrencyMatrix().getConversion(originalCurrency, desiredCurrency).accept(
        new TimeSeriesCurrencyConversionRequirements(OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context)));
    } catch (final Exception e) {
      return null;
    }
  }

  protected ValueSpecification getValueSpec(final ValueSpecification inputSpec, final String currencyCode) {
    final ValueProperties properties = inputSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .withoutAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURRENCY, currencyCode).get();
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, inputSpec.getTargetSpecification(), properties);
  }

  private LocalDateDoubleTimeSeries convertSeries(final LocalDateDoubleTimeSeries sourceTs, final LocalDateDoubleTimeSeries conversionTS, final Currency sourceCurrency,
      final Currency targetCurrency) {
    final CurrencyMatrixValue fxConversion = getCurrencyMatrix().getConversion(sourceCurrency, targetCurrency);
    return fxConversion.accept(new TimeSeriesCurrencyConverter(sourceTs, conversionTS));
  }

  //-------------------------------------------------------------------------
  private class TimeSeriesCurrencyConverter implements CurrencyMatrixValueVisitor<LocalDateDoubleTimeSeries> {

    private final LocalDateDoubleTimeSeries _baseTs;
    private final LocalDateDoubleTimeSeries _conversionTS;

    public TimeSeriesCurrencyConverter(final LocalDateDoubleTimeSeries baseTs, final LocalDateDoubleTimeSeries conversionTS) {
      _baseTs = baseTs;
      _conversionTS = conversionTS;
    }

    public LocalDateDoubleTimeSeries getBaseTimeSeries() {
      return _baseTs;
    }

    public LocalDateDoubleTimeSeries getConversionTimeSeries() {
      return _conversionTS;
    }

    @Override
    public LocalDateDoubleTimeSeries visitFixed(final CurrencyMatrixFixed fixedValue) {
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(fixedValue.getFixedValue());
    }

    @Override
    public LocalDateDoubleTimeSeries visitValueRequirement(final CurrencyMatrixValueRequirement uniqueId) {
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(getConversionTimeSeries());
    }

    @Override
    public LocalDateDoubleTimeSeries visitCross(final CurrencyMatrixCross cross) {
      // TODO
      throw new UnsupportedOperationException();
    }

  }

  private class TimeSeriesCurrencyConversionRequirements implements CurrencyMatrixValueVisitor<Set<ValueRequirement>> {

    private final HistoricalTimeSeriesResolver _timeSeriesResolver;

    public TimeSeriesCurrencyConversionRequirements(final HistoricalTimeSeriesResolver timeSeriesResolver) {
      _timeSeriesResolver = timeSeriesResolver;
    }

    public HistoricalTimeSeriesResolver getTimeSeriesResolver() {
      return _timeSeriesResolver;
    }

    @Override
    public Set<ValueRequirement> visitFixed(final CurrencyMatrixFixed fixedValue) {
      return Collections.emptySet();
    }

    @Override
    public Set<ValueRequirement> visitValueRequirement(final CurrencyMatrixValueRequirement uniqueId) {
      final ValueRequirement requirement = uniqueId.getValueRequirement();
      final ExternalId targetId = requirement.getTargetSpecification().getIdentifier();
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(targetId.toBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      // TODO: this is not great, but we don't know the range of the underlying time series when the graph is built
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE, DateConstraint.EARLIEST_START, true,
          DateConstraint.VALUATION_TIME, true));
    }

    @Override
    public Set<ValueRequirement> visitCross(final CurrencyMatrixCross cross) {
      // TODO
      throw new UnsupportedOperationException();
    }

  }

}
