/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.VarianceSwapSensitivityCalculator;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
 * In this formulation, Rates enter the pricing of a VarianceSwap in two places: in the discounting and forward projection.<p>
 * i.e. We are using the rates to infer the forward: spot / Z(t,T).
*/
public class EquityVarianceSwapYieldCurveNodeSensitivityFunction extends EquityVarianceSwapFunction {
  private static final VarianceSwapSensitivityCalculator CALCULATOR = VarianceSwapSensitivityCalculator.getInstance();

  public EquityVarianceSwapYieldCurveNodeSensitivityFunction(String curveDefinitionName, String surfaceDefinitionName, String forwardCalculationMethod) {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, curveDefinitionName, surfaceDefinitionName, forwardCalculationMethod);
  }

  @Override
  protected Set<ComputedValue> computeValues(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    final DoubleMatrix1D sensitivities = CALCULATOR.calcDeltaBucketed(derivative, market);
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(derivative.getCurrency()));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final ValueSpecification resultSpec = getValueSpecification(target);
    YieldCurveBundle curveMap = new YieldCurveBundle();
    curveMap.setCurve(getCurveDefinitionName(), market.getDiscountCurve());
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(getCurveDefinitionName(), curveMap, sensitivities, curveSpec, resultSpec);
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
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, getCurveDefinitionName()).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

}
