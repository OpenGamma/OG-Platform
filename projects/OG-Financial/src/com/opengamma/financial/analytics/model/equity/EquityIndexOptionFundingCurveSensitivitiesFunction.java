/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.interestrate.NodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

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
    // Get Funding Curve Name
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final String fundingCurveName = fundingCurves.iterator().next();
    result.add(getCurveSpecRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), fundingCurveName));
    return result;
  }

  // Need to do this to get labels for the output
  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }


  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityIndexOptionSecurity security = (EquityIndexOptionSecurity) target.getSecurity();
    final EquityIndexOptionDefinition defn = getConverter().visitEquityIndexOptionSecurity(security);
    final EquityIndexOption derivative = (EquityIndexOption) defn.toDerivative(now);

    // 2. Build up the market data bundle
    final ValueRequirement desiredValue = desiredValues.iterator().next();


    // a. The Vol Surface

    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    HistoricalTimeSeriesSource tsSource = getTimeSeriesSource(executionContext);
    final Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(tsSource, security, volSurfaceName));
    if (volSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final VolatilitySurface volSurface = (VolatilitySurface) volSurfaceObject;
    //TODO no choice of other surfaces
    final BlackVolatilitySurface<?> blackVolSurf = new BlackVolatilitySurfaceStrike(volSurface.getSurface()); // TODO This doesn't need to be like this anymore

    // b. The Funding Curve
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final Object fundingObject = inputs.getValue(getDiscountCurveRequirement(security, fundingCurveName));
    if (fundingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Funding Curve");
    }
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingObject;
    // Put curve into a bundle
    YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve(fundingCurveName, fundingCurve);

    // c. The Spot Index
    final Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    final ForwardCurve forwardCurve = new ForwardCurve(spot, fundingCurve.getCurve());
    final EquityOptionDataBundle market = new EquityOptionDataBundle(blackVolSurf, fundingCurve, forwardCurve);

    // 3. Perform the calculation - what we came here to do
    final DoubleMatrix1D sensVector;

    if (fundingCurve.getCurve() instanceof InterpolatedDoublesCurve) {
      // We can use chain rule to distribute closed-form model sensitivity across the curve
      // We have two dates of interest, expiry and settlement
      // Sensitivity to the rate to expiry might be used to estimate the underlying's forward, but we don't include this here.
      // The sensitivity to settlement rate is in the discounting, the ZeroBond price: PV = Z(t,S) * C(F,K,sig,T)
      final double settle = derivative.getTimeToSettlement();
      EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
      final double rhoSettle = -1 * settle * model.presentValue(derivative, market);
      //  We use PresentValueNodeSensitivityCalculator to distribute this risk across the curve
      final NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
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

    ValueProperties resultProps = getValueProperties(fundingCurveName, volSurfaceName);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), resultProps);

    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, curveBundle, sensVector, curveSpec, resultSpec);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    return null;
  }

}
