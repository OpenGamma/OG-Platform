/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import static com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.forwardcurve.ConfigDBForwardSwapCurveDefinitionSource;
import com.opengamma.financial.analytics.forwardcurve.ConfigDBForwardSwapCurveSpecificationSource;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveDefinition;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveInstrumentProvider;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveSpecification;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ForwardSwapCurveFromMarketQuotesFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForwardSwapCurveFromMarketQuotesFunction.class);

  private ConfigDBForwardSwapCurveDefinitionSource _forwardSwapCurveDefinitionSource;
  private ConfigDBForwardSwapCurveSpecificationSource _forwardSwapCurveSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _forwardSwapCurveDefinitionSource = ConfigDBForwardSwapCurveDefinitionSource.init(context, this);
    _forwardSwapCurveSpecificationSource = ConfigDBForwardSwapCurveSpecificationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(PROPERTY_FORWARD_CURVE_INTERPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR).withAny(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).withAny(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR)
            .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardSwapCurveMarketDataFunction.FORWARD_SWAP_QUOTES).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          s_logger.error("Did not supply a single curve name; asked for {}", curveNames);
          return null;
        }
        final Set<String> forwardTenors = constraints.getValues(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR);
        if (forwardTenors == null || forwardTenors.size() != 1) {
          s_logger.error("Did not supply a single forward tenor; asked for {}", forwardTenors);
          return null;
        }
        final Set<String> forwardCurveInterpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
        if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
          return null;
        }
        final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
        if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
          return null;
        }
        final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
        if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
          return null;
        }
        final String curveName = curveNames.iterator().next();
        final String forwardTenor = forwardTenors.iterator().next();
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).with(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR, forwardTenor)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.FORWARD_SWAP_CURVE_MARKET_DATA, target.toSpecification(), properties));
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
        final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final DoubleArrayList expiries = new DoubleArrayList();
        final DoubleArrayList forwards = new DoubleArrayList();
        final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
        final ForwardSwapCurveDefinition definition = _forwardSwapCurveDefinitionSource.getDefinition(curveName, currency.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find a forward swap curve definition called " + curveName + " for target " + target);
        }
        final ForwardSwapCurveSpecification specification = _forwardSwapCurveSpecificationSource.getSpecification(curveName, currency.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find a forward swap curve specification called " + curveName + " for target " + target);
        }
        final ForwardSwapCurveInstrumentProvider provider = (ForwardSwapCurveInstrumentProvider) specification.getCurveInstrumentProvider();
        final Object dataObject = inputs.getValue(ValueRequirementNames.FORWARD_SWAP_CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get market data");
        }
        @SuppressWarnings("unchecked")
        final Map<ExternalId, Double> data = (Map<ExternalId, Double>) dataObject;
        final String interpolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
        final String leftExtrapolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
        final String rightExtrapolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
        final String forwardTenorName = desiredValue.getConstraint(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR);
        final String conventionName = currency.getCode() + "_SWAP";
        final ConventionBundle convention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
        if (convention == null) {
          throw new OpenGammaRuntimeException("Could not get convention named " + conventionName);
        }
        final DayCount dayCount = convention.getSwapFloatingLegDayCount();
        if (dayCount == null) {
          throw new OpenGammaRuntimeException("Could not get daycount");
        }
        final Integer settlementDays = convention.getSwapFloatingLegSettlementDays();
        if (settlementDays == null) {
          throw new OpenGammaRuntimeException("Could not get number of settlement days");
        }
        final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, currency);
        final LocalDate localNow = now.toLocalDate();
        final Period forwardPeriod = Period.parse(forwardTenorName);
        final Tenor forwardTenor = Tenor.of(forwardPeriod);
        final LocalDate forwardStart = ScheduleCalculator.getAdjustedDate(localNow.plus(forwardPeriod), settlementDays, calendar); //TODO check adjustments
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(localNow, tenor, forwardTenor);
          if (data.containsKey(identifier)) {
            final LocalDate expiry = ScheduleCalculator.getAdjustedDate(forwardStart.plus(tenor.getPeriod()), settlementDays, calendar);
            expiries.add(dayCount.getDayCountFraction(localNow, expiry));
            forwards.add(data.get(identifier));
          }
        }
        if (expiries.size() == 0) {
          throw new OpenGammaRuntimeException("Could not get any values for forward swaps");
        }
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final ForwardCurve curve = new ForwardCurve(InterpolatedDoublesCurve.from(expiries, forwards, interpolator));
        return Collections.singleton(new ComputedValue(getResultSpec(target, curveName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName, forwardTenorName), curve));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName, final String interpolatorName, final String leftExtrapolatorName,
          final String rightExtrapolatorName, final String forwardTenor) {
        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, interpolatorName)
            .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, leftExtrapolatorName).with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, rightExtrapolatorName)
            .with(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR, forwardTenor)
            .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardSwapCurveMarketDataFunction.FORWARD_SWAP_QUOTES).get();
        return new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
      }
    };
  }
}
