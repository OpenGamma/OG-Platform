/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
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

  public YieldCurveInterpolatingFunction(Currency currency, String curveDefinitionName) {
    _currency = currency;
    _curveName = curveDefinitionName;
    
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_currency);
    
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
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      NodalDoublesCurve curve = interpolateCurve((YieldCurve) inputs.getValue(_curveRequirement));
      return Sets.newHashSet(new ComputedValue(_interpolatedCurveResult, curve));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
        ValueRequirement desiredValue) {
      if (canApplyTo(context, target)) {
        return _requirements;
      }
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return _results;
    }
    
    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      return _currency.getUniqueId().equals(target.getUniqueId());
    }
  }


  private static NodalDoublesCurve interpolateCurve(YieldCurve yieldCurve) {
    Curve<Double, Double> curve = yieldCurve.getCurve();
    return interpolateCurve(curve);
  }

  public static NodalDoublesCurve interpolateCurve(Curve<Double, Double> curve) {
    if (curve instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve;

      // This is a hack for now as it's all about to change
      Interpolator1DDataBundle interpolatorBundle = interpolatedCurve.getDataBundle();
      double first = interpolatorBundle.firstKey();
      double last = interpolatorBundle.lastKey();

      return interpolateCurve(curve, first, last);

    } else {
      double first = 1. / 12;
      double last = 30;
      
      return interpolateCurve(curve, first, last);
    }
  }

  private static NodalDoublesCurve interpolateCurve(Curve<Double, Double> curve, double first, double last) {
    int steps = 100;
    
    List<Double> xs = new ArrayList<Double>(steps);
    List<Double> ys = new ArrayList<Double>(steps);
    
    // Output 100 points equally spaced along the curve
    double step = (last - first) / (steps - 1);
    for (int i = 0; i < steps; i++) {
      double t = first + step * i;
      xs.add(t);
      ys.add(curve.getYValue(t));
    }
    return new NodalDoublesCurve(xs, ys, true);
  }


  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    ValueRequirement curveReq = new ValueRequirement(ValueRequirementNames.YIELD_CURVE,
        new ComputationTargetSpecification(_currency),
        ValueProperties.with(ValuePropertyNames.CURVE, _curveName).get());
    return new CompiledImpl(curveReq);
  }
}
