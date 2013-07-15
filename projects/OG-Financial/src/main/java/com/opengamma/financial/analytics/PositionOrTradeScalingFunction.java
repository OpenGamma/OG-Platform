/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class PositionOrTradeScalingFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _requirementName;

  public PositionOrTradeScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public String getShortName() {
    return "PositionScaling for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Object value = target.getValue();
    if (value instanceof Position) {
      if (!((Position) value).getTrades().isEmpty()) { // Use PositionTradeScalingFunction instead
        return false;
      }
    }
    return ((PositionOrTrade) value).getSecurity() != null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(_requirementName, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Security security = target.getPositionOrTrade().getSecurity();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), desiredValue.getConstraints().withoutAny(
        ValuePropertyNames.FUNCTION));
    return Collections.singleton(requirement);
  }

  protected ValueProperties getResultProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(input));
    return Collections.singleton(specification);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final Object value = input.getValue();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(input.getSpecification()));
    ComputedValue scaledValue;
    final double quantity = target.getPositionOrTrade().getQuantity().doubleValue();
    if (value instanceof Double) {
      Double doubleValue = (Double) value;
      doubleValue *= quantity;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else if (value instanceof MultipleCurrencyAmount) {
      final MultipleCurrencyAmount m = (MultipleCurrencyAmount) value;
      scaledValue = new ComputedValue(specification, m.multipliedBy(quantity));
    } else if (value instanceof YieldCurveNodeSensitivityDataBundle) {
      final YieldCurveNodeSensitivityDataBundle nodeSensitivities = (YieldCurveNodeSensitivityDataBundle) value;
      final Currency ccy = nodeSensitivities.getCurrency();
      final String name = nodeSensitivities.getYieldCurveName();
      final DoubleLabelledMatrix1D m = nodeSensitivities.getLabelledMatrix();
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new YieldCurveNodeSensitivityDataBundle(ccy, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled), name));
    } else if (value instanceof DoubleLabelledMatrix1D) {
      final DoubleLabelledMatrix1D m = (DoubleLabelledMatrix1D) value;
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof LocalDateLabelledMatrix1D) {
      final LocalDateLabelledMatrix1D m = (LocalDateLabelledMatrix1D) value;
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new LocalDateLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof ZonedDateTimeLabelledMatrix1D) {
      final ZonedDateTimeLabelledMatrix1D m = (ZonedDateTimeLabelledMatrix1D) value;
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new ZonedDateTimeLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof CurrencyLabelledMatrix1D) {
      final CurrencyLabelledMatrix1D m = (CurrencyLabelledMatrix1D) value;
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new CurrencyLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof StringLabelledMatrix1D) {
      final StringLabelledMatrix1D m = (StringLabelledMatrix1D) value;
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new StringLabelledMatrix1D(m.getKeys(), scaled));
    } else if (_requirementName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> map = (Map<String, List<DoublesPair>>) value;
      final Map<String, List<DoublesPair>> scaled = Maps.newHashMap();
      for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
        final List<DoublesPair> scaledList = Lists.newArrayList();
        for (final DoublesPair pair : entry.getValue()) {
          scaledList.add(DoublesPair.of(pair.first, pair.second * quantity));
        }
        scaled.put(entry.getKey(), scaledList);
      }
      scaledValue = new ComputedValue(specification, scaled);
    } else if (value instanceof DoubleLabelledMatrix2D) {
      final DoubleLabelledMatrix2D matrix = (DoubleLabelledMatrix2D) value;
      final Double[] xKeys = matrix.getXKeys();
      final Object[] xLabels = matrix.getXLabels();
      final Double[] yKeys = matrix.getYKeys();
      final Object[] yLabels = matrix.getYLabels();
      final double[][] values = matrix.getValues();
      final int n = values.length;
      if (n == 0) {
        return Collections.singleton(new ComputedValue(specification, value));
      }
      final int m = values[0].length;
      final double[][] scaledValues = new double[n][m];
      final double scale = quantity;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
          scaledValues[i][j] = values[i][j] * scale;
        }
      }
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, scaledValues));
    } else if (value instanceof DoubleLabelledMatrix3D) {
      final DoubleLabelledMatrix3D matrix = (DoubleLabelledMatrix3D) value;
      final Double[] xKeys = matrix.getXKeys();
      final Object[] xLabels = matrix.getXLabels();
      final Double[] yKeys = matrix.getYKeys();
      final Object[] yLabels = matrix.getYLabels();
      final Double[] zKeys = matrix.getZKeys();
      final Object[] zLabels = matrix.getZLabels();
      final double[][][] values = matrix.getValues();
      final int n = values.length;
      final int m = values[0].length;
      final int l = values[0][0].length;
      final double[][][] scaledValues = new double[n][m][l];
      final double scale = quantity;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
          for (int k = 0; k < l; k++) {
            scaledValues[i][j][k] = values[i][j][k] * scale;
          }
        }
      }
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, scaledValues));
    } else {
      //REVIEW emcleod 27-1-2011 aaaaaaaaaarrrrrrrrgggggghhhhhhhhh Why is nothing done here?
      scaledValue = new ComputedValue(specification, value);
    }
    return Collections.singleton(scaledValue);
  }

  private double[] getScaledMatrix(final double[] values, final double quantity) {
    final int n = values.length;
    final double[] scaled = new double[n];
    for (int i = 0; i < n; i++) {
      scaled[i] = values[i] * quantity;
    }
    return scaled;
  }

}
