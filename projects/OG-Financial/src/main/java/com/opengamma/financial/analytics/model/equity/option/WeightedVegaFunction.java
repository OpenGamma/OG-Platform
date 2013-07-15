/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * TODO - PROTOTYPE 
 * @author casey
 *
 */
public class WeightedVegaFunction extends AbstractFunction.NonCompiledInvoker {

  private static String s_vega = ValueRequirementNames.VEGA;
  private static String s_weightedVega = ValueRequirementNames.WEIGHTED_VEGA;
  private static int s_baseDays = 90; // TODO - Should be property available to the user 
  
  private String getValueRequirementName() {
    return s_weightedVega;
  }
  
  @Override
  // TODO - Pass Weighting Property - Base Days
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        
    // 1. Get Vega
    Double vega = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(s_vega)) {
        Object inputVal = input.getValue();
        if (inputVal != null) {
          vega = (Double) inputVal;
        } else {
          throw new OpenGammaRuntimeException("Did not satisfy requirement," + s_vega + ", for security" + target.getSecurity().toString());
        }
      }
    }
    
    // 2. Compute Weighted Vega
    
    Expiry expiry = null; // TODO: Create an ExpiryVisitor - this is the only reason we need to mention specific securities..
    final Security security = target.getSecurity();
    
    if (security instanceof EquityOptionSecurity) {
      expiry = ((EquityOptionSecurity) security).getExpiry();
    } else if (security instanceof EquityIndexOptionSecurity) {
      expiry = ((EquityIndexOptionSecurity) security).getExpiry();
    } else if (security instanceof EquityIndexFutureOptionSecurity) {
      expiry = ((EquityIndexFutureOptionSecurity) security).getExpiry();
    } else {
      s_logger.error("If applicable, please add the following SecurityType to WeightedVegaFunction, " + security.getSecurityType());
    }
    
    
    if (expiry.getAccuracy().equals(ExpiryAccuracy.MONTH_YEAR) || expiry.getAccuracy().equals(ExpiryAccuracy.YEAR)) {
      throw new OpenGammaRuntimeException("Security's Expiry is not accurate to the day, which is required: " + security.toString());
    }
    
    final long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(executionContext.getValuationClock()), expiry.getExpiry().toLocalDate()); 
    final double weighting = Math.sqrt(s_baseDays / Math.max(daysToExpiry, 1.0));     
    final double weightedVega = weighting * vega;
    
    // 3. Create specification and return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, weightedVega);
    return Sets.newHashSet(result);
    
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    
    final Security security = target.getSecurity();
    if (security instanceof EquityOptionSecurity || 
        security instanceof EquityIndexOptionSecurity ||
        security instanceof EquityIndexFutureOptionSecurity) {
      return true;
    }
    return false;
    
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification vega = inputs.keySet().iterator().next();
    final ValueProperties properties = vega.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueRequirement vegaReq = new ValueRequirement(s_vega, target.toSpecification(), desiredValue.getConstraints().withoutAny(
        ValuePropertyNames.FUNCTION));
    final Set<ValueRequirement> requirements = Sets.newHashSet(vegaReq);
    return requirements;
  }

  private static final Logger s_logger = LoggerFactory.getLogger(WeightedVegaFunction.class);
}
