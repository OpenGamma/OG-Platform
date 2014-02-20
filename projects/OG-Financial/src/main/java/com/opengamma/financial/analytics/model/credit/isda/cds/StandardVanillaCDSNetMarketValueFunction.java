/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * Prototype - Reports {@link ValueRequirementNames#NET_MARKET_VALUE} 
 * as the sum of the swap's {@link ValueRequirementNames#NOTIONAL} and its {@link ValueRequirementNames#PRESENT_VALUE}
 */
public class StandardVanillaCDSNetMarketValueFunction extends StandardVanillaCDSFunction {

  public StandardVanillaCDSNetMarketValueFunction() {
    super(ValueRequirementNames.NET_MARKET_VALUE);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    final CreditDefaultSwapSecurity security = (CreditDefaultSwapSecurity) target.getSecurity();
    final double notionalAmt = security.getNotional().getAmount();
   
    final Object pvObj = inputs.getValue(ValueRequirementNames.PRESENT_VALUE);
    if (pvObj == null) {
      throw new OpenGammaRuntimeException("Missing Present Value requirement");
    }
    final double pv = (Double) pvObj;
    
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.NET_MARKET_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, notionalAmt + pv));
  }
  
  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), 
        desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION))); 
    return requirements; 
  }
  @Override
  protected Set<ComputedValue> getComputedValue(CreditDefaultSwapDefinition definition, ISDACompliantYieldCurve yieldCurve, ZonedDateTime[] times, double[] marketSpreads, ZonedDateTime valuationTime,
      ComputationTarget target, ValueProperties properties, FunctionInputs inputs, ISDACompliantCreditCurve hazardCurve, CDSAnalytic analytic, Tenor[] tenors) {
    return null;
  }

  @Override
  protected Builder getCommonResultProperties() {
    return createValueProperties();
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }

}
