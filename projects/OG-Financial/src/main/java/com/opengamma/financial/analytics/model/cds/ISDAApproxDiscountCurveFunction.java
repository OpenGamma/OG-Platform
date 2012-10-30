/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
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
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Build an ISDA-style discount curve for use with the ISDA CDS price functions.
 * 
 * Currently this is a simple map function that translates the existing OpenGamma curves
 * in to ISDA format. A more correct implementation would pull data from Markit and build
 * the curve replicating ISDA functionality.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPriceFunction
 * @see ISDAApproxCDSPriceFlatSpreadFunction
 */
public class ISDAApproxDiscountCurveFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE || target.getUniqueId() == null) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {    
    if (canApplyTo(context, target)) {
    
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      
      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, target.getUniqueId(),
        ValueProperties
          .with("Curve", "SECONDARY")
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with("CurveCalculationMethod", "ParRate")
          .get()));
      
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {

    Set<ValueSpecification> results = new HashSet<ValueSpecification>();

    results.add(new ValueSpecification(
      new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, target.getUniqueId(),
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get()),
      getUniqueId()));
    
    return results;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    final YieldCurve sourceCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, target.getUniqueId(),
      ValueProperties.withAny(ValuePropertyNames.CURVE).get()));
    
    if (sourceCurve == null) {
      throw new OpenGammaRuntimeException("Could not get source discount curve to translate for ISDA");
    }
    
    final Curve<Double, Double> curveData = sourceCurve.getCurve();
    final ISDACurve isdaCurve = ISDACurve.fromBoxed(sourceCurve.getName(), curveData.getXData(), curveData.getYData(), 0.0);
    
    ComputedValue result = new ComputedValue(
      new ValueSpecification(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, target.getUniqueId(),
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get()),
        getUniqueId()),
      isdaCurve);
    
    return Collections.singleton(result);
  }

}
