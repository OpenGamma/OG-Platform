/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.id.ExternalId;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class DummyFXForwardCurveFunction extends AbstractFunction {
  private static final double[] EXPIRIES = new double[] {7. / 365, 14. / 365, 21. / 365, 1. / 12, 1. / 4, 1. / 2, 3. / 4, 1, 5, 10};
  private static final double[] FORWARDS = new double[] {1.314562, 1.314508, 1.314517, 1.314548, 1.314804, 1.315253, 1.316140, 1.317225, 1.317903, 1.323726};
  private final String _curveName;
  private final UnorderedCurrencyPair _currencyPair;
  private final ExternalId _spotInstrument;

  public DummyFXForwardCurveFunction(final String curveName, final String ccy1, final String ccy2) {
    _curveName = curveName;
    _currencyPair = UnorderedCurrencyPair.of(Currency.of(ccy1), Currency.of(ccy2));
    _spotInstrument = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, ccy1 + " Curncy");
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
            .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
            .withAny(PROPERTY_FORWARD_CURVE_INTERPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
            .withAny(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
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
        result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, _spotInstrument, properties));
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
        return Collections.singleton(getResultSpec(target, interpolatorName, leftExtrapolatorName, rightExtrapolatorName));
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _currencyPair.getUniqueId());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
        final String interpolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR).iterator().next();
        final String leftExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR).iterator().next();
        final String rightExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).iterator().next();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final ForwardCurve curve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator));
        return Collections.singleton(new ComputedValue(getResultSpec(target, interpolatorName, leftExtrapolatorName, rightExtrapolatorName), curve));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, _curveName)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "Dummy")
            .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, interpolatorName)
            .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, leftExtrapolatorName)
            .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, rightExtrapolatorName).get();
        return new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
      }
    };
  }

}
