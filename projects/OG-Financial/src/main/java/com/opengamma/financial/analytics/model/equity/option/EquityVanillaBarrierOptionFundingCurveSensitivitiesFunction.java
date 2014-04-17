/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This Function provides the sensitivity to the discount rate. <p>
 * We have two dates of interest, expiry and settlement.
 * Sensitivity to the expiry rate might be implicit in the estimation of the underlying equity's forward, but we don't include this here.
 * The sensitivity to settlement rate is in the discounting, the ZeroBond price: PV = Z(t,S) * C(F,K,sig,T) <p>
 * We use chain rule to distribute closed-form model sensitivity across the curve
 */
public class EquityVanillaBarrierOptionFundingCurveSensitivitiesFunction extends EquityVanillaBarrierOptionBlackFunction {
  /** The present value calculator */
  private static final EquityOptionBlackPresentValueCalculator PV_CALCULATOR = EquityOptionBlackPresentValueCalculator.getInstance();

  /**
   * Default constructor
   */
  public EquityVanillaBarrierOptionFundingCurveSensitivitiesFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    // Get Funding Curve Name
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final String fundingCurveName = fundingCurves.iterator().next();
    requirements.add(getCurveSpecRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), fundingCurveName));
    return requirements;
  }

  // Need to do this to get labels for the output
  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object fundingObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (fundingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Funding Curve");
    }
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingObject;
    // Put curve into a bundle
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve(fundingCurveName, fundingCurve);
    if (!(fundingCurve instanceof YieldCurve)) {
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }

    // 4. Compute sensitivity to discount rate, then distribute across curve's nodes
    final DoubleMatrix1D sensVector;
    if (((YieldCurve) fundingCurve).getCurve() instanceof InterpolatedDoublesCurve) {

      // Compute the sum of the underlying vanillas' present values
      double pv = 0.0;
      for (final EquityIndexOption derivative : vanillaOptions) {
        pv += PV_CALCULATOR.visitEquityIndexOption(derivative, market);
      }
      final double settlementTime = vanillaOptions.iterator().next().getTimeToSettlement(); // All share the same dates
      if (settlementTime < 0.0) {
        throw new OpenGammaRuntimeException("EquityBarrierOptionSecurity has already settled.");
      }
      final double rhoSettle = -1 * settlementTime * pv;
      //  We use PresentValueNodeSensitivityCalculator to distribute this risk across the curve
      final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
      // What's left is to package up the inputs to the distributor, a YieldCurveBundle and a Map of Sensitivities
      final Map<String, List<DoublesPair>> curveSensMap = new HashMap<>();
      curveSensMap.put(fundingCurveName, Lists.newArrayList(DoublesPair.of(settlementTime, rhoSettle)));
      sensVector = distributor.curveToNodeSensitivities(curveSensMap, curveBundle);
    } else {
      throw new IllegalArgumentException("YieldCurveNodeSensitivities currently available only for Funding Curve backed by a InterpolatedDoublesCurve");
    }
    // Build up InstrumentLabelledSensitivities for the Curve
    final Object curveSpecObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;

    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, curveBundle, sensVector, curveSpec, resultSpec);
  }
}
