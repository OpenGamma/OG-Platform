/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
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
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This Function provides the sensitivity to the discount rate. <p>
 * We have two dates of interest, expiry and settlement.
 * Sensitivity to the expiry rate might be implicit in the estimation of the underlying equity's forward, but we don't include this here.
 * The sensitivity to settlement rate is in the discounting, the ZeroBond price: PV = Z(t,S) * C(F,K,sig,T) <p>
 * We use chain rule to distribute closed-form model sensitivity across the curve
 */
public class EquityIndexVanillaBarrierOptionFundingCurveSensitivitiesFunction extends EquityIndexVanillaBarrierOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator PV_CALCULATOR = EquityIndexOptionPresentValueCalculator.getInstance();

  public EquityIndexVanillaBarrierOptionFundingCurveSensitivitiesFunction() {
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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {

    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityBarrierOptionSecurity barrierSec = getEquityBarrierOptionSecurity(target);
    final ExternalId underlyingId = barrierSec.getUnderlyingId();
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    // 1. Get parameters for the smoothing of binary payoffs into put spreads
    final String strOH = desiredValue.getConstraint(ValuePropertyNames.BINARY_OVERHEDGE);
    if (strOH == null) {
      throw new OpenGammaRuntimeException("Could not find: " + ValuePropertyNames.BINARY_OVERHEDGE);
    }
    final Double overhedge = Double.parseDouble(strOH);

    final String strSmooth = desiredValue.getConstraint(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    if (strSmooth == null) {
      throw new OpenGammaRuntimeException("Could not find: " + ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    }
    final Double smoothing = Double.parseDouble(strSmooth);

    // 2. Break the barrier security into it's vanilla analytic derivatives
    final Set<EquityIndexOption> vanillas = vanillaDecomposition(now, barrierSec, smoothing, overhedge);
    final double settlementTime = vanillas.iterator().next().getTimeToSettlement(); // All share the same dates
    if (settlementTime < 0.0) {
      throw new OpenGammaRuntimeException("EquityBarrierOptionSecurity with expiry, " + barrierSec.getExpiry().getExpiry().toString() + ", has already settled.");
    }
    // 3. Build up the market data bundle
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // Unpack the curve we're bumping
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Object fundingObject = inputs.getValue(getDiscountCurveRequirement(fundingCurveName, curveConfigName, barrierSec));
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
      for (final EquityIndexOption derivative : vanillas) {
        pv += PV_CALCULATOR.visitEquityIndexOption(derivative, market);
      }
      final double rhoSettle = -1 * settlementTime * pv;
      //  We use PresentValueNodeSensitivityCalculator to distribute this risk across the curve
      final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
      // What's left is to package up the inputs to the distributor, a YieldCurveBundle and a Map of Sensitivities
      final Map<String, List<DoublesPair>> curveSensMap = new HashMap<String, List<DoublesPair>>();
      curveSensMap.put(fundingCurveName, Lists.newArrayList(new DoublesPair(settlementTime, rhoSettle)));
      sensVector = distributor.curveToNodeSensitivities(curveSensMap, curveBundle);
    } else {
      throw new IllegalArgumentException("YieldCurveNodeSensitivities currently available only for Funding Curve backed by a InterpolatedDoublesCurve");
    }
    // 5. Create Result's Specification that matches the properties promised and Return

    // Build up InstrumentLabelledSensitivities for the Curve
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(barrierSec.getCurrency(), fundingCurveName));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(),
        createValueProperties(target, desiredValue, executionContext).get());

    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, curveBundle, sensVector, curveSpec, resultSpec);
  }

  @Override
  protected Object computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market) {
    return null;
  }
}
