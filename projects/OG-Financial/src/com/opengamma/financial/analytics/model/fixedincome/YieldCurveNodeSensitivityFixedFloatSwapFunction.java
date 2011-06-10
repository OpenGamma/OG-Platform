/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.swap.FixedFloatSwapFunction;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class YieldCurveNodeSensitivityFixedFloatSwapFunction extends FixedFloatSwapFunction {

  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  // TODO: This will be hit for a curve definition on each calculation cycle, so it really needs to cache stuff rather than do any I/O
  private InterpolatedYieldCurveDefinitionSource _definitionSource;

  public YieldCurveNodeSensitivityFixedFloatSwapFunction() {
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _definitionSource = OpenGammaCompilationContext.getInterpolatedYieldCurveDefinitionSource(context);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionInputs inputs, final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle, final String forwardCurveName,
      final String fundingCurveName) {
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new NullPointerException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array;
    // Fudge encodings of double[][] and List<double[]> are identical, so receiving either is valid.
    if (jacobianObject instanceof double[][]) {
      array = (double[][]) jacobianObject;
    } else if (jacobianObject instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<double[]> parRateJacobianList = (List<double[]>) jacobianObject;
      final int rows = parRateJacobianList.size();
      array = new double[rows][];
      int i = 0;
      for (final double[] d : parRateJacobianList) {
        array[i++] = d;
      }
    } else {
      throw new ClassCastException("Jacobian object " + jacobianObject + " not List<double[]> or double[][]");
    }
    final DoubleMatrix2D parRateJacobian = new DoubleMatrix2D(array);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(forwardCurveName, bundle.getCurve(forwardCurveName));
    final DoubleMatrix1D sensitivitiesForCurves = CALCULATOR.calculateFromParRate(swap, null, interpolatedCurves, parRateJacobian);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    final int n = sensitivitiesForCurves.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(forwardCurveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(_definitionSource, getCurrencyForTarget(security), forwardCurveName);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurves.getEntry(i));
    }
    final YieldCurveNodeSensitivityDataBundle data = new YieldCurveNodeSensitivityDataBundle(getCurrencyForTarget(security), labelledMatrix, forwardCurveName);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, security), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(security).getCode())
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
    result.add(new ComputedValue(specification, data.getLabelledMatrix()));
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(context, desiredValue);
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      return Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), null, null), getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    } else {
      return Sets.newHashSet(
          getCurveRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()),
          getCurveRequirement(target, curveNames.getSecond(), curveNames.getFirst(), curveNames.getSecond()),
          getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(target).getCode())
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(target).getCode())
        .with(ValuePropertyNames.CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get()));
  }

  @Override
  public String getShortName() {
    return "YieldCurveNodeSensitivityFixedFloatSwapFunction";
  }

}
