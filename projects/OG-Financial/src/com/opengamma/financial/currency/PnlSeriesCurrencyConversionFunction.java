/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixed;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

// TODO jonathan 2012-02-01 -- added this for immediate demo needs. Need to revisit and fully implement.

/**
 * 
 */
public class PnlSeriesCurrencyConversionFunction extends AbstractFunction.NonCompiledInvoker {

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
    ComputedValue originalPnlSeriesComputedValue = inputs.getComputedValue(ValueRequirementNames.PNL_SERIES);
    LocalDateDoubleTimeSeries originalPnlSeries = (LocalDateDoubleTimeSeries) originalPnlSeriesComputedValue.getValue();
    Currency originalCurrency = Currency.of(originalPnlSeriesComputedValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY));
    ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    String desiredCurrencyCode = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    Currency desiredCurrency = Currency.of(desiredCurrencyCode);
    HistoricalTimeSeriesSource htsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    
    LocalDateDoubleTimeSeries fxSeries = convertSeries(originalPnlSeries, htsSource, originalCurrency, desiredCurrency);
    return ImmutableSet.of(new ComputedValue(getValueSpec(originalPnlSeriesComputedValue.getSpecification(), desiredCurrencyCode), fxSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;  
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (getCurrencyMatrix() == null) {
      return false;
    }
    return target.getType() == getTargetType();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION).get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
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
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.PNL_SERIES, desiredValue.getTargetSpecification(), constraints));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    ValueRequirement inputRequirement = Iterables.getOnlyElement(inputs.values());
    ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    return ImmutableSet.of(getValueSpec(inputSpec, inputRequirement.getConstraint(CONVERSION_CCY_PROPERTY)));
  }

  private ValueSpecification getValueSpec(ValueSpecification inputSpec, String currencyCode) {
    ValueProperties properties = inputSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId())
        .withoutAny(ValuePropertyNames.CURRENCY).with(ValuePropertyNames.CURRENCY, currencyCode).get();
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, inputSpec.getTargetSpecification(), properties);
  }
  
  private LocalDateDoubleTimeSeries convertSeries(LocalDateDoubleTimeSeries sourceTs, HistoricalTimeSeriesSource htsSource, Currency sourceCurrency, Currency targetCurrency) {
    CurrencyMatrixValue fxConversion = getCurrencyMatrix().getConversion(sourceCurrency, targetCurrency);
    return fxConversion.accept(new TimeSeriesCurrencyConverter(sourceTs, htsSource));
  }
  
  //-------------------------------------------------------------------------
  private class TimeSeriesCurrencyConverter implements CurrencyMatrixValueVisitor<LocalDateDoubleTimeSeries> {

    private final LocalDateDoubleTimeSeries _baseTs;
    private final HistoricalTimeSeriesSource _htsSource;
    
    public TimeSeriesCurrencyConverter(LocalDateDoubleTimeSeries baseTs, HistoricalTimeSeriesSource htsSource) {
      _baseTs = baseTs;
      _htsSource = htsSource;
    }
    
    public LocalDateDoubleTimeSeries getBaseTimeSeries() {
      return _baseTs;
    }
    
    public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
      return _htsSource;
    }
    
    @Override
    public LocalDateDoubleTimeSeries visitFixed(CurrencyMatrixFixed fixedValue) {
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(fixedValue.getFixedValue());
    }

    @Override
    public LocalDateDoubleTimeSeries visitValueRequirement(CurrencyMatrixValueRequirement uniqueId) {
      ValueRequirement requirement = uniqueId.getValueRequirement();
      ExternalId targetId = requirement.getTargetSpecification().getIdentifier();
      HistoricalTimeSeries fxSeries = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
          MarketDataRequirementNames.MARKET_VALUE, ExternalIdBundle.of(targetId), null, getBaseTimeSeries().getEarliestTime(), true, getBaseTimeSeries().getLatestTime(), true);
      if (fxSeries == null) {
        throw new OpenGammaRuntimeException("Could not find FX time-series for " + targetId);
      }
      return (LocalDateDoubleTimeSeries) getBaseTimeSeries().multiply(fxSeries.getTimeSeries());
    }

    @Override
    public LocalDateDoubleTimeSeries visitCross(CurrencyMatrixCross cross) {
      // TODO
      throw new UnsupportedOperationException();
    }
    
  }
  
}
