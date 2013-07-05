/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
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
import com.opengamma.util.money.Currency;

/**
 * Allows dumb clients to get interpolated {@link YieldCurve}s
 */
public class YieldCurveInterpolatingFunction extends AbstractFunction {
  private final Currency _currency;
  private final String _curveName;

  private ValueSpecification _interpolatedCurveResult;
  private Set<ValueSpecification> _results;

  public YieldCurveInterpolatingFunction(final String currency, final String curveDefinitionName) {
    this(Currency.of(currency), curveDefinitionName);
  }

  public YieldCurveInterpolatingFunction(final Currency currency, final String curveDefinitionName) {
    _currency = currency;
    _curveName = curveDefinitionName;

  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencySpec = ComputationTargetSpecification.of(_currency);

    _interpolatedCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INTERPOLATED, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get());
    _results = Sets.newHashSet(_interpolatedCurveResult);
  }

  /**
   *
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final ValueRequirement _curveRequirement;
    private final Set<ValueRequirement> _requirements;

    private CompiledImpl(final ValueRequirement curveRequirement) {
      super();
      _curveRequirement = curveRequirement;
      _requirements = Sets.newHashSet(curveRequirement);
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final NodalDoublesCurve curve = interpolateCurve((YieldCurve) inputs.getValue(_curveRequirement));
      return Sets.newHashSet(new ComputedValue(_interpolatedCurveResult, curve));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      if (canApplyTo(context, target)) {
        return _requirements;
      }
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CURRENCY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _currency.equals(target.getValue());
    }
  }


  private static NodalDoublesCurve interpolateCurve(final YieldCurve yieldCurve) {
    final Curve<Double, Double> curve = yieldCurve.getCurve();
    return interpolateCurve(curve);
  }

  public static NodalDoublesCurve interpolateCurve(final Curve<Double, Double> curve) {
    if (curve instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve;

      // This is a hack for now as it's all about to change
      final Interpolator1DDataBundle interpolatorBundle = interpolatedCurve.getDataBundle();
      final double first = interpolatorBundle.firstKey();
      final double last = interpolatorBundle.lastKey();

      return interpolateCurve(curve, first, last);

    } else {
      final double first = 1. / 12;
      final double last = 30;

      return interpolateCurve(curve, first, last);
    }
  }

  private static NodalDoublesCurve interpolateCurve(final Curve<Double, Double> curve, final double first, final double last) {
    final int steps = 100;

    final List<Double> xs = new ArrayList<Double>(steps);
    final List<Double> ys = new ArrayList<Double>(steps);

    // Output 100 points equally spaced along the curve
    final double step = (last - first) / (steps - 1);
    for (int i = 0; i < steps; i++) {
      final double t = first + step * i;
      xs.add(t);
      ys.add(curve.getYValue(t));
    }
    return new NodalDoublesCurve(xs, ys, true);
  }


  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ValueRequirement curveReq = new ValueRequirement(ValueRequirementNames.YIELD_CURVE,
        ComputationTargetSpecification.of(_currency),
        ValueProperties.with(ValuePropertyNames.CURVE, _curveName).get());
    return new CompiledImpl(curveReq);
  }
}
