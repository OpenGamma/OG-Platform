/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Collections;
import java.util.Set;

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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.async.AsynchronousExecution;


/**
 * Extract a flat spread from the spread curve available in the market.
 * 
 * The spread curve to use is keyed by issuer, currency, seniority and restructuring clause
 * for the underlying bond. The point to extract is chosen based on the maturity of the
 * CDS being priced.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPriceFlatSpreadFunction
 */
public class ISDAApproxFlatSpreadFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CDS_SECURITY;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    
    /* TODO: Pull in CDS spread curve as an input

    CDSSecurity cds = (CDSSecurity) target.getSecurity();
    
    final ValueRequirement requirement = new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(),
        ValueProperties
          .with(ValuePropertyNames.CURVE, "CDS_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
          .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get());
    
    return Collections.singleton(requirement);
    */
    
    return Collections.emptySet();
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    // TODO: Are extra value properties needed here? (see ISDAApproxCDSPriceFlatSpreadFunction)
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), createValueProperties().get()));
  }

  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    /* TODO: Pull in the spread curve as an input
     * 
     * This is an example implementation only. If this approach were used, maturity should be computed using the same day count as the spread curve
     * In may be necessary to index points on the spread curve using real dates rather than t-values  
    
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final double maturity = TimeCalculator.getTimeBetween(ZonedDateTime.now(executionContext.getValuationClock()), cds.getMaturity());
    
    final YieldCurve spreadCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties
        .with(ValuePropertyNames.CURVE, "CDS_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
        .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get()));
    
    final Double[] times = spreadCurve.getCurve().getXData();
    final Double[] rates = spreadCurve.getCurve().getYData();
    
    int i = 0;
    while (i < times.length && times[i] < maturity) {
      ++i;
    }
    
    final double flatSpread = rates[i];
    
    */
    
    final double flatSpread = 0.01;
    
    final ComputedValue result = new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), createValueProperties().get()), flatSpread);
    
    return Collections.singleton(result);
  }

}
