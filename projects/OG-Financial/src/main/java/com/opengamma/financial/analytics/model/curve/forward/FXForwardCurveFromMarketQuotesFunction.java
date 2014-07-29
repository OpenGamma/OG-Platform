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
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class FXForwardCurveFromMarketQuotesFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurveFromMarketQuotesFunction.class);
  /** Name of the calculation method */
  public static final String FX_FORWARD_QUOTES = "FXForwardQuotes";

  private ConfigDBFXForwardCurveSpecificationSource _fxForwardCurveSpecificationSource;
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _fxForwardCurveSpecificationSource = ConfigDBFXForwardCurveSpecificationSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(PROPERTY_FORWARD_CURVE_INTERPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR).withAny(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
            .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, FX_FORWARD_QUOTES).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
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
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, target.toSpecification(), properties));
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final DoubleArrayList expiries = new DoubleArrayList();
        final DoubleArrayList forwards = new DoubleArrayList();
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(target.getUniqueId());
        final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + curveName + " for target " + target);
        }
        final FXForwardCurveSpecification specification = _fxForwardCurveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " for target " + target);
        }
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        final Object dataObject = inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get market data");
        }
        @SuppressWarnings("unchecked")
        final Map<ExternalId, Double> data = (Map<ExternalId, Double>) dataObject;
        final String interpolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
        final String leftExtrapolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
        final String rightExtrapolatorName = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
          if (data.containsKey(identifier)) {
            expiries.add(TimeCalculator.getTimeBetween(now, now.plus(tenor.getPeriod())));
            forwards.add(data.get(identifier));
          }
        }
        if (expiries.size() == 0) {
          throw new OpenGammaRuntimeException("Could not get any values for FX forwards");
        }
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final ForwardCurve curve = new ForwardCurve(InterpolatedDoublesCurve.from(expiries, forwards, interpolator));
        return Collections.singleton(new ComputedValue(getResultSpec(target, curveName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName), curve));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName, final String interpolatorName, final String leftExtrapolatorName,
          final String rightExtrapolatorName) {
        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, interpolatorName)
            .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, leftExtrapolatorName).with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, rightExtrapolatorName)
            .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, FX_FORWARD_QUOTES).get();
        return new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
      }
    };
  }
}
