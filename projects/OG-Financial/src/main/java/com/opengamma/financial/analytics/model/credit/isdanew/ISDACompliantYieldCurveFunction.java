/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Function to return a @{code ISDACompliantYieldCurve}
 */
public class ISDACompliantYieldCurveFunction extends AbstractFunction {
  private static final BusinessDayConvention badDayConv = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter();
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount DCC_30_360 = DayCounts.THIRTY_U_360;
  private static final DayCount CURVE_DCC = ACT_365;

  private InterpolatedYieldCurveSpecificationBuilder.AtVersionCorrection _interpolatedYieldCurveSpecificationBuilder;
  private ConfigSourceQuery<YieldCurveDefinition> _yieldCurveDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext compilationContext) {
    _interpolatedYieldCurveSpecificationBuilder = InterpolatedYieldCurveSpecificationBuilder.AtVersionCorrection.init(compilationContext, this);
    _yieldCurveDefinitionSource = ConfigSourceQuery.init(compilationContext, this, YieldCurveDefinition.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ZonedDateTime valuationDate = ZonedDateTime.now(executionContext.getValuationClock());
        final HistoricalTimeSeriesBundle timeSeries = (HistoricalTimeSeriesBundle) inputs.getValue(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String spotDateString = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_DATE);
        final LocalDate spotDate = LocalDate.parse(spotDateString, dateFormatter);
        final String offsetString = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
        final int offset = Integer.parseInt(offsetString); //TODO: Is this still used???
        final Object definitionObject = inputs.getValue(ValueRequirementNames.TARGET);
        if (definitionObject == null) {
          throw new OpenGammaRuntimeException("Couldn't get interpolated yield curve specification: " + curveName);
        }
        final YieldCurveDefinition curveDefinition = (YieldCurveDefinition) definitionObject;
        final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Couldn't get yield curve data for " + curveName);
        }
        final YieldCurveData marketData = (YieldCurveData) dataObject;
        final InterpolatedYieldCurveSpecification specification = getCurveSpecification(curveDefinition, spotDate);
        final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities = marketData.getCurveSpecification();
        final ISDAInstrumentTypes[] instruments = new ISDAInstrumentTypes[specificationWithSecurities.getStrips().size()];
        final Period[] tenors = new Period[specificationWithSecurities.getStrips().size()];
        final double[] values = new double[specificationWithSecurities.getStrips().size()];

        Period swapIvl = null;
        DayCount moneyDCC = null;
        DayCount swapFixLegDCC = null;
        int i = 0;
        for (final FixedIncomeStripWithSecurity strip : specificationWithSecurities.getStrips()) {
          final String securityType = strip.getSecurity().getSecurityType();
          if (!(securityType.equals(CashSecurity.SECURITY_TYPE) || securityType.equals(SwapSecurity.SECURITY_TYPE))) {
            throw new OpenGammaRuntimeException("ISDA curves should only use Libor and swap rates");
          }
          final Double rate = marketData.getDataPoint(strip.getSecurityIdentifier());
          if (rate == null) {
            throw new OpenGammaRuntimeException("Could not get rate for " + strip);
          }
          if (CashSecurity.SECURITY_TYPE.equals(strip.getSecurity().getSecurityType())) {
            instruments[i] = ISDAInstrumentTypes.MoneyMarket;
            moneyDCC = ((CashSecurity) strip.getSecurity()).getDayCount();
          } else if (SwapSecurity.SECURITY_TYPE.equals(strip.getSecurity().getSecurityType())) {
            instruments[i] = ISDAInstrumentTypes.Swap;
            swapIvl = getFixedLegPaymentTenor((SwapSecurity) strip.getSecurity());
            swapFixLegDCC = getFixedLegDCC((SwapSecurity) strip.getSecurity());
          } else {
            throw new OpenGammaRuntimeException("Unexpected curve instument type, can only handle cash and swaps, got: " + strip.getSecurity());
          }
          tenors[i] = strip.getTenor().getPeriod();
          values[i] = rate;
          i++;
        }
        final Calendar calendar = new HolidaySourceCalendarAdapter(OpenGammaExecutionContext.getHolidaySource(executionContext), curveDefinition.getCurrency());

        final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurveBuild(valuationDate.toLocalDate(), spotDate, instruments, tenors, moneyDCC, swapFixLegDCC, swapIvl, CURVE_DCC,
            badDayConv, calendar).build(values);

        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
            .with(ISDAFunctionConstants.ISDA_CURVE_DATE, spotDateString).with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, yieldCurve));
      }

      private Period getFixedLegPaymentTenor(final SwapSecurity swap) {
        if (swap.getReceiveLeg() instanceof FixedInterestRateLeg) {
          FixedInterestRateLeg fixLeg = (FixedInterestRateLeg) swap.getReceiveLeg();
          return PeriodFrequency.convertToPeriodFrequency(fixLeg.getFrequency()).getPeriod();
        } else if (swap.getPayLeg() instanceof FixedInterestRateLeg) {
          FixedInterestRateLeg fixLeg = (FixedInterestRateLeg) swap.getPayLeg();
          return PeriodFrequency.convertToPeriodFrequency(fixLeg.getFrequency()).getPeriod();
        } else {
          throw new OpenGammaRuntimeException("Got a swap without a fixed leg " + swap);
        }
      }

      private DayCount getFixedLegDCC(final SwapSecurity swap) {
        if (swap.getReceiveLeg() instanceof FixedInterestRateLeg) {
          FixedInterestRateLeg fixLeg = (FixedInterestRateLeg) swap.getReceiveLeg();
          return fixLeg.getDayCount();
        } else if (swap.getPayLeg() instanceof FixedInterestRateLeg) {
          FixedInterestRateLeg fixLeg = (FixedInterestRateLeg) swap.getPayLeg();
          return fixLeg.getDayCount();
        } else {
          throw new OpenGammaRuntimeException("Got a swap without a fixed leg " + swap);
        }
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
            .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE).with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final String curveName = Iterables.getOnlyElement(curveNames);
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();

        // look up yield curve specification - dont rely on YieldCurveSpecificationFunction as that may have been compiled before the yield curve was created
        // this is a slight performance hit over the standard curve spec handling but shouldn't be an issue
        final YieldCurveDefinition curveDefinition = _yieldCurveDefinitionSource.get(curveName);
        if (curveDefinition == null) {
          return null;
        }

        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(2);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.TARGET, ComputationTargetType.of(YieldCurveDefinition.class), curveDefinition.getUniqueId()));
        return requirements;
      }

      private InterpolatedYieldCurveSpecification getCurveSpecification(final YieldCurveDefinition curveDefinition, final LocalDate curveDate) {
        final InterpolatedYieldCurveSpecification curveSpec = _interpolatedYieldCurveSpecificationBuilder.buildCurve(curveDate, curveDefinition);
        return curveSpec;
      }

    };
  }

}
