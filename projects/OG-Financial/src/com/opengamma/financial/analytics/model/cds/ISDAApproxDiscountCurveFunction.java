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
import com.opengamma.util.async.AsynchronousExecution;

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
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {    
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();

    requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, target.toSpecification(),
        ValueProperties
            .with("Curve", "SECONDARY")
            .with("FundingCurve", "SECONDARY")
            .with("ForwardCurve", "SECONDARY")
            .with("CurveCalculationMethod", "ParRate")
            .get()));
    
    return requirements;
  }

  protected ValueProperties.Builder createValueProperties() {
    return super.createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    final YieldCurve sourceCurve = (YieldCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    
    if (sourceCurve == null) {
      throw new OpenGammaRuntimeException("Could not get source discount curve to translate for ISDA");
    }
    
    final Curve<Double, Double> curveData = sourceCurve.getCurve();
    final ISDACurve isdaCurve = ISDACurve.fromBoxed(sourceCurve.getName(), curveData.getXData(), curveData.getYData(), 0.0);
    
    ComputedValue result = new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), createValueProperties().get()), isdaCurve);
    
    return Collections.singleton(result);
  }

}
