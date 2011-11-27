/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.VarianceSwapRatesSensitivityCalculator;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class EquityVarianceSwapYieldCurveNodeSensitivityFunction extends EquityVarianceSwapFunction {
  private static final VarianceSwapRatesSensitivityCalculator CALCULATOR = VarianceSwapRatesSensitivityCalculator.getInstance();
  
  public EquityVarianceSwapYieldCurveNodeSensitivityFunction(String curveDefinitionName, String surfaceDefinitionName, String forwardCalculationMethod, String strikeParameterizationMethodName) {
    super(curveDefinitionName, surfaceDefinitionName, forwardCalculationMethod, strikeParameterizationMethodName);
  }

  @Override
  protected Set<ComputedValue> getResults(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final VarianceSwapDataBundle market) {
    final DoubleMatrix1D sensitivities = CALCULATOR.calcDeltaBucketed(derivative, market);
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(derivative.getCurrency()));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final ValueSpecification resultSpec = getValueSpecification(target);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(market.getDiscountCurve(), sensitivities, curveSpec, resultSpec);
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = super.getRequirements(context, target, desiredValue);
    result.add(getCurveSpecRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity())));
    return result;
  }
  
  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, getCurveDefinitionName())
        .with(ValuePropertyNames.CURVE_CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, getCurveDefinitionName()).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

}
