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

import org.fudgemsg.FudgeFieldContainer;
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
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.financial.riskfactor.GreekDataBundle;
import com.opengamma.financial.riskfactor.GreekToValueGreekConverter;
import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class OptionGreekToValueGreekConverterFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_Log = LoggerFactory.getLogger(OptionGreekToValueGreekConverterFunction.class);
  private static final Map<String, Sensitivity<Greek>> AVAILABLE_VALUE_GREEKS = new HashMap<String, Sensitivity<Greek>>();
  private final Function1D<GreekDataBundle, Map<Sensitivity<Greek>, RiskFactorResult<?>>> _converter = new GreekToValueGreekConverter();

  // TODO extract this out in a similar fashion to AvailableGreeks
  static {
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DELTA, new ValueGreek(Greek.DELTA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_GAMMA, new ValueGreek(Greek.GAMMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_THETA, new ValueGreek(Greek.THETA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VEGA, new ValueGreek(Greek.VEGA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VANNA, new ValueGreek(Greek.VANNA));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final GreekResultCollection greekResultCollection = new GreekResultCollection();
    final Map<Greek, Map<Object, Double>> underlyingData = new HashMap<Greek, Map<Object, Double>>();
    Object greekResult;
    Greek greek;
    Map<Object, Double> underlyingDataForGreek;
    FudgeFieldContainer liveGreeks;
    FudgeFieldContainer liveUnderlying;
    // I have no idea if this will work. I'm assuming that the greeks are treated as live data
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      liveGreeks = (FudgeFieldContainer) inputs.getValue(new ValueRequirement(valueName, security));
      // ?????????????????????????????????????????????????????????????????
      greekResult = inputs.getValue(new ValueRequirement(valueName, security));
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
      //TODO check that there isn't already a value for the greek? can't see how it could happen but
      //overwriting silently in that situation would be bad
      underlyingDataForGreek = new HashMap<Object, Double>();
      underlyingDataForGreek.put(TradeData.NUMBER_OF_CONTRACTS, TradeDataToPositionDataMapper.getData(
          TradeData.NUMBER_OF_CONTRACTS, target));
      ////////////////////////////////////////////////////////////////////////////////////////////
      //TODO: this value is only 25 because I don't know how to get the point value - change it //
      underlyingDataForGreek.put(TradeData.OPTION_POINT_VALUE, 25.);                            //
      //                                                                                        //
      ////////////////////////////////////////////////////////////////////////////////////////////
      for(final Underlying underlying : greek.getOrder().getUnderlyings()) {
        liveUnderlying = (FudgeFieldContainer) inputs.getValue(UnderlyingToValueRequirementMapper.getValueRequirement(underlying, security));
        if (liveUnderlying == null) {
          s_Log.warn("Could not get value for " + underlying + " for security " + security);
        } else {
          underlyingDataForGreek.put(underlying, liveUnderlying.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD));
        }
      }
      underlyingData.put(greek, underlyingDataForGreek);
    }
    final GreekDataBundle dataBundle = new GreekDataBundle(greekResultCollection, underlyingData);
    final Map<Sensitivity<Greek>, RiskFactorResult<?>> sensitivities = _converter.evaluate(dataBundle);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    Sensitivity sensitivity;
    RiskFactorResult<?> riskFactorResult;
    ValueSpecification resultSpecification;
    ComputedValue resultValue;
    for (final ValueRequirement dV : desiredValues) {
      // TODO probably need some checks here
      sensitivity = AVAILABLE_VALUE_GREEKS.get(dV.getValueName());
      riskFactorResult = sensitivities.get(sensitivity);
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
      final Security security = target.getSecurity();
      Set<Underlying> underlyings;
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (final String valueName : AvailableGreeks.getAllGreekNames()) {
        requirements.add(new ValueRequirement(valueName, security));
        underlyings = AvailableGreeks.getGreekForValueRequirementName(valueName).getOrder().getUnderlyings();
        if(underlyings.isEmpty()) {
          //TODO what to do here? will only happen for the price
        } else {
          for(final Underlying underlying : underlyings) {
            requirements.add(UnderlyingToValueRequirementMapper.getValueRequirement(underlying, security));
          }
        }
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
      for (final String valueName : AVAILABLE_VALUE_GREEKS.keySet()) {
        results.add(new ValueSpecification(new ValueRequirement(valueName, security)));
      }
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GreekToValueGreekConverter";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }
}
