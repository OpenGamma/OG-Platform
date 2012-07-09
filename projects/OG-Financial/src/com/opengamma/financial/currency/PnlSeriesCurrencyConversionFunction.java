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

  public PnlSeriesCurrencyConversionFunction(String currencyMatrixName) {
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
  public void init(FunctionCompilationContext context) {
    super.init(context);
    CurrencyMatrix matrix = OpenGammaCompilationContext.getCurrencyMatrixSource(context).getCurrencyMatrix(getCurrencyMatrixName());
    setCurrencyMatrix(matrix);
    if (matrix != null) {
      if (matrix.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(this, matrix.getUniqueId());
      }
    }
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final ComputedValue originalPnlSeriesComputedValue = inputs.getComputedValue(ValueRequirementNames.PNL_SERIES);
    final HistoricalTimeSeries conversionTS = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    LocalDateDoubleTimeSeries originalPnlSeries = (LocalDateDoubleTimeSeries) originalPnlSeriesComputedValue.getValue();
    Currency originalCurrency = Currency.of(originalPnlSeriesComputedValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY));
    ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    String desiredCurrencyCode = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    Currency desiredCurrency = Currency.of(desiredCurrencyCode);
    LocalDateDoubleTimeSeries fxSeries = convertSeries(originalPnlSeries, conversionTS, originalCurrency, desiredCurrency);
    return ImmutableSet.of(new ComputedValue(getValueSpec(originalPnlSeriesComputedValue.getSpecification(), desiredCurrencyCode), fxSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return getCurrencyMatrix() != null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    Set<String> desiredCurrencyValues = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencyValues == null || desiredCurrencyValues.size() != 1) {
      return null;
    }
    ValueProperties constraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY).withAny(ValuePropertyNames.CURRENCY)
        .with(CONVERSION_CCY_PROPERTY, Iterables.getOnlyElement(desiredCurrencyValues))
        .withOptional(CONVERSION_CCY_PROPERTY).get();
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), constraints));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    ValueRequirement inputRequirement = Iterables.getOnlyElement(inputs.values());
    ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    return ImmutableSet.of(getValueSpec(inputSpec, inputRequirement.getConstraint(CONVERSION_CCY_PROPERTY)));
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    final ValueSpecification input = inputs.iterator().next(); // only one input, so must be the P&L Series
    final Currency originalCurrency = Currency.of(input.getProperty(ValuePropertyNames.CURRENCY));
    final ValueSpecification output = outputs.iterator().next(); // only one output, so must be the result P&L Series
    final Currency desiredCurrency = Currency.of(output.getProperty(ValuePropertyNames.CURRENCY));
    return getCurrencyMatrix().getConversion(originalCurrency, desiredCurrency).accept(
        new TimeSeriesCurrencyConversionRequirements(OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context)));
  }

  protected abstract ValueSpecification getValueSpec(ValueSpecification inputSpec, String currencyCode);

  private LocalDateDoubleTimeSeries convertSeries(LocalDateDoubleTimeSeries sourceTs, HistoricalTimeSeries conversionTS, Currency sourceCurrency, Currency targetCurrency) {
    CurrencyMatrixValue fxConversion = getCurrencyMatrix().getConversion(sourceCurrency, targetCurrency);
    return fxConversion.accept(new TimeSeriesCurrencyConverter(sourceTs, conversionTS));
  }

  //-------------------------------------------------------------------------
  private class TimeSeriesCurrencyConverter implements CurrencyMatrixValueVisitor<LocalDateDoubleTimeSeries> {

    private final LocalDateDoubleTimeSeries _baseTs;
    private final HistoricalTimeSeries _conversionTS;

    public TimeSeriesCurrencyConverter(LocalDateDoubleTimeSeries baseTs, HistoricalTimeSeries conversionTS) {
      _baseTs = baseTs;
      _conversionTS = conversionTS;
    }

    public LocalDateDoubleTimeSeries getBaseTimeSeries() {
      return _baseTs;
    }

    public HistoricalTimeSeries getConversionTimeSeries() {
      return _conversionTS;
    }

    @Override
    public LocalDateDoubleTimeSeries visitFixed(CurrencyMatrixFixed fixedValue) {
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(fixedValue.getFixedValue());
    }

    @Override
    public LocalDateDoubleTimeSeries visitValueRequirement(CurrencyMatrixValueRequirement uniqueId) {
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(getConversionTimeSeries().getTimeSeries());
    }

    @Override
    public LocalDateDoubleTimeSeries visitCross(CurrencyMatrixCross cross) {
      // TODO
      throw new UnsupportedOperationException();
    }

  }

  private class TimeSeriesCurrencyConversionRequirements implements CurrencyMatrixValueVisitor<Set<ValueRequirement>> {

    private final HistoricalTimeSeriesResolver _timeSeriesResolver;

    public TimeSeriesCurrencyConversionRequirements(HistoricalTimeSeriesResolver timeSeriesResolver) {
      _timeSeriesResolver = timeSeriesResolver;
    }

    public HistoricalTimeSeriesResolver getTimeSeriesResolver() {
      return _timeSeriesResolver;
    }

    @Override
    public Set<ValueRequirement> visitFixed(CurrencyMatrixFixed fixedValue) {
      return Collections.emptySet();
    }

    @Override
    public Set<ValueRequirement> visitValueRequirement(CurrencyMatrixValueRequirement uniqueId) {
      ValueRequirement requirement = uniqueId.getValueRequirement();
      ExternalId targetId = requirement.getTargetSpecification().getIdentifier();
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(targetId.toBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      // TODO: this is not great, but we don't know the range of the underlying time series when the graph is built
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(),
          DateConstraint.EARLIEST_START, true, DateConstraint.VALUATION_TIME, true));
    }

    @Override
    public Set<ValueRequirement> visitCross(CurrencyMatrixCross cross) {
      // TODO
      throw new UnsupportedOperationException();
    }

  }

}
