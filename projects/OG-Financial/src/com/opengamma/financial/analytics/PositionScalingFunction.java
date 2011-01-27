/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.core.common.Currency;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class PositionScalingFunction extends PropertyPreservingFunction {

  @Override
  protected String[] getPreservedProperties() {
    return new String[] {ValuePropertyNames.CURRENCY};
  }

  private final String _requirementName;

  public PositionScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue));
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(specification);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.iterator().next()));
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "PositionScaling for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final Object value = input.getValue();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(input.getSpecification()));
    ComputedValue scaledValue = null;
    if (value instanceof Double) {
      Double doubleValue = (Double) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      doubleValue *= quantity;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else if (value instanceof YieldCurveNodeSensitivityDataBundle) {
      final YieldCurveNodeSensitivityDataBundle nodeSensitivities = (YieldCurveNodeSensitivityDataBundle) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final Currency ccy = nodeSensitivities.getCurrency();
      final String name = nodeSensitivities.getYieldCurveName();
      final DoubleLabelledMatrix1D m = nodeSensitivities.getLabelledMatrix();
      final double[] values = m.getValues();
      final int n = values.length;
      final double[] scaled = new double[n];
      for (int i = 0; i < n; i++) {
        scaled[i] = values[i] * quantity;
      }
      scaledValue = new ComputedValue(specification, new YieldCurveNodeSensitivityDataBundle(ccy, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled), name));
    } else if (value instanceof DoubleLabelledMatrix1D) {
      final DoubleLabelledMatrix1D m = (DoubleLabelledMatrix1D) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final double[] values = m.getValues();
      final int n = values.length;
      final double[] scaled = new double[n];
      for (int i = 0; i < n; i++) {
        scaled[i] = values[i] * quantity;
      }
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else {
      //REVIEW emcleod 27-1-2011 aaaaaaaaaarrrrrrrrgggggghhhhhhhhh Why is nothing done here?
      scaledValue = new ComputedValue(specification, value);
    }
    return Collections.singleton(scaledValue);
  }

}
