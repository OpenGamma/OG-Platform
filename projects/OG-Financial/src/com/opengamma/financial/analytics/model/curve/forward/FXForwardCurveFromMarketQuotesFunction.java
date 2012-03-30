/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import static com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXForwardCurveFromMarketQuotesFunction extends AbstractFunction {
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private final String _definitionName;
  private final String _specificationName;
  private FXForwardCurveDefinition _definition;
  private FXForwardCurveSpecification _specification;
  private final String _curveName;

  public FXForwardCurveFromMarketQuotesFunction(final String definitionName, final String specificationName, final String curveName) {
    ArgumentChecker.notNull(definitionName, "definition name");
    ArgumentChecker.notNull(specificationName, "specification name");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _curveName = curveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBFXForwardCurveDefinitionSource curveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    _definition = curveDefinitionSource.getDefinition(_definitionName);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + _definitionName);
    }
    final ConfigDBFXForwardCurveSpecificationSource curveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    _specification = curveSpecificationSource.getSpecification(_specificationName);
    if (_specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + _specificationName);
    }
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, _curveName)
            .withAny(PROPERTY_FORWARD_CURVE_INTERPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE,
            new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, _definition.getTarget().getUniqueId()), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
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
        final ValueProperties properties = ValueProperties.builder()
            .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolatorNames.iterator().next())
            .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolatorNames.iterator().next())
            .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolatorNames.iterator().next()).get();
        final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
        final FXForwardCurveInstrumentProvider provider = _specification.getCurveInstrumentProvider();
        for (final Tenor tenor : _definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(atInstant.toLocalDate(), tenor);
          result.add(new ValueRequirement(provider.getDataFieldName(), identifier, properties));
        }
        result.add(new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument(), properties));
        return Collections.unmodifiableSet(result);
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
        final ValueSpecification spec = inputs.keySet().iterator().next();
        final ValueProperties constraints = spec.getProperties();
        String interpolatorName = null;
        String leftExtrapolatorName = null;
        String rightExtrapolatorName = null;
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR) != null) {
          final Set<String> interpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
          if (interpolatorNames == null || interpolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique interpolator name");
          }
          interpolatorName = interpolatorNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR) != null) {
          final Set<String> leftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
          if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique left extrapolator name");
          }
          leftExtrapolatorName = leftExtrapolatorNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR) != null) {
          final Set<String> rightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
          if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique right extrapolator name");
          }
          rightExtrapolatorName = rightExtrapolatorNames.iterator().next();
        }
        assert interpolatorName != null;
        assert leftExtrapolatorName != null;
        assert rightExtrapolatorName != null;
        return Collections.singleton(getResultSpec(interpolatorName, leftExtrapolatorName, rightExtrapolatorName));
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final DoubleArrayList expiries = new DoubleArrayList();
        final DoubleArrayList forwards = new DoubleArrayList();
        final FXForwardCurveInstrumentProvider provider = _specification.getCurveInstrumentProvider();
        final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument());
        if (inputs.getValue(spotRequirement) == null) {
          throw new OpenGammaRuntimeException("Could not get value for spot; requirement was " + spotRequirement);
        }
        final ValueProperties constraints = spotRequirement.getConstraints();
        final String interpolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR).iterator().next();
        final String leftExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR).iterator().next();
        final String rightExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).iterator().next();
        final Double spot = (Double) inputs.getValue(spotRequirement);
        for (final Tenor tenor : _definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
          if (inputs.getValue(requirement) != null) {
            final Double spread = (Double) inputs.getValue(requirement);
            if (spread != null) {
              expiries.add(DAY_COUNT.getDayCountFraction(now, now.plus(tenor.getPeriod())));
              forwards.add(spot + spread);
            }
          }
        }
        if (expiries.size() == 0) {
          throw new OpenGammaRuntimeException("Could not get any values for FX forwards");
        }
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final ForwardCurve curve = new ForwardCurve(InterpolatedDoublesCurve.from(expiries, forwards, interpolator));
        return Collections.singleton(new ComputedValue(getResultSpec(interpolatorName, leftExtrapolatorName, rightExtrapolatorName), curve));
      }

      private ValueSpecification getResultSpec(final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, _curveName)
            .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, interpolatorName)
            .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, leftExtrapolatorName)
            .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, rightExtrapolatorName).get();
        return new ValueSpecification(ValueRequirementNames.FORWARD_CURVE,
            new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, _definition.getTarget().getUniqueId()), properties);
      }
    };
  }

}
