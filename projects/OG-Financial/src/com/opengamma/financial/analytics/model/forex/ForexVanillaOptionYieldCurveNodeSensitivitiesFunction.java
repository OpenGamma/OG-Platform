/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.HashMap;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveLabelGenerator;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexYieldCurveNodeSensitivityCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
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
  private static final PresentValueForexYieldCurveNodeSensitivityCalculator CALCULATOR = PresentValueForexYieldCurveNodeSensitivityCalculator.getInstance();

  public ForexVanillaOptionYieldCurveNodeSensitivitiesFunction(final String putCurveName, final String receiveCurveName, final String surfaceName) {
    super(putCurveName, receiveCurveName, surfaceName, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative forex, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final ForexOptionVanilla fxOption = (ForexOptionVanilla) forex;
    final Forex fx = fxOption.getUnderlyingForex();
    final Currency putCurrency = fx.getCurrency1();
    final Currency callCurrency = fx.getCurrency2();
    final String putCurveName = getPutCurveName() + "_" + putCurrency.getCode();
    final String callCurveName = getCallCurveName() + "_" + callCurrency.getCode();
    final Object putJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(putCurrency, getPutCurveName(), getPutCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (putJacobianObject == null) {
      throw new OpenGammaRuntimeException("Put currency curve Jacobian was null");
    }
    final Object receiveJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(callCurrency, getCallCurveName(), getCallCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (receiveJacobianObject == null) {
      throw new OpenGammaRuntimeException("Call currency curve Jacobian was null");
    }
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target, getPutCurveName(), getCallCurveName()));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Curve sensitivities were null");
    }
    final Object putCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, getPutCurveName(), getPutCurveName()));
    if (putCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Put curve coupon sensitivities were null");
    }
    final Object receiveCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, getCallCurveName(), getCallCurveName()));
    if (receiveCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Call curve coupon sensitivities were null");
    }
    final Object putCurveSpecObject = inputs.getValue(getPutCurveSpecRequirement(putCurrency, getPutCurveName()));
    if (putCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Put curve specification was null");
    }
    final Object callCurveSpecObject = inputs.getValue(getCallCurveSpecRequirement(callCurrency, getCallCurveName()));
    if (callCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Call curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities putCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) putCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities callCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) callCurveSpecObject;
    final DoubleMatrix2D putJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(putJacobianObject));
    final DoubleMatrix2D receiveJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(receiveJacobianObject));
    final YieldAndDiscountCurve putCurve = data.getCurve(putCurveName);
    final YieldAndDiscountCurve receiveCurve = data.getCurve(callCurveName);
    final Map<String, DoubleMatrix2D> jacobians = new HashMap<String, DoubleMatrix2D>();
    jacobians.put(putCurveName, putJacobian);
    jacobians.put(callCurveName, receiveJacobian);
    final Map<String, DoubleMatrix1D> couponSensitivities = new HashMap<String, DoubleMatrix1D>();
    couponSensitivities.put(putCurveName, (DoubleMatrix1D) putCouponSensitivitiesObject);
    couponSensitivities.put(callCurveName, (DoubleMatrix1D) receiveCouponSensitivitiesObject);
    final Map<String, YieldAndDiscountCurve> interpolatedCurves = new HashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(putCurveName, putCurve);
    interpolatedCurves.put(callCurveName, receiveCurve);
    final Map<String, List<DoublesPair>> curveSensitivities = ((PresentValueSensitivity) curveSensitivitiesObject).getSensitivities();
    final Map<String, DoubleMatrix1D> result = CALCULATOR.calculate(fx, curveSensitivities, interpolatedCurves, couponSensitivities, jacobians);
    final DoubleLabelledMatrix1D putResult = getSensitivitiesForCurve(target, getPutCurveName(), putCurveName, data, result.get(putCurveName), putCurrency, putCurveSpec);
    final DoubleLabelledMatrix1D receiveResult = getSensitivitiesForCurve(target, getCallCurveName(), callCurveName, data, result.get(callCurveName), callCurrency, callCurveSpec);
    return Sets.newHashSet(new ComputedValue(getPutSpec(target, putCurrency.getCode()), putResult),
                           new ComputedValue(getCallSpec(target, callCurrency.getCode()), receiveResult));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final String putCurveName = getPutCurveName();
    final String callCurveName = getCallCurveName();
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    final ExternalId spotIdentifier = FXUtils.getSpotIdentifier(fxOption, true);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, getSurfaceName())
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    final ValueRequirement fxVolatilitySurface = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    result.add(YieldCurveFunction.getCurveRequirement(putCurrency, putCurveName, putCurveName, putCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(callCurrency, callCurveName, callCurveName, callCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(putCurrency, putCurveName, putCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(callCurrency, callCurveName, callCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(putCurrency, putCurveName, putCurveName));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(callCurrency, callCurveName, callCurveName));
    result.add(getCurveSensitivitiesRequirement(target, putCurveName, callCurveName));
    result.add(spotRequirement);
    result.add(fxVolatilitySurface);
    result.add(getPutCurveSpecRequirement(putCurrency, putCurveName));
    result.add(getCallCurveSpecRequirement(callCurrency, callCurveName));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency receiveCurrency = fxOption.getCallCurrency();
    return Sets.newHashSet(getPutSpec(target, putCurrency.getCode()), getCallSpec(target, receiveCurrency.getCode()));
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target, final String putCurveName, final String receiveCurveName) {
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, putCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).get());
  }

  private ValueRequirement getPutCurveSpecRequirement(final Currency currency, final String putCurveName) {
    final ValueRequirement putCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, putCurveName).get());
    return putCurveRequirement;
  }

  private ValueRequirement getCallCurveSpecRequirement(final Currency currency, final String callCurveName) {
    final ValueRequirement callCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, callCurveName).get());
    return callCurveRequirement;
  }

  private ValueSpecification getPutSpec(final ComputationTarget target, final String putCurrency) {
    final ValueProperties putCurveProperties = createValueProperties()
        .with(ValuePropertyNames.CURVE, getPutCurveName())
        .with(ValuePropertyNames.CURVE_CURRENCY, putCurrency)
        .with(ValuePropertyNames.CURRENCY, putCurrency)
        .get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), putCurveProperties);
  }

  private ValueSpecification getCallSpec(final ComputationTarget target, final String receiveCurrency) {
    final ValueProperties receiveCurveProperties = createValueProperties()
        .with(ValuePropertyNames.CURVE, getCallCurveName())
        .with(ValuePropertyNames.CURVE_CURRENCY, receiveCurrency)
        .with(ValuePropertyNames.CURRENCY, receiveCurrency)
        .get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), receiveCurveProperties);
  }

  private DoubleLabelledMatrix1D getSensitivitiesForCurve(final ComputationTarget target, final String curveDefinitionName, final String curveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivities, final Currency currency, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec) {
    final int n = sensitivities.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec, currency, curveDefinitionName);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivities.getEntry(i));
    }
    return labelledMatrix;
  }
}
