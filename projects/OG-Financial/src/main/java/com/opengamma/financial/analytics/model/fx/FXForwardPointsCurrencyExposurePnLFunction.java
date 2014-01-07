/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_FUNCTION;
import static com.opengamma.engine.value.ValuePropertyNames.SCHEDULE_CALCULATOR;
import static com.opengamma.engine.value.ValuePropertyNames.TRANSFORMATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.PNL_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.RETURN_SERIES;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.FORWARD_POINTS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
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
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.money.UnorderedCurrencyPair;


/**
 *
 */
public class FXForwardPointsCurrencyExposurePnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Position position = target.getPosition();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
    final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(FX_CURRENCY_EXPOSURE);
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    final CurrencyPairs currencyPairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
    final double exposure = mca.getAmount(currencyNonBase);
    final LocalDateDoubleTimeSeries fxSpotReturnSeries = (LocalDateDoubleTimeSeries) inputs.getValue(RETURN_SERIES);
    final LocalDateDoubleTimeSeries pnlSeries = fxSpotReturnSeries.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(spec, pnlSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity ||
        security instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
          .withAny(START_DATE_PROPERTY)
          .withAny(END_DATE_PROPERTY)
          .withAny(INCLUDE_START_PROPERTY)
          .withAny(INCLUDE_END_PROPERTY)
          .withAny(TRANSFORMATION_METHOD)
          .withAny(SCHEDULE_CALCULATOR)
          .withAny(SAMPLING_FUNCTION)
          .withAny(CURVE_EXPOSURES)
          .withAny(FORWARD_CURVE_NAME)
          .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
          .with(PROPERTY_PNL_CONTRIBUTIONS, FX_CURRENCY_EXPOSURE)
          .withAny(CURRENCY)
          .get();
    return Collections.singleton(new ValueSpecification(PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
    if (curveExposureConfigs == null || curveExposureConfigs.size() != 1) {
      return null;
    }
    final Set<String> fxForwardCurveNames = constraints.getValues(FORWARD_CURVE_NAME);
    if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> includeStarts = constraints.getValues(INCLUDE_START_PROPERTY);
    if (includeStarts != null && includeStarts.size() != 1) {
      return null;
    }
    final Set<String> includeEnds = constraints.getValues(INCLUDE_END_PROPERTY);
    if (includeEnds != null && includeEnds.size() != 1) {
      return null;
    }
    final Set<String> startDates = constraints.getValues(START_DATE_PROPERTY);
    if (startDates != null && startDates.size() != 1) {
      return null;
    }
    final Set<String> endDates = constraints.getValues(END_DATE_PROPERTY);
    if (endDates != null && endDates.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctions = constraints.getValues(SAMPLING_FUNCTION);
    if (samplingFunctions == null || samplingFunctions.size() != 1) {
      return null;
    }
    final Set<String> scheduleMethods = constraints.getValues(SCHEDULE_CALCULATOR);
    if (scheduleMethods == null || scheduleMethods.size() != 1) {
      return null;
    }
    final Set<String> transformationMethods = constraints.getValues(TRANSFORMATION_METHOD);
    if (transformationMethods == null || transformationMethods.size() != 1) {
      return null;
    }
    final String curveExposureConfig = Iterables.getOnlyElement(curveExposureConfigs);
    final String fxForwardCurveName = Iterables.getOnlyElement(fxForwardCurveNames);
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_EXPOSURES, curveExposureConfig)
        .with(FORWARD_CURVE_NAME, fxForwardCurveName)
        .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
        .get();
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ComputationTargetSpecification fxSpotReturnSeriesSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties returnSeriesProperties = ValueProperties.builder()
        .with(SCHEDULE_CALCULATOR, scheduleMethods)
        .with(SAMPLING_FUNCTION, samplingFunctions)
        .with(INCLUDE_START_PROPERTY, includeStarts)
        .with(INCLUDE_END_PROPERTY, includeEnds)
        .with(START_DATE_PROPERTY, startDates)
        .with(END_DATE_PROPERTY, endDates)
        .with(TRANSFORMATION_METHOD, transformationMethods)
        .get();
    requirements.add(new ValueRequirement(RETURN_SERIES, fxSpotReturnSeriesSpec, returnSeriesProperties));
    final Trade trade = Iterables.getOnlyElement(target.getPosition().getTrades());
    requirements.add(new ValueRequirement(FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(trade), properties));
    requirements.add(new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, ValueProperties.none()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String includeStart = null;
    String includeEnd = null;
    String startDate = null;
    String endDate = null;
    String transformationMethod = null;
    String scheduleCalculator = null;
    String samplingFunction = null;
    String curveExposures = null;
    String forwardCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final String valueName = entry.getValue().getValueName();
      final ValueRequirement requirement = entry.getValue();
      if (valueName.equals(RETURN_SERIES)) {
        includeStart = requirement.getConstraint(INCLUDE_START_PROPERTY);
        includeEnd = requirement.getConstraint(INCLUDE_END_PROPERTY);
        startDate = requirement.getConstraint(START_DATE_PROPERTY);
        endDate = requirement.getConstraint(END_DATE_PROPERTY);
        transformationMethod = requirement.getConstraint(TRANSFORMATION_METHOD);
        scheduleCalculator = requirement.getConstraint(SCHEDULE_CALCULATOR);
        samplingFunction = requirement.getConstraint(SAMPLING_FUNCTION);
      } else if (valueName.equals(FX_CURRENCY_EXPOSURE)) {
        curveExposures = requirement.getConstraint(CURVE_EXPOSURES);
        forwardCurveName = requirement.getConstraint(FORWARD_CURVE_NAME);
      }
    }
    if (includeStart == null || curveExposures == null) {
      return null;
    }
    //TODO how should this be done?
    final CurrencyPairs pairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyPair currencyPair = pairs.getCurrencyPair(payCurrency, receiveCurrency);
    final Currency currencyBase = currencyPair.getBase();
    final ValueProperties properties = createValueProperties()
        .with(INCLUDE_START_PROPERTY, includeStart)
        .with(INCLUDE_END_PROPERTY, includeEnd)
        .with(START_DATE_PROPERTY, startDate)
        .with(END_DATE_PROPERTY, endDate)
        .with(TRANSFORMATION_METHOD, transformationMethod)
        .with(SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(SAMPLING_FUNCTION, samplingFunction)
        .with(CURVE_EXPOSURES, curveExposures)
        .with(FORWARD_CURVE_NAME, forwardCurveName)
        .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
        .with(PROPERTY_PNL_CONTRIBUTIONS, FX_CURRENCY_EXPOSURE)
        .with(CURRENCY, currencyBase.getCode())
        .get();
    return Collections.singleton(new ValueSpecification(PNL_SERIES, target.toSpecification(), properties));
  }

}
