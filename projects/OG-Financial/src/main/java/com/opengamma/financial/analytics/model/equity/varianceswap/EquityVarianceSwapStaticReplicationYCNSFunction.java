/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.VarianceSwapPresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
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
public class EquityVarianceSwapStaticReplicationYCNSFunction extends EquityVarianceSwapStaticReplicationFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityVarianceSwapStaticReplicationYCNSFunction.class);
  private static final EquityDerivativeSensitivityCalculator CALCULATOR = new EquityDerivativeSensitivityCalculator(VarianceSwapPresentValueCalculator.getInstance());

  public EquityVarianceSwapStaticReplicationYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> computeValues(final ValueSpecification resultSpec, final FunctionInputs inputs, final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    final DoubleMatrix1D sensitivities = CALCULATOR.calcDeltaBucketed(derivative, market);
    final String curveName = resultSpec.getProperty(ValuePropertyNames.CURVE);
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(derivative.getCurrency(), curveName));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final YieldCurveBundle curveMap = new YieldCurveBundle();
    curveMap.setCurve(curveName, market.getDiscountCurve());
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, curveMap, sensitivities, curveSpec, resultSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = super.getRequirements(context, target, desiredValue);
    if (result == null) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      s_logger.error("Must specify a curve name");
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curves);
    result.add(getCurveSpecRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName));
    return result;
  }

  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURVE_CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target, final String curveName, final String curveCalculationConfig, final String surfaceName) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURVE_CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

}
