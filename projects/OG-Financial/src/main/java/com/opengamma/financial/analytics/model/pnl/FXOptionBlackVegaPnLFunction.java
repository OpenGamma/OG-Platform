/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXOptionBlackVegaPnLFunction extends AbstractFunction {

  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  private ConfigDBVolatilitySurfaceDefinitionSource _volatilitySurfaceDefinitionSource;
  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceDefinitionSource = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this);
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

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
      return security instanceof FXOptionSecurity || security instanceof NonDeliverableFXOptionSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
      final String currencyBase = currencyPair.getBase().getCode(); // The base currency
      final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
          .withAny(FXOptionBlackFunction.PUT_CURVE).withAny(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG).withAny(FXOptionBlackFunction.CALL_CURVE)
          .withAny(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG).withAny(ValuePropertyNames.SURFACE).withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
          .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME).with(ValuePropertyNames.CURRENCY, currencyBase)
          .withAny(ValuePropertyNames.SAMPLING_PERIOD).withAny(ValuePropertyNames.SCHEDULE_CALCULATOR).withAny(ValuePropertyNames.SAMPLING_FUNCTION)
          .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.VEGA_QUOTE_MATRIX).get();
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> putCurveNames = constraints.getValues(FXOptionBlackFunction.PUT_CURVE);
      if (putCurveNames == null || putCurveNames.size() != 1) {
        return null;
      }
      final Set<String> putCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
      if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> callCurveNames = constraints.getValues(FXOptionBlackFunction.CALL_CURVE);
      if (callCurveNames == null || callCurveNames.size() != 1) {
        return null;
      }
      final Set<String> callCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
      if (callCurveCalculationConfigs == null || callCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
      if (surfaceNames == null || surfaceNames.size() != 1) {
        return null;
      }
      final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      if (interpolatorNames == null || interpolatorNames.size() != 1) {
        return null;
      }
      final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
        return null;
      }
      final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
      if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
        return null;
      }
      final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
      if (samplingPeriods == null || samplingPeriods.size() != 1) {
        return null;
      }
      final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final UnorderedCurrencyPair currencies = UnorderedCurrencyPair.of(putCurrency, callCurrency);
      final String surfaceName = Iterables.getOnlyElement(surfaceNames);
      final String samplingPeriod = Iterables.getOnlyElement(samplingPeriods);
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
      final String vegaResultCurrency = getResultCurrency(target, currencyPair);
      final String currencyBase = currencyPair.getBase().getCode(); // The base currency
      final ValueRequirement vegaMatrixRequirement = new ValueRequirement(ValueRequirementNames.VEGA_QUOTE_MATRIX, ComputationTargetSpecification.of(security), ValueProperties.builder()
          .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).with(FXOptionBlackFunction.PUT_CURVE, Iterables.getOnlyElement(putCurveNames))
          .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, Iterables.getOnlyElement(putCurveCalculationConfigs))
          .with(FXOptionBlackFunction.CALL_CURVE, Iterables.getOnlyElement(callCurveNames))
          .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, Iterables.getOnlyElement(callCurveCalculationConfigs)).with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, Iterables.getOnlyElement(interpolatorNames))
          .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, Iterables.getOnlyElement(leftExtrapolatorNames))
          .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, Iterables.getOnlyElement(rightExtrapolatorNames)).with(ValuePropertyNames.CURRENCY, vegaResultCurrency).get());
      final ValueRequirement surfaceHTSRequirement = getVolatilitySurfaceHTSRequirement(currencies, surfaceName, samplingPeriod);
      final Set<ValueRequirement> requirements = new HashSet<>();
      requirements.add(vegaMatrixRequirement);
      requirements.add(surfaceHTSRequirement);
      if (!currencyBase.equals(vegaResultCurrency)) {
        requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(putCurrency, callCurrency));
      }
      return requirements;
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
        throws AsynchronousExecution {
      final Object vegaMatrixObject = inputs.getValue(ValueRequirementNames.VEGA_QUOTE_MATRIX);
      if (vegaMatrixObject == null) {
        throw new OpenGammaRuntimeException("Could not get vega matrix");
      }
      final Object volatilityHTSObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES);
      if (volatilityHTSObject == null) {
        throw new OpenGammaRuntimeException("Could not get historical time series for volatilities");
      }
      final DoubleLabelledMatrix2D vegaMatrix = (DoubleLabelledMatrix2D) vegaMatrixObject;
      final HistoricalTimeSeriesBundle timeSeriesBundle = (HistoricalTimeSeriesBundle) volatilityHTSObject;
      final Clock snapshotClock = executionContext.getValuationClock();
      final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
      final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
      final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
      final Position position = target.getPosition();
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
      final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(currencyPair, surfaceName);
      final VolatilitySurfaceSpecification specification = getSurfaceSpecification(currencyPair, surfaceName);
      final Period samplingPeriod = getSamplingPeriod(desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD));
      final LocalDate startDate = now.minus(samplingPeriod);
      final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
      final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
      final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
      final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
      final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
      DoubleTimeSeries<?> vegaPnL = getPnLSeries(definition, specification, timeSeriesBundle, vegaMatrix, now, schedule, samplingFunction);
      vegaPnL = vegaPnL.multiply(position.getQuantity().doubleValue());
      final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      final CurrencyPair baseCounterPair = currencyPairs.getCurrencyPair(putCurrency, callCurrency);
      final String vegaResultCurrency = getResultCurrency(target, baseCounterPair);
      final String currencyBase = baseCounterPair.getBase().getCode();
      if (!currencyBase.equals(vegaResultCurrency)) {
        final Object spotFXObject = inputs.getValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
        if (spotFXObject == null) {
          throw new OpenGammaRuntimeException("Could not get spot FX time series");
        }
        final DoubleTimeSeries<?> spotFX = ((HistoricalTimeSeries) spotFXObject).getTimeSeries();
        vegaPnL = vegaPnL.multiply(spotFX);
      }
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(spec, vegaPnL));
    }

  }

  private ValueRequirement getVolatilitySurfaceHTSRequirement(final UnorderedCurrencyPair currencies, final String surfaceName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createVolatilitySurfaceHTSRequirement(currencies, surfaceName, InstrumentTypeProperties.FOREX, MarketDataRequirementNames.MARKET_VALUE, null,
        DateConstraint.VALUATION_TIME.minus(samplingPeriod), true, DateConstraint.VALUATION_TIME, true);
  }

  private VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final UnorderedCurrencyPair currencyPair, final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + currencyPair.getUniqueId().getValue();
    final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName,
        InstrumentTypeProperties.FOREX);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + InstrumentTypeProperties.FOREX);
    }
    return definition;
  }

  private VolatilitySurfaceSpecification getSurfaceSpecification(final UnorderedCurrencyPair currencyPair, final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + currencyPair.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.FOREX);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
    }
    return specification;
  }

  private Period getSamplingPeriod(final String samplingPeriodName) {
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  private DoubleTimeSeries<?> getPnLSeries(final VolatilitySurfaceDefinition<Object, Object> definition, final VolatilitySurfaceSpecification specification,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final DoubleLabelledMatrix2D vegaMatrix, final LocalDate endDate, final LocalDate[] schedule,
      final TimeSeriesSamplingFunction samplingFunction) {
    final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
    final double[][] vegas = vegaMatrix.getValues();
    if (vegas.length != definition.getYs().length || vegas[0].length != definition.getXs().length) {
      throw new OpenGammaRuntimeException("Vega matrix not the same shape as that in definition");
    }
    DoubleTimeSeries<?> vegaPnL = null;
    int i = 0;
    for (final Object x : definition.getXs()) {
      int j = 0;
      for (final Object y : definition.getYs()) {
        ExternalId id = provider.getInstrument(x, y, endDate);
        if (id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
          id = ExternalSchemes.bloombergTickerSecurityId(id.getValue());
        }
        final ExternalIdBundle identifier = ExternalIdBundle.of(id);
        final HistoricalTimeSeries tsForTicker = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, identifier);
        if (tsForTicker == null) {
          throw new OpenGammaRuntimeException("Could not get identifier / vol series for " + id);
        }
        final DoubleTimeSeries<?> volHistory = DIFFERENCE.evaluate(samplingFunction.getSampledTimeSeries(tsForTicker.getTimeSeries(), schedule));
        final double vega = vegas[j][i] / 100;
        if (vegaPnL == null) {
          vegaPnL = volHistory.multiply(vega);
        } else {
          vegaPnL = vegaPnL.add(volHistory.multiply(vega));
        }
        j++;
      }
      i++;
    }
    return vegaPnL;
  }

  private String getResultCurrency(final ComputationTarget target, final CurrencyPair currencyPair) {
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    Currency ccy;
    if (putCurrency.equals(currencyPair.getBase())) {
      ccy = callCurrency;
    } else {
      ccy = putCurrency;
    }
    return ccy.getCode();
  }
}
