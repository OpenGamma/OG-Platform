/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * CDSPresentValueFunction currently contains initial work on CDS model only
 * 
 * @author Martin Traverse
 * @see CDSSecurity
 */
public class CDSPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof CDSSecurity) {
      return true;
    }
    return false;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      
      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      final ExternalIdBundle bundle = ExternalIdBundle.of(cds.getUnderlying());
      final BondSecurity bond = (BondSecurity) context.getSecuritySource().getSecurity(bundle);
      
      if (bond == null) {
        ; // TODO: handle error
      }
      
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      
      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(), 
        ValueProperties
          .with("Curve", "SECONDARY")                  // "RiskFree" vs "Credit"
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with("CurveCalculationMethod", "ParRate")
          .get()
      ));

      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        bond.getCurrency().getUniqueId(), 
        ValueProperties
          .with(ValuePropertyNames.CURVE, "SECONDARY")
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate")
          .get()
      ));
      
      return requirements;
    }
    return null;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      final ValueSpecification pvSpec = new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .get()
        ),
        getUniqueId()
      );
      return Collections.<ValueSpecification>singleton(pvSpec);
    }
    return null;
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final ExternalIdBundle bundle = ExternalIdBundle.of(cds.getUnderlying());
    final BondSecurity bond = (BondSecurity) executionContext.getSecuritySource().getSecurity(bundle);
    
    YieldCurve yieldCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE,
      ComputationTargetType.PRIMITIVE,
      cds.getCurrency().getUniqueId(),
      ValueProperties
        .with("Curve", "SECONDARY")
        .with("FundingCurve", "SECONDARY")
        .with("ForwardCurve", "SECONDARY")
        .with("CurveCalculationMethod", "ParRate")
        .get()
    ));
    
    YieldCurve bondYieldCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        bond.getCurrency().getUniqueId(),
        ValueProperties
          .with("Curve", "SECONDARY")
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with("CurveCalculationMethod", "ParRate")
          .get()
    ));

    final ComputedValue marketPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .get()
        ),
        getUniqueId()
      ),
      yieldCurve.getInterestRate(0.25) * bond.getCouponRate() * cds.getNotional()
    );
    
    return Collections.<ComputedValue>singleton(marketPriceValue);
  }


}
