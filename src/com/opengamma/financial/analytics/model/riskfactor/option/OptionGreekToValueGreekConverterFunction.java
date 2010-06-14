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
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.riskfactor.GreekDataBundle;
import com.opengamma.financial.riskfactor.GreekToValueGreekConverter;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class OptionGreekToValueGreekConverterFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_Log = LoggerFactory.getLogger(OptionGreekToValueGreekConverterFunction.class);
  private final Function1D<GreekDataBundle, Map<ValueGreek, Double>> _converter = new GreekToValueGreekConverter();
  private final String _requirementName;
  
  public OptionGreekToValueGreekConverterFunction(String requirementName) {
    ArgumentChecker.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  /**
   * @return the requirementName
   */
  public String getRequirementName() {
    return _requirementName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    final GreekResultCollection greekResultCollection = new GreekResultCollection();
    final Map<Object, Double> underlyingData = new HashMap<Object, Double>();
    Greek greek;
    Underlying order;
    FudgeFieldContainer liveUnderlying;
    Set<UnderlyingType> underlyings;
    String underlyingGreekRequirementName = AvailableValueGreeks.getGreekRequirementNameForValueGreekName(getRequirementName());
    Double greekResult = (Double) inputs.getValue(new ValueRequirement(underlyingGreekRequirementName, security));
    greek = AvailableGreeks.getGreekForValueRequirementName(underlyingGreekRequirementName);
    greekResultCollection.put(greek, greekResult);
    
    //TODO check that there isn't already a value for the greek? can't see how it could happen but
    //overwriting silently in that situation would be bad
    underlyingData.put(TradeData.NUMBER_OF_CONTRACTS, TradeDataToPositionDataMapper.getData(TradeData.NUMBER_OF_CONTRACTS, target));
    // TODO kirk 2010-05-13 -- Once we have the point value from the security,
    // swap these two lines.
    underlyingData.put(TradeData.POINT_VALUE, 1.);
    //underlyingData.put(TradeData.POINT_VALUE, security instanceof ExchangeTradedOptionSecurity ? ((ExchangeTradedOptionSecurity) security).getPointValue() : 1);
    order = greek.getUnderlying();
    underlyings = order.getUnderlyings();
    for (final UnderlyingType underlying : underlyings) {
      liveUnderlying = (FudgeFieldContainer) inputs.getValue(UnderlyingTypeToValueRequirementMapper.getValueRequirement(executionContext.getSecurityMaster(), underlying, security));
      if (liveUnderlying == null) {
        s_Log.warn("Could not get value for " + underlying + " for security " + security);
      } else {
        underlyingData.put(underlying, liveUnderlying.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD));
      }
    }
    
    final GreekDataBundle dataBundle = new GreekDataBundle(greekResultCollection, underlyingData);
    final Map<ValueGreek, Double> sensitivities = _converter.evaluate(dataBundle);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    ValueGreek valueGreek;
    Double valueGreekResult;
    ValueSpecification resultSpecification;
    ComputedValue resultValue;
    for (final ValueRequirement dV : desiredValues) {
      // TODO probably need some checks here
      valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(dV.getValueName());
      valueGreekResult = sensitivities.get(valueGreek);
      resultSpecification = new ValueSpecification(new ValueRequirement(dV.getValueName(), target.getPosition()));
      resultValue = new ComputedValue(resultSpecification, valueGreekResult);
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
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    Set<UnderlyingType> underlyings;
    Underlying order;
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    String underlyingGreekRequirementName = AvailableValueGreeks.getGreekRequirementNameForValueGreekName(getRequirementName());
    requirements.add(new ValueRequirement(underlyingGreekRequirementName, security));
    order = AvailableGreeks.getGreekForValueRequirementName(underlyingGreekRequirementName).getUnderlying();
    if (order == null) {
      throw new UnsupportedOperationException("No available order for configured value greek " + getRequirementName());
    }
    underlyings = order.getUnderlyings();
    if (underlyings.isEmpty()) {
      //TODO what to do here? will only happen for the price
    } else {
      for (final UnderlyingType underlying : underlyings) {
        requirements.add(UnderlyingTypeToValueRequirementMapper.getValueRequirement(context.getSecurityMaster(), underlying, security));
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Position position = target.getPosition();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(new ValueRequirement(getRequirementName(), position)));
    return results;
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
