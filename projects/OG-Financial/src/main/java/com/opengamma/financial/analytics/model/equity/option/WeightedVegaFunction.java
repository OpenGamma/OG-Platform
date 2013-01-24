/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * TODO - PROTOTYPE 
 * @author casey
 *
 */
public class WeightedVegaFunction extends AbstractFunction.NonCompiledInvoker {

  private static String s_vega = ValueRequirementNames.VEGA;
  private static String s_weightedVega = ValueRequirementNames.WEIGHTED_VEGA;
  
  private String getValueRequirementName() {
    return s_weightedVega;
  }
  
  @Override
  // TODO - Pass Weighting Property - Base Days
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        
    // 1. Get vega
    Double vega = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(s_vega)) {
        Object inputVal = input.getValue();
        if (inputVal != null) {
          vega = (Double) inputVal;
        } else {
          throw new OpenGammaRuntimeException("Did not satisfy requirement," + s_vega + ", for trade" + target.getTrade().getUniqueId());
        }
      }
    }
    
    // 2. Compute Weighted Vega
    
    Double weighting = null;
    final Security security = target.getTrade().getSecurity();
    if (security instanceof EquityOptionSecurity) {
      ZonedDateTime expiry = ((EquityOptionSecurity) security).getExpiry().getExpiry();
      ZonedDateTime valDt = executionContext.getValuationClock().zonedDateTimeToMinute();
      weighting = TimeCalculator.getTimeBetween(valDt, expiry);
    }
    
    //final int baseDays = 90;
    //final Integer daysToExpiry = null;
    //final double weighting = Math.sqrt(baseDays / daysToExpiry);
    
    final double weightedVega = weighting * vega;
    
 // 3. Create spec and return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, weightedVega);
    return Sets.newHashSet(result);
    
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {

    final Security security = target.getTrade().getSecurity();
    if (security instanceof EquityOptionSecurity || security instanceof EquityIndexOptionSecurity) {
      return true;
    }
    return false;
    
  }

  @Override
  // TODO What does it imply to call createValueProperties()? Is this the ALL that Elaine talked about?
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties().get()));
  }

  @Override
  // TODO FINISH ME
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      return null;
    }
    String keyName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (input.getValue().getValueName().equals(ValueRequirementNames.VEGA)) {
        keyName = input.getKey().getValueName();
      }
    }
    final Builder propertiesBuilder = createValueProperties();
    if (keyName != null) {
      propertiesBuilder.with("MarkToMarketTimeSeries", keyName);
    }    
      
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), propertiesBuilder.get()));
  }

  @Override
  /** TODO FINISH ME - Basically, we just need the Vega for the trade
   *  
   */
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    Trade trade = target.getTrade();

    final ValueRequirement vegaReq = new ValueRequirement(ValueRequirementNames.VEGA, ComputationTargetType.TRADE, trade.getUniqueId());
    final Set<ValueRequirement> requirements = Sets.newHashSet(vegaReq);
    return requirements;
  }

}
