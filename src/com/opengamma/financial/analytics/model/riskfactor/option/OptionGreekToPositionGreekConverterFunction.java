/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.riskfactor.GreekDataBundle;
import com.opengamma.financial.riskfactor.GreekToPositionGreekConverter;
import com.opengamma.financial.riskfactor.PositionGreek;
import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class OptionGreekToPositionGreekConverterFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_Log = LoggerFactory.getLogger(OptionGreekToPositionGreekConverterFunction.class);
  private static final Map<String, PositionGreek> AVAILABLE_POSITION_GREEKS = new HashMap<String, PositionGreek>();
  private final Function1D<GreekDataBundle, Map<PositionGreek, RiskFactorResult<?>>> _converter = new GreekToPositionGreekConverter();

  // TODO extract this out in a similar fashion to AvailableGreeks
  static {
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DELTA, PositionGreek.POSITION_DELTA);
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_GAMMA, PositionGreek.POSITION_GAMMA);
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_RHO, PositionGreek.POSITION_RHO);
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_THETA, PositionGreek.POSITION_THETA);
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VEGA, PositionGreek.POSITION_VEGA);
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VANNA, PositionGreek.POSITION_VANNA);

  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final GreekResultCollection greekResultCollection = new GreekResultCollection();
    final Map<Greek, Map<Object, Double>> underlyingData = new HashMap<Greek, Map<Object, Double>>();
    Object greekResult;
    Greek greek;
    Map<Object, Double> underlyingDataForGreek;
    // REVIEW: I have no idea if this will work
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      greekResult = inputs.getValue(new ValueRequirement(valueName, target.getSecurity()));
      greek = AvailableGreeks.getGreekForValueRequirementName(valueName);
      if (greekResult == null) {
        s_Log.warn("Could not get value for " + valueName + ", continuing");
      } else {
        if (greekResult instanceof Double) {
          greekResultCollection.put(greek, new SingleGreekResult((Double) greekResult));
        } else if (greekResult instanceof Map<?, ?>) {
          greekResultCollection.put(greek, new MultipleGreekResult((Map<String, Double>) greekResult));
        } else {
          throw new IllegalArgumentException("Got a value for greek " + valueName + " that is neither a Double nor a Map<String, Double>: should never happen");
        }
      }
      underlyingDataForGreek = new HashMap<Object, Double>();
      underlyingDataForGreek.put(TradeData.NUMBER_OF_CONTRACTS, target.getPosition().getQuantity().doubleValue());
      underlyingData.put(greek, underlyingDataForGreek);
    }
    final GreekDataBundle dataBundle = new GreekDataBundle(greekResultCollection, underlyingData);
    final Map<PositionGreek, RiskFactorResult<?>> positionGreeks = _converter.evaluate(dataBundle);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    PositionGreek positionGreek;
    RiskFactorResult<?> riskFactorResult;
    ValueSpecification resultSpecification;
    ComputedValue resultValue;
    for (final ValueRequirement dV : desiredValues) {
      // TODO probably need some checks here
      positionGreek = AVAILABLE_POSITION_GREEKS.get(dV.getValueName());
      riskFactorResult = positionGreeks.get(positionGreek);
      resultSpecification = new ValueSpecification(new ValueRequirement(dV.getValueName(), target.getSecurity()));
      resultValue = new ComputedValue(resultSpecification, riskFactorResult.getResult());
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (final String valueName : AvailableGreeks.getAllGreekNames()) {
        requirements.add(new ValueRequirement(valueName, target.getSecurity()));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Security security = target.getSecurity();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final String valueName : AVAILABLE_POSITION_GREEKS.keySet()) {
        results.add(new ValueSpecification(new ValueRequirement(valueName, security)));
      }
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GreekToPositionGreekConverter";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
