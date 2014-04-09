/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.horizon.EqyTrsConstantSpreadHorizonCalculator;
import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the value theta of an equity total return swap security.
 */
public class EquityTotalReturnSwapConstantSpreadThetaFunction extends EquityTotalReturnSwapFunction {
  /** The calculator */
  private static final HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> CALCULATOR =
      EqyTrsConstantSpreadHorizonCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_THETA}.
   */
  public EquityTotalReturnSwapConstantSpreadThetaFunction() {
    super(VALUE_THETA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new EquityTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints();
        final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
        final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
        final Trade trade = target.getTrade();
        final EquityTotalReturnSwapSecurity security = (EquityTotalReturnSwapSecurity) trade.getSecurity();
        final MulticurveProviderInterface curves = (MulticurveProviderInterface) inputs.getValue(CURVE_BUNDLE);
        final EquityTotalReturnSwapDefinition definition = (EquityTotalReturnSwapDefinition) getTargetToDefinitionConverter(context).convert(trade);
        final int daysForward = Integer.parseInt(desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD));
        final ZonedDateTimeDoubleTimeSeries fixingSeries = TotalReturnSwapUtils.getIndexTimeSeries(security.getFundingLeg(), security.getEffectiveDate(), now, timeSeries);
        final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
        final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        final Set<ExternalId> fixingDateCalendars = security.getFundingLeg().getFixingDateCalendars();
        if (fixingDateCalendars.size() != 1) {
          throw new OpenGammaRuntimeException("Cannot handle more than one fixing date calendar");
        }
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, Iterables.getOnlyElement(fixingDateCalendars));
        final MultipleCurrencyAmount theta = CALCULATOR.getTheta(definition, now, curves, daysForward, calendar, fixingSeries);
        if (theta.size() != 1) {
          throw new OpenGammaRuntimeException("Got result with more than one currency for theta: " + theta);
        }
        final ValueSpecification spec = new ValueSpecification(VALUE_THETA, target.toSpecification(), properties);
        final Currency currency = FinancialSecurityUtils.getCurrency(security);
        return Collections.singleton(new ComputedValue(spec, theta.getAmount(currency)));
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        throw new IllegalStateException("Should never reach this code");
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> daysForward = constraints.getValues(PROPERTY_DAYS_TO_MOVE_FORWARD);
        if (daysForward == null || daysForward.size() != 1) {
          return null;
        }
        return super.getRequirements(compilationContext, target, desiredValue);
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
        final Collection<ValueProperties.Builder> properties = super.getResultProperties(compilationContext, target);
        final Collection<ValueProperties.Builder> result = new HashSet<>();
        for (final ValueProperties.Builder builder : properties) {
          result.add(builder
              .withAny(PROPERTY_DAYS_TO_MOVE_FORWARD)
              .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
              .with(CURRENCY, currency));
        }
        return result;
      }

    };

  }

}
