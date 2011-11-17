/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class PositionTradeScalingFunction extends PropertyPreservingFunction {

  @Override
  protected Collection<String> getPreservedProperties() {
    // TODO [PLAT-1356] PositionTradeScalingFunction should propagate everything
    return Arrays.asList(
        ValuePropertyNames.CUBE,
        ValuePropertyNames.CURRENCY,
        ValuePropertyNames.CURVE,
        ValuePropertyNames.CURVE_CURRENCY,
        YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        YieldCurveFunction.PROPERTY_FUNDING_CURVE,
        ValuePropertyNames.CURVE_CALCULATION_METHOD,
        ValuePropertyNames.CALCULATION_METHOD,
        ValuePropertyNames.SURFACE,
        ValuePropertyNames.PAY_CURVE,
        ValuePropertyNames.RECEIVE_CURVE,
        ValuePropertyNames.SMILE_FITTING_METHOD);
  }

  @Override
  protected Collection<String> getOptionalPreservedProperties() {
    return Collections.emptySet();
  }

  private final String _requirementName;

  public PositionTradeScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return (target.getType() == ComputationTargetType.POSITION) && !target.getPosition().getTrades().isEmpty();
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Set<Trade> trades = position.getTrades();
    if (trades.isEmpty()) {
      // Shouldn't happen; canApplyTo will reject it
      throw new OpenGammaRuntimeException("Position has no trades");
    }
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final Trade trade : trades) {
      result.add(new ValueRequirement(_requirementName, ComputationTargetType.TRADE, trade.getUniqueId(), getInputConstraint(desiredValue)));
    }
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(specification);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet().iterator().next()));
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "PositionTradeScaling for " + _requirementName;
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
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new YieldCurveNodeSensitivityDataBundle(ccy, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled), name));
    } else if (value instanceof DoubleLabelledMatrix1D) {
      final DoubleLabelledMatrix1D m = (DoubleLabelledMatrix1D) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof LocalDateLabelledMatrix1D) {
      final LocalDateLabelledMatrix1D m = (LocalDateLabelledMatrix1D) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new LocalDateLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof ZonedDateTimeLabelledMatrix1D) {
      final ZonedDateTimeLabelledMatrix1D m = (ZonedDateTimeLabelledMatrix1D) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new ZonedDateTimeLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (value instanceof CurrencyLabelledMatrix1D) {
      final CurrencyLabelledMatrix1D m = (CurrencyLabelledMatrix1D) value;
      final double quantity = target.getPosition().getQuantity().doubleValue();
      final double[] scaled = getScaledMatrix(m.getValues(), quantity);
      scaledValue = new ComputedValue(specification, new CurrencyLabelledMatrix1D(m.getKeys(), m.getLabels(), scaled));
    } else if (_requirementName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> map = (Map<String, List<DoublesPair>>) value;
      final Map<String, List<DoublesPair>> scaled = new HashMap<String, List<DoublesPair>>();
      for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
        final List<DoublesPair> scaledList = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : entry.getValue()) {
          scaledList.add(DoublesPair.of(pair.first, pair.second * target.getPosition().getQuantity().doubleValue()));
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
      final int m = values[0].length;
      final double[][] scaledValues = new double[n][m];
      final double scale = target.getPosition().getQuantity().doubleValue();
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
          scaledValues[i][j] = values[i][j] * scale;
        }
      }
      scaledValue = new ComputedValue(specification, new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, scaledValues));
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
