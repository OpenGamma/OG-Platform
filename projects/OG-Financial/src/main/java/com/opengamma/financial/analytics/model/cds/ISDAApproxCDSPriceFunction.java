/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Base class for ISDA CDS pricing functions
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxHazardCurveFunction
 * @see ISDAApproxFlatSpreadFunction
 */
public abstract class ISDAApproxCDSPriceFunction extends NonCompiledInvoker {
  
  protected abstract String getHazardRateStructure();

  protected abstract DoublesPair executeImpl(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    
    // ISDA can price any CDS
    if (target.getSecurity() instanceof CDSSecurity) {
      return true;
    }
    
    return false;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      
      final ValueSpecification cleanPriceSpec = new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.CLEAN_PRICE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId());
      
      final ValueSpecification dirtyPriceSpec = new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId());
      
      final ValueSpecification presentValueSpec = new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId());
      
      Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(cleanPriceSpec);
      results.add(dirtyPriceSpec);
      results.add(presentValueSpec);
      
      return results;
    }
    return null;
  }
    
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    
    final DoublesPair calculationResult = executeImpl(executionContext, inputs, target, desiredValues);
    final Double cleanPrice = calculationResult.getFirst();
    final Double dirtyPrice = calculationResult.getSecond();
    
    // Pack up the results
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    
    final ComputedValue cleanPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.CLEAN_PRICE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId()),
      cleanPrice);
    
    final ComputedValue dirtyPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId()),
      dirtyPrice);
    
    final ComputedValue presentValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
            .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
            .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure())
            .get()),
        getUniqueId()),
      cleanPrice);
    
    results.add(cleanPriceValue);
    results.add(dirtyPriceValue);
    results.add(presentValue);
    
    return results;
  }
  
  protected ZonedDateTime findSettlementDate(final ZonedDateTime startDate, final Convention convention) {
    
    final DateAdjuster adjuster = convention.getBusinessDayConvention().getDateAdjuster(convention.getWorkingDayCalendar());
    
    ZonedDateTime result = startDate;
    
    for (int i = 0, n = convention.getSettlementDays(); i < n; ++i) {
      result = result.plusDays(1).with(adjuster);
    }
    
    return result;
  }

}
