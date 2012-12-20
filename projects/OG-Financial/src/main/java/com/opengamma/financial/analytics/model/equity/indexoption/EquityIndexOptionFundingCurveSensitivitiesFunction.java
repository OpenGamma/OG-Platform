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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
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
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 *
 */
public class EquityIndexOptionFundingCurveSensitivitiesFunction extends EquityIndexOptionFunction {

  public EquityIndexOptionFundingCurveSensitivitiesFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = super.getRequirements(context, target, desiredValue);
    if (result == null) {
      return null;
    }
    // Get Funding Curve Name
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final String fundingCurveName = Iterables.getOnlyElement(fundingCurves);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    result.add(getCurveSpecRequirement(currency, fundingCurveName));
    return result;
  }

  // Need to do this to get labels for the output
  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityIndexOptionSecurity security = getEquityIndexOptionSecurity(target);
    final EquityIndexOptionDefinition defn = getConverter().visitEquityIndexOptionSecurity(security);
    final EquityIndexOption derivative = (EquityIndexOption) defn.toDerivative(now);
    if (derivative.getTimeToSettlement() < 0.0) {
      throw new OpenGammaRuntimeException("EquityIndexOption with expiry, " + security.getExpiry().getExpiry().toString() + ", has already settled.");
    }

    // 2. Build up the market data bundle

    final StaticReplicationDataBundle market = buildMarketBundle(security.getUnderlyingId(), executionContext, inputs, target, desiredValues);

    // Unpack the curve we're bumping
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Object fundingObject = inputs.getValue(getDiscountCurveRequirement(fundingCurveName, curveConfigName, security));
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

    // 3. Perform the calculation - what we came here to do

    final DoubleMatrix1D sensVector;
    if (((YieldCurve) fundingCurve).getCurve() instanceof InterpolatedDoublesCurve) {
      // We can use chain rule to distribute closed-form model sensitivity across the curve
      // We have two dates of interest, expiry and settlement
      // Sensitivity to the rate to expiry might be used to estimate the underlying's forward, but we don't include this here.
      // The sensitivity to settlement rate is in the discounting, the ZeroBond price: PV = Z(t,S) * C(F,K,sig,T)
      final double settle = derivative.getTimeToSettlement();
      final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
      final double rhoSettle = -1 * settle * model.presentValue(derivative, market);
      //  We use PresentValueNodeSensitivityCalculator to distribute this risk across the curve
      final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
      // What's left is to package up the inputs to the distributor, a YieldCurveBundle and a Map of Sensitivities
      final Map<String, List<DoublesPair>> curveSensMap = new HashMap<String, List<DoublesPair>>();
      curveSensMap.put(fundingCurveName, Lists.newArrayList(new DoublesPair(settle, rhoSettle)));
      sensVector = distributor.curveToNodeSensitivities(curveSensMap, curveBundle);

    } else {
      throw new IllegalArgumentException("YieldCurveNodeSensitivities currently available only for Funding Curve backed by a InterpolatedDoublesCurve");
    }
    // 4. Create Result's Specification that matches the properties promised and Return

    // Build up InstrumentLabelledSensitivities for the Curve
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(security.getCurrency(), fundingCurveName));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(),
        createValueProperties(target, desiredValue, executionContext).get());

    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, curveBundle, sensVector, curveSpec, resultSpec);
  }

  @Override
  protected Object computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market) {
    return null;
  }

}
