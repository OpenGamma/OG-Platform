/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripIdentifierAndMaturityBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Function to return a @{code ISDACompliantYieldCurve}
 */
public class ISDACompliantYieldCurveFunction extends AbstractFunction {
  private static final BusinessDayConvention badDayConv = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter();
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount DCC_30_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final DayCount MONEY_MARKET_DCC = ACT_360;
  private static final DayCount SWAP_DCC = DCC_30_360;
  private static final DayCount CURVE_DCC = ACT_365;


  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(compilationContext);
    final InterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
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
        final int offset = Integer.parseInt(offsetString);  //TODO: Is this still used???
        final Object definitionObject = inputs.getValue(ValueRequirementNames.TARGET);
        if (definitionObject == null) {
          throw new OpenGammaRuntimeException("Couldn't get interpolated yield curve specification: " + curveName);
        }
        final YieldCurveDefinition curveDefinition = (YieldCurveDefinition) definitionObject;
        final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Couldn't get yield curve data for " + curveName);
        }
        final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
        final InterpolatedYieldCurveSpecification specification = getCurveSpecification(curveDefinition, spotDate);
        final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities = getCurveWithSecurities(
            specification,
            executionContext,
            marketData);
        final ISDAInstrumentTypes[] instruments = new ISDAInstrumentTypes[specificationWithSecurities.getStrips().size()];
        final Period[] tenors = new Period[specificationWithSecurities.getStrips().size()];
        final double[] values = new double[specificationWithSecurities.getStrips().size()];

        Period swapIvl = null;
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
          } else if (SwapSecurity.SECURITY_TYPE.equals(strip.getSecurity().getSecurityType())) {
            instruments[i] = ISDAInstrumentTypes.Swap;
            swapIvl = getFixedLegPaymentTenor((SwapSecurity) strip.getSecurity());
          } else {
            throw new OpenGammaRuntimeException("Unexpected curve instument type, can only handle cash and swaps, got: " + strip.getSecurity());
          }
          tenors[i] = strip.getTenor().getPeriod();
          values[i] = rate;
          i++;
        }

        final ISDACompliantYieldCurve yieldCurve = ISDACompliantYieldCurveBuild.build(valuationDate.toLocalDate(), spotDate, instruments, tenors, values, MONEY_MARKET_DCC, SWAP_DCC, swapIvl, CURVE_DCC, badDayConv);

        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
            .with(ISDAFunctionConstants.ISDA_CURVE_DATE, spotDateString)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
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
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
            .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .get();
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
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();

        // look up yield curve specification - dont rely on YieldCurveSpecificationFunction as that may have been compiled before the yield curve was created
        // this is a slight performance hit over the standard curve spec handling but shouldn't be an issue
        //TODO: should use versionOf rather than latest but we dont access to the valuation clock here
        final YieldCurveDefinition curveDefinition = configSource.getLatestByName(YieldCurveDefinition.class, curveName);
        if (curveDefinition == null) {
          return null;
        }

        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(2);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.TARGET, ComputationTargetType.of(YieldCurveDefinition.class), curveDefinition.getUniqueId()));
        return requirements;
      }

      private InterpolatedYieldCurveSpecificationWithSecurities getCurveWithSecurities(final InterpolatedYieldCurveSpecification curveSpec, final FunctionExecutionContext executionContext,
                                                                                       final SnapshotDataBundle marketData) {
        //TODO: Move this to a seperate function
        final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
                                                                                                                      OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource(), OpenGammaExecutionContext.getHolidaySource(executionContext));
        final InterpolatedYieldCurveSpecificationWithSecurities curveSpecificationWithSecurities = builder.resolveToSecurity(curveSpec, marketData);
        return curveSpecificationWithSecurities;
      }

      private InterpolatedYieldCurveSpecification getCurveSpecification(final YieldCurveDefinition curveDefinition, final LocalDate curveDate) {
        final InterpolatedYieldCurveSpecification curveSpec = curveSpecBuilder.buildCurve(curveDate, curveDefinition);
        return curveSpec;
      }

    };
  }

}
