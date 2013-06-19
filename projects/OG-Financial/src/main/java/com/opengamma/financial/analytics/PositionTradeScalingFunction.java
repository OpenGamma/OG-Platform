/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
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
import com.opengamma.util.tuple.DoublesPair;

/**
 * Sums the values of the trades that make up a position to produce the position's value.
 */
public class PositionTradeScalingFunction extends AbstractFunction.NonCompiledInvoker {

  // NOTE: The name is for legacy reasons; this does no scaling and should really be called PositionTradeSummingFunction or similar.
  // All scaling is done by PositionOrTradeScaling function which will work nicely for positions with just one trade.

  private final String _requirementName;

  public PositionTradeScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
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
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    // Only apply when there are multiple trades; otherwise the PositionOrTradeScaling function on its own will work. Allowing this
    // into the graph creates an ambiguity otherwise as either PositionTradeScaling(POSITION) <- PositionOrTradeScaling(TRADE) <- F(SECURITY)
    // or PositionOrTradeScaling(POSITION) <- F(SECURITY) may be possible for some value requirements
    return (!target.getPosition().getTrades().isEmpty());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), ValueProperties.all());
    return Collections.singleton(specification);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Collection<Trade> trades = position.getTrades();
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final ValueProperties inputConstraint = desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION);
    for (final Trade trade : trades) {
      result.add(new ValueRequirement(_requirementName, ComputationTargetType.TRADE, trade.getUniqueId(), inputConstraint));
    }
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Result properties are anything that was common to the input specifications
    ValueProperties common = null;
    for (final ValueSpecification input : inputs.keySet()) {
      common = SumUtils.addProperties(common, input.getProperties());
    }
    if (common == null) {
      // Can't have been any inputs ... ?
      return null;
    }
    common = common.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return Collections.singleton(new ValueSpecification(_requirementName, target.toSpecification(), common));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    Object summedValue = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final Object value = input.getValue();
      if (value == null) {
        continue;
      }
      if (value instanceof YieldCurveNodeSensitivityDataBundle) {
        final YieldCurveNodeSensitivityDataBundle nodeSensitivities = (YieldCurveNodeSensitivityDataBundle) value;
        final Currency ccy = nodeSensitivities.getCurrency();
        final String name = nodeSensitivities.getYieldCurveName();
        final DoubleLabelledMatrix1D m = nodeSensitivities.getLabelledMatrix();
        YieldCurveNodeSensitivityDataBundle dataBundle = (YieldCurveNodeSensitivityDataBundle) summedValue;
        if (ccy.equals(dataBundle.getCurrency()) && name.equals(dataBundle.getYieldCurveName())) {
          summedValue = new YieldCurveNodeSensitivityDataBundle(ccy, (DoubleLabelledMatrix1D) SumUtils.addValue(dataBundle.getLabelledMatrix(), m, _requirementName), name);
        } else {
          // TODO: Throwing the value away like this looks bad!
        }
      } else if (_requirementName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
        // THIS IS ALMOST CERATINLY WRONG
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
        // TODO: THIS IS MOST DEFINATELY WRONG - We should be adding, not scaling in this function. Should we add or does that make no sense and this is from when we used to scale in this function? 
        summedValue = scaled;
      } else {
        summedValue = SumUtils.addValue(summedValue, value, _requirementName);
      }
    }
    if (summedValue == null) {
      return null;
    }
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(specification, summedValue));
  }

}
