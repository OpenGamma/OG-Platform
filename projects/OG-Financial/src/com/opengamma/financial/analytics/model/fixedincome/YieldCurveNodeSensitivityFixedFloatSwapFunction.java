/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.model.swap.FixedFloatSwapFunction;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class YieldCurveNodeSensitivityFixedFloatSwapFunction extends FixedFloatSwapFunction {
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private InterpolatedYieldCurveDefinitionSource _definitionSource;

  public YieldCurveNodeSensitivityFixedFloatSwapFunction(final String currency, final String curveName, final String valueRequirementName) {
    super(Currency.getInstance(currency), curveName, valueRequirementName, curveName, valueRequirementName);
  }

  public YieldCurveNodeSensitivityFixedFloatSwapFunction(final String currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(Currency.getInstance(currency), forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  public YieldCurveNodeSensitivityFixedFloatSwapFunction(final Currency currency, final String name, final String valueRequirementName) {
    super(currency, name, valueRequirementName, name, valueRequirementName);
  }

  public YieldCurveNodeSensitivityFixedFloatSwapFunction(final Currency currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(currency, forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _definitionSource = OpenGammaCompilationContext.getInterpolatedYieldCurveDefinitionSource(context);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionInputs inputs, final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle) {
    final ValueRequirement jacobianRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, getCurrencyForTarget(security).getUniqueId());
    final Object jacobianObject = inputs.getValue(jacobianRequirement);
    if (jacobianObject == null) {
      throw new NullPointerException("Could not get " + jacobianRequirement);
    }
    @SuppressWarnings("unchecked")
    final List<double[]> parRateJacobianList = (List<double[]>) jacobianObject;
    final int rows = parRateJacobianList.size();
    final double[][] array = new double[rows][];
    int i = 0;
    for (final double[] d : parRateJacobianList) {
      array[i++] = d;
    }
    final DoubleMatrix2D parRateJacobian = new DoubleMatrix2D(array);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(getForwardCurveName(), bundle.getCurve(getForwardCurveName()));
    final DoubleMatrix1D sensitivitiesForCurves = CALCULATOR.calculateFromParRate(swap, null, interpolatedCurves, parRateJacobian);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    final int n = sensitivitiesForCurves.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(getForwardCurveName());
    final Double[] keys = curve.getCurve().getXData();
    final Object[] labels = YieldCurveLabelGenerator.getLabels(_definitionSource, getCurrency(), getForwardCurveName());
    final double[] values = new double[n];
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, values);
    for (i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.addIgnoringLabel(keys[i], labels, sensitivitiesForCurves.getEntry(i));
    }
    final YieldCurveNodeSensitivityDataBundle data = new YieldCurveNodeSensitivityDataBundle(getCurrency(), labelledMatrix, getForwardCurveName());
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES + "_" + getForwardCurveName() + "_"
        + getCurrency().getISOCode(), security), createValueProperties().with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(security).getISOCode()).get());
    result.add(new ComputedValue(specification, data.getLabelledMatrix()));
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final UniqueIdentifier ccy = getCurrencyForTarget(target).getUniqueId();
      if (getForwardCurveName().equals(getFundingCurveName())) {
        return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, ccy), new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN,
            ComputationTargetType.PRIMITIVE, ccy));
      }
      return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId()), new ValueRequirement(
          getFundingValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final ValueProperties props = createValueProperties().with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(target).getISOCode()).get();
      if (getForwardCurveName().equals(getFundingCurveName())) {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES + "_" + getForwardCurveName() + "_" + getCurrency().getISOCode(),
            target.getSecurity()), props));
      }
      return Sets.newHashSet(
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES + "_" + getForwardCurveName() + "_" + getCurrency().getISOCode(), target.getSecurity()),
              props),
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES + "_" + getFundingCurveName() + "_" + getCurrency().getISOCode(), target.getSecurity()),
              props));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "YieldCurveNodeSensitivityFixedFloatSwapFunction";
  }

}
