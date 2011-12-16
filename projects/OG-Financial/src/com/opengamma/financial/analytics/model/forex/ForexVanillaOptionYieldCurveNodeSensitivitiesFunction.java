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
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexVanillaOptionYieldCurveNodeSensitivitiesFunction extends ForexVanillaOptionFunction {
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();

  public ForexVanillaOptionYieldCurveNodeSensitivitiesFunction(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName) {
    super(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final Currency putCurrency = ((FXOptionSecurity) target.getSecurity()).getPutCurrency();
    final Currency callCurrency = ((FXOptionSecurity) target.getSecurity()).getCallCurrency();
    final String putFundingCurveName = getPutFundingCurveName() + "_" + putCurrency.getCode();
    final String putForwardCurveName = getPutForwardCurveName() + "_" + putCurrency.getCode();
    final String callFundingCurveName = getCallFundingCurveName() + "_" + callCurrency.getCode();
    final String callForwardCurveName = getCallForwardCurveName() + "_" + callCurrency.getCode();
    final Object putJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(putCurrency, getPutForwardCurveName(), getPutFundingCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (putJacobianObject == null) {
      throw new OpenGammaRuntimeException("Put currency curve Jacobian was null");
    }
    final Object callJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(callCurrency, getCallForwardCurveName(), getCallFundingCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (callJacobianObject == null) {
      throw new OpenGammaRuntimeException("Call currency curve Jacobian was null");
    }
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Curve sensitivities were null");
    }
    final Object putCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, getPutForwardCurveName(), getPutFundingCurveName()));
    if (putCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Put curve coupon sensitivities were null");
    }
    final Object callCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, getCallForwardCurveName(), getCallFundingCurveName()));
    if (callCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Call curve coupon sensitivities were null");
    }
    final Object putFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(putCurrency, getPutFundingCurveName()));
    if (putFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Put funding curve specification was null");
    }
    final Object putForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(putCurrency, getPutForwardCurveName()));
    if (putForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Put forward curve specification was null");
    }
    final Object callFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(callCurrency, getCallFundingCurveName()));
    if (callFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Call funding curve specification was null");
    }
    final Object callForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(callCurrency, getCallForwardCurveName()));
    if (callForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Call forward curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities putFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) putFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities putForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) putForwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities callFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) callFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities callForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) callForwardCurveSpecObject;
    final DoubleMatrix2D putJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(putJacobianObject));
    final DoubleMatrix2D callJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(callJacobianObject));
    final YieldAndDiscountCurve putFundingCurve = data.getCurve(putFundingCurveName);
    final YieldAndDiscountCurve putForwardCurve = data.getCurve(putForwardCurveName);
    final YieldAndDiscountCurve callFundingCurve = data.getCurve(callFundingCurveName);
    final YieldAndDiscountCurve callForwardCurve = data.getCurve(callForwardCurveName);
    final DoubleMatrix1D putCouponSensitivity = (DoubleMatrix1D) putCouponSensitivitiesObject;
    final DoubleMatrix1D callCouponSensitivity = (DoubleMatrix1D) callCouponSensitivitiesObject;
    final YieldCurveBundle putCurveBundle = new YieldCurveBundle(new String[] {putFundingCurveName, putForwardCurveName}, new YieldAndDiscountCurve[] {putFundingCurve, putForwardCurve});
    final YieldCurveBundle callCurveBundle = new YieldCurveBundle(new String[] {callFundingCurveName, callForwardCurveName}, new YieldAndDiscountCurve[] {callFundingCurve, callForwardCurve});
    final Map<String, List<DoublesPair>> curveSensitivities = ((InterestRateCurveSensitivity) curveSensitivitiesObject).getSensitivities();
    final Map<String, DoubleMatrix1D> putArrayResult, callArrayResult;
    putArrayResult = CALCULATOR.calculate(curveSensitivities, putCurveBundle, putCouponSensitivity, putJacobian);
    callArrayResult = CALCULATOR.calculate(curveSensitivities, callCurveBundle, callCouponSensitivity, callJacobian);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(putFundingCurveName, data, putArrayResult.get(putFundingCurveName), putFundingCurveSpec,
        getResultSpecForCurve(target, putCurrency.getCode(), getPutFundingCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(putForwardCurveName, data, putArrayResult.get(putForwardCurveName), putForwardCurveSpec,
        getResultSpecForCurve(target, putCurrency.getCode(), getPutForwardCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(callFundingCurveName, data, callArrayResult.get(callFundingCurveName), callFundingCurveSpec,
        getResultSpecForCurve(target, callCurrency.getCode(), getCallFundingCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(callForwardCurveName, data, callArrayResult.get(callForwardCurveName), callForwardCurveSpec,
        getResultSpecForCurve(target, callCurrency.getCode(), getCallForwardCurveName())));
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final String putFundingCurveName = getPutFundingCurveName();
    final String putForwardCurveName = getPutForwardCurveName();
    final String callFundingCurveName = getCallFundingCurveName();
    final String callForwardCurveName = getCallForwardCurveName();
    final String surfaceName = getSurfaceName();
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    final ExternalId spotIdentifier = FXUtils.getSpotIdentifier(fxOption, true);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final ExternalId inverseSpotIdentifier = FXUtils.getSpotIdentifier(fxOption, true);
    final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    final ValueRequirement fxVolatilitySurface = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    result.add(YieldCurveFunction.getCurveRequirement(putCurrency, putFundingCurveName, putForwardCurveName, putFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(putCurrency, putForwardCurveName, putForwardCurveName, putFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(callCurrency, callFundingCurveName, callForwardCurveName, callFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(callCurrency, callForwardCurveName, callForwardCurveName, callFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(putCurrency, putForwardCurveName, putFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(callCurrency, callForwardCurveName, callFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, putForwardCurveName, putFundingCurveName));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, callForwardCurveName, callFundingCurveName));
    result.add(getCurveSensitivitiesRequirement(target));
    result.add(spotRequirement);
    result.add(inverseSpotRequirement);
    result.add(fxVolatilitySurface);
    result.add(getCurveSpecRequirement(putCurrency, putFundingCurveName));
    result.add(getCurveSpecRequirement(putCurrency, putForwardCurveName));
    result.add(getCurveSpecRequirement(callCurrency, callFundingCurveName));
    result.add(getCurveSpecRequirement(callCurrency, callForwardCurveName));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    return Sets.newHashSet(getResultSpecForCurve(target, putCurrency.getCode(), getPutFundingCurveName()), getResultSpecForCurve(target, putCurrency.getCode(), getPutForwardCurveName()),
        getResultSpecForCurve(target, callCurrency.getCode(), getCallFundingCurveName()), getResultSpecForCurve(target, callCurrency.getCode(), getCallForwardCurveName()));
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName(), getPutForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName(), getCallForwardCurveName()).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURRENCY, currency)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
