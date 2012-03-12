/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexOptionYieldCurveNodeSensitivitiesFunction extends ForexOptionFunction {
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final String putCurrencyString = putCurrency.getCode();
    final String callCurrencyString = callCurrency.getCode();
    final String fullPutFundingCurveName = putFundingCurveName + "_" + putCurrencyString;
    final String fullPutForwardCurveName = putForwardCurveName + "_" + putCurrencyString;
    final String fullCallFundingCurveName = callFundingCurveName + "_" + callCurrencyString;
    final String fullCallForwardCurveName = callForwardCurveName + "_" + callCurrencyString;
    final Object putJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(putCurrency, putForwardCurveName, putFundingCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (putJacobianObject == null) {
      throw new OpenGammaRuntimeException("Put currency curve Jacobian was null");
    }
    final Object callJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(callCurrency, callForwardCurveName, callFundingCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (callJacobianObject == null) {
      throw new OpenGammaRuntimeException("Call currency curve Jacobian was null");
    }
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target, putFundingCurveName, putForwardCurveName, callFundingCurveName,
        callForwardCurveName, surfaceName));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Curve sensitivities were null");
    }
    final Object putCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, putForwardCurveName, putFundingCurveName));
    if (putCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Put curve coupon sensitivities were null");
    }
    final Object callCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, callForwardCurveName, callFundingCurveName));
    if (callCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Call curve coupon sensitivities were null");
    }
    final Object putFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(putCurrency, putFundingCurveName));
    if (putFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Put funding curve specification was null");
    }
    final Object putForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(putCurrency, putForwardCurveName));
    if (putForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Put forward curve specification was null");
    }
    final Object callFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(callCurrency, callFundingCurveName));
    if (callFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Call funding curve specification was null");
    }
    final Object callForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(callCurrency, callForwardCurveName));
    if (callForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Call forward curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities putFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) putFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities putForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) putForwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities callFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) callFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities callForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) callForwardCurveSpecObject;
    final DoubleMatrix2D putJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(putJacobianObject));
    final DoubleMatrix2D callJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(callJacobianObject));
    final YieldAndDiscountCurve putFundingCurve = data.getCurve(fullPutFundingCurveName);
    final YieldAndDiscountCurve putForwardCurve = data.getCurve(fullPutForwardCurveName);
    final YieldAndDiscountCurve callFundingCurve = data.getCurve(fullCallFundingCurveName);
    final YieldAndDiscountCurve callForwardCurve = data.getCurve(fullCallForwardCurveName);
    final DoubleMatrix1D putCouponSensitivity = (DoubleMatrix1D) putCouponSensitivitiesObject;
    final DoubleMatrix1D callCouponSensitivity = (DoubleMatrix1D) callCouponSensitivitiesObject;
    final YieldCurveBundle putCurveBundle = new YieldCurveBundle(new String[] {fullPutFundingCurveName, fullPutForwardCurveName}, new YieldAndDiscountCurve[] {putFundingCurve, putForwardCurve});
    final YieldCurveBundle callCurveBundle = new YieldCurveBundle(new String[] {fullCallFundingCurveName, fullCallForwardCurveName}, new YieldAndDiscountCurve[] {callFundingCurve, callForwardCurve});
    final Map<String, List<DoublesPair>> curveSensitivities = ((InterestRateCurveSensitivity) curveSensitivitiesObject).getSensitivities();
    final Map<String, DoubleMatrix1D> putArrayResult, callArrayResult;
    putArrayResult = CALCULATOR.calculate(curveSensitivities, putCurveBundle, putCouponSensitivity, putJacobian);
    callArrayResult = CALCULATOR.calculate(curveSensitivities, callCurveBundle, callCouponSensitivity, callJacobian);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(fullPutFundingCurveName, data, putArrayResult.get(fullPutFundingCurveName), putFundingCurveSpec,
        getResultSpecForCurve(target, putCurrencyString, putFundingCurveName, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName)));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(fullPutForwardCurveName, data, putArrayResult.get(fullPutForwardCurveName), putForwardCurveSpec,
        getResultSpecForCurve(target, putCurrencyString, putForwardCurveName, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName)));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(fullCallFundingCurveName, data, callArrayResult.get(fullCallFundingCurveName), callFundingCurveSpec,
        getResultSpecForCurve(target, callCurrencyString, callFundingCurveName, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName)));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(fullCallForwardCurveName, data, callArrayResult.get(fullCallForwardCurveName), callForwardCurveSpec,
        getResultSpecForCurve(target, callCurrencyString, callForwardCurveName, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName)));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    final ValueProperties putProperties = getResultProperties(putCurrency).get();
    final ValueSpecification putResultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), putProperties);
    final ValueProperties callProperties = getResultProperties(callCurrency).get();
    final ValueSpecification callResultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), callProperties);
    return Sets.newHashSet(putResultSpec, putResultSpec, callResultSpec, callResultSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Set<String> putFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PUT_FUNDING_CURVE_NAME);
    if (putFundingCurveNames == null || putFundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PUT_FORWARD_CURVE_NAME);
    if (putForwardCurveNames == null || putForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_CALL_FUNDING_CURVE_NAME);
    if (callFundingCurveNames == null || callFundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_CALL_FORWARD_CURVE_NAME);
    if (callForwardCurveNames == null || callForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String putFundingCurveName = putFundingCurveNames.iterator().next();
    final String putForwardCurveName = putForwardCurveNames.iterator().next();
    final String callFundingCurveName = callFundingCurveNames.iterator().next();
    final String callForwardCurveName = callForwardCurveNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ExternalId spotIdentifier = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final ExternalId inverseSpotIdentifier = security.accept(ForexVisitors.getInverseSpotIdentifierVisitor());
    final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency);
    final ValueRequirement putFundingCurve = getCurveRequirement(putFundingCurveName, PROPERTY_PUT_FUNDING_CURVE_NAME, putCurrency);
    final ValueRequirement putForwardCurve = getCurveRequirement(putForwardCurveName, PROPERTY_PUT_FORWARD_CURVE_NAME, putCurrency);
    final ValueRequirement callFundingCurve = getCurveRequirement(callFundingCurveName, PROPERTY_CALL_FUNDING_CURVE_NAME, callCurrency);
    final ValueRequirement callForwardCurve = getCurveRequirement(callForwardCurveName, PROPERTY_CALL_FORWARD_CURVE_NAME, callCurrency);
    final ValueRequirement putJacobian =
        YieldCurveFunction.getJacobianRequirement(putCurrency, putForwardCurveName, putFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    final ValueRequirement callJacobian =
        YieldCurveFunction.getJacobianRequirement(callCurrency, callForwardCurveName, callFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    final ValueRequirement putCouponSensitivity = YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, putForwardCurveName, putFundingCurveName);
    final ValueRequirement callCouponSensitivity = YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, callForwardCurveName, callFundingCurveName);
    final ValueRequirement curveSensitivities = getCurveSensitivitiesRequirement(target, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName,
        surfaceName);
    final ValueRequirement putFundingCurveSpec = getCurveSpecRequirement(putCurrency, putFundingCurveName);
    final ValueRequirement putForwardCurveSpec = getCurveSpecRequirement(putCurrency, putForwardCurveName);
    final ValueRequirement callFundingCurveSpec = getCurveSpecRequirement(callCurrency, callFundingCurveName);
    final ValueRequirement callForwardCurveSpec = getCurveSpecRequirement(callCurrency, callForwardCurveName);
    return Sets.newHashSet(putFundingCurve, callFundingCurve, putForwardCurve, callForwardCurve, fxVolatilitySurface, spotRequirement, inverseSpotRequirement,
        putJacobian, callJacobian, putCouponSensitivity, callCouponSensitivity, curveSensitivities, putFundingCurveSpec, putForwardCurveSpec,
        callFundingCurveSpec, callForwardCurveSpec);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    String putFundingCurveName = null;
    String putForwardCurveName = null;
    String callFundingCurveName = null;
    String callForwardCurveName = null;
    String surfaceName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (input.getValue().getValueName().equals(ValueRequirementNames.FX_CURVE_SENSITIVITIES)) {
        putFundingCurveName = input.getKey().getProperty(PROPERTY_PUT_FUNDING_CURVE_NAME);
        putForwardCurveName = input.getKey().getProperty(PROPERTY_PUT_FORWARD_CURVE_NAME);
        callFundingCurveName = input.getKey().getProperty(PROPERTY_CALL_FUNDING_CURVE_NAME);
        callForwardCurveName = input.getKey().getProperty(PROPERTY_CALL_FORWARD_CURVE_NAME);
        surfaceName = input.getKey().getProperty(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
        break;
      }
    }
    assert putFundingCurveName != null;
    assert putForwardCurveName != null;
    assert callFundingCurveName != null;
    assert callForwardCurveName != null;
    assert surfaceName != null;
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    final ValueSpecification putFundingResult = getResultSpecForCurve(target, putCurrency, putFundingCurveName);
    final ValueSpecification putForwardResult = getResultSpecForCurve(target, putCurrency, putForwardCurveName);
    final ValueSpecification callFundingResult = getResultSpecForCurve(target, callCurrency, callFundingCurveName);
    final ValueSpecification callForwardResult = getResultSpecForCurve(target, callCurrency, callForwardCurveName);
    return Sets.newHashSet(putFundingResult, putForwardResult, callFundingResult, callForwardResult);
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  }

  private ValueProperties.Builder getResultProperties(final String ccy) {
    return createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy)
        .with(ValuePropertyNames.CURRENCY, ccy)
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
  }

  private ValueProperties.Builder getResultProperties(final String ccy, final String curveName) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy)
        .with(ValuePropertyNames.CURRENCY, ccy)
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
  }

  private ValueProperties.Builder getResultProperties(final String ccy, final String curveName, final String putFundingCurveName, final String putForwardCurveName,
      final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy)
        .with(ValuePropertyNames.CURRENCY, ccy)
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName);
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target, final String putFundingCurveName, final String putForwardCurveName,
      final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target)).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName) {
    final ValueProperties properties = getResultProperties(currency, curveName).get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
  }

  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName,
      final String surfaceName) {
    final ValueProperties properties = getResultProperties(currency, curveName, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName).get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
  }
}
