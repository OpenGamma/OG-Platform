/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturesPricer;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.simpleinstruments.definition.SimpleFutureDefinition;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.SimpleFutureConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class EquityFuturesYieldCurveNodeSensitivityFunction extends EquityFuturesFunction {

  private static final SimpleFutureConverter CONVERTER = new SimpleFutureConverter();

  public EquityFuturesYieldCurveNodeSensitivityFunction(String pricingMethodName) {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, pricingMethodName);
  }

  // Need to do this to get labels for the output
  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    // Get Funding Curve Name and Configuration
    final String fundingCurveName = getFundingCurveName(desiredValue);
    if (fundingCurveName == null) {
      return null;
    }
    final String curveConfigName = getCurveConfigName(desiredValue);
    if (curveConfigName == null) {
      return null;
    }
    // Add Funding Curve Requirement, which may not have been in super's requirements
    final FutureSecurity security = (FutureSecurity)  target.getTrade().getSecurity();
    requirements.add(getDiscountCurveRequirement(fundingCurveName, curveConfigName, security));
    // Add Funding Curve Spec, to get labels correct in result
    requirements.add(getCurveSpecRequirement(FinancialSecurityUtils.getCurrency(security), fundingCurveName));
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    // 1. Build the analytic derivative to be priced
    final Trade trade = target.getTrade();
    final FutureSecurity security = (FutureSecurity) trade.getSecurity();

    // TODO: Clean up
    // lastMarginPrice is unnecessary, but the timeSeriesBundle would be for DIVIDEND_YIELD if we are estimating the Futures Price instead of taking its marked price
    final HistoricalTimeSeriesBundle timeSeriesBundle = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    //final Double lastMarginPrice = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
    //final Double lastMarginPrice = 0.0;
    //final EquityFutureDefinition definition = CONVERTER.visitEquityFutureTrade(trade, lastMarginPrice);
    final SimpleFutureDefinition simpleDefn = (SimpleFutureDefinition) security.accept(CONVERTER);
    // TODO: Refactor and hence remove the following line
    final EquityFutureDefinition definition = new EquityFutureDefinition(simpleDefn.getExpiry(), simpleDefn.getSettlementDate(), simpleDefn.getReferencePrice(), simpleDefn.getCurrency(), simpleDefn.getUnitAmount());
    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();
    final EquityFuture derivative = definition.toDerivative(valuationTime);

    // 2. Build up the market data bundle
    final SimpleFutureDataBundle market = getFutureDataBundle(security, inputs, timeSeriesBundle, desiredValues.iterator().next());

    // 3. Create specification that matches the properties promised in getResults
    // For the curve we're bumping, create a bundle
    YieldCurveBundle curveBundle = new YieldCurveBundle();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String fundingCurveName = getFundingCurveName(desiredValue);
    final String curveConfigName = getCurveConfigName(desiredValue);
    final YieldAndDiscountCurve fundingCurve = getYieldCurve(security, inputs, fundingCurveName, curveConfigName);
    curveBundle.setCurve(fundingCurveName, fundingCurve);
    // Build up InstrumentLabelledSensitivities for the Curve
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(security.getCurrency(), fundingCurveName));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(),
        createValueProperties(target, fundingCurveName, curveConfigName, getPricingMethodName()).get());

    // 4. Compute sensitivity to the discount rate, then use chain rule to distribute sensitivity across the curve
    final DoubleMatrix1D sensVector;
    if (((YieldCurve) fundingCurve).getCurve() instanceof InterpolatedDoublesCurve) {
      final double settle = derivative.getTimeToSettlement();
      EquityFuturesPricer pricer = EquityFuturePricerFactory.getMethod(getPricingMethodName());
      final double rhoSettle = -1 * settle * pricer.presentValue(derivative, market);
      //  We use PresentValueNodeSensitivityCalculator to distribute this risk across the curve
      final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
      final Map<String, List<DoublesPair>> curveSensMap = new HashMap<String, List<DoublesPair>>();
      curveSensMap.put(fundingCurveName, Lists.newArrayList(new DoublesPair(settle, rhoSettle)));
      sensVector = distributor.curveToNodeSensitivities(curveSensMap, curveBundle);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, curveBundle, sensVector, curveSpec, resultSpec);
    } else {
      throw new IllegalArgumentException("YieldCurveNodeSensitivities currently available only for Funding Curve backed by a InterpolatedDoublesCurve");
    }
  }
}
