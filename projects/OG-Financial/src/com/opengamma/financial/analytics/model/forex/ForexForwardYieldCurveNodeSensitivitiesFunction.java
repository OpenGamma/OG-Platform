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
import com.opengamma.financial.forex.calculator.PresentValueForexYieldCurveNodeSensitivityCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends ForexForwardFunction {
  private static final PresentValueForexYieldCurveNodeSensitivityCalculator CALCULATOR = PresentValueForexYieldCurveNodeSensitivityCalculator.getInstance();

  public ForexForwardYieldCurveNodeSensitivitiesFunction(final String payCurveName, final String receiveCurveName) {
    super(payCurveName, receiveCurveName, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final Currency payCurrency = fxForward.getCurrency1();
    final Currency receiveCurrency = fxForward.getCurrency2();
    final String payCurveName = getPayCurveName() + "_" + payCurrency.getCode();
    final String receiveCurveName = getReceiveCurveName() + "_" + receiveCurrency.getCode();
    final Object payJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(payCurrency, getPayCurveName(), getPayCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (payJacobianObject == null) {
      throw new OpenGammaRuntimeException("Pay currency curve Jacobian was null");
    }
    final Object receiveJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(receiveCurrency, getReceiveCurveName(), getReceiveCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (receiveJacobianObject == null) {
      throw new OpenGammaRuntimeException("Receive currency curve Jacobian was null");
    }
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target, getPayCurveName(), getReceiveCurveName()));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Curve sensitivities were null");
    }
    final Object payCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, getPayCurveName(), getPayCurveName()));
    if (payCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Pay curve coupon sensitivities were null");
    }
    final Object receiveCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, getReceiveCurveName(), getReceiveCurveName()));
    if (receiveCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Receive curve coupon sensitivities were null");
    }
    final Object payCurveSpecObject = inputs.getValue(getPayCurveSpecRequirement(payCurrency, getPayCurveName()));
    if (payCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Pay curve specification was null");
    }
    final Object receiveCurveSpecObject = inputs.getValue(getReceiveCurveSpecRequirement(receiveCurrency, getReceiveCurveName()));
    if (receiveCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Receive curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities payCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) payCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities receiveCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) receiveCurveSpecObject;
    final DoubleMatrix2D payJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(payJacobianObject));
    final DoubleMatrix2D receiveJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(receiveJacobianObject));
    final YieldAndDiscountCurve payCurve = data.getCurve(payCurveName);
    final YieldAndDiscountCurve receiveCurve = data.getCurve(receiveCurveName);
    final Map<String, DoubleMatrix2D> jacobians = new HashMap<String, DoubleMatrix2D>();
    jacobians.put(payCurveName, payJacobian);
    jacobians.put(receiveCurveName, receiveJacobian);
    final Map<String, DoubleMatrix1D> couponSensitivities = new HashMap<String, DoubleMatrix1D>();
    couponSensitivities.put(payCurveName, (DoubleMatrix1D) payCouponSensitivitiesObject);
    couponSensitivities.put(receiveCurveName, (DoubleMatrix1D) receiveCouponSensitivitiesObject);
    final Map<String, YieldAndDiscountCurve> interpolatedCurves = new HashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(payCurveName, payCurve);
    interpolatedCurves.put(receiveCurveName, receiveCurve);
    final Map<String, List<DoublesPair>> curveSensitivities = ((PresentValueSensitivity) curveSensitivitiesObject).getSensitivities();
    final Map<String, DoubleMatrix1D> result = CALCULATOR.calculate(fxForward, curveSensitivities, interpolatedCurves, couponSensitivities, jacobians);
    final DoubleLabelledMatrix1D payResult = getSensitivitiesForCurve(target, getPayCurveName(), payCurveName, data, result.get(payCurveName), payCurrency, payCurveSpec);
    final DoubleLabelledMatrix1D receiveResult = getSensitivitiesForCurve(target, getReceiveCurveName(), receiveCurveName, data, result.get(receiveCurveName), receiveCurrency, receiveCurveSpec);
    return Sets.newHashSet(new ComputedValue(getPaySpec(target, payCurrency.getCode()), payResult),
                           new ComputedValue(getReceiveSpec(target, receiveCurrency.getCode()), receiveResult));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) getSecuritySource().getSecurity(IdentifierBundle.of(fxForward.getUnderlyingIdentifier()));
    final String payCurveName = getPayCurveName();
    final String receiveCurveName = getReceiveCurveName();
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payCurveName, payCurveName, payCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveCurveName, receiveCurveName, receiveCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(payCurrency, payCurveName, payCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(receiveCurrency, receiveCurveName, receiveCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, payCurveName, payCurveName));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, receiveCurveName, receiveCurveName));
    result.add(getCurveSensitivitiesRequirement(target, payCurveName, receiveCurveName));
    result.add(getPayCurveSpecRequirement(payCurrency, payCurveName));
    result.add(getReceiveCurveSpecRequirement(receiveCurrency, receiveCurveName));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) getSecuritySource().getSecurity(IdentifierBundle.of(fxForward.getUnderlyingIdentifier()));
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    return Sets.newHashSet(getPaySpec(target, payCurrency.getCode()), getReceiveSpec(target, receiveCurrency.getCode()));
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target, final String payCurveName, final String receiveCurveName) {
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).get());
  }

  private ValueSpecification getPaySpec(final ComputationTarget target, final String payCurrency) {
    final ValueProperties payCurveProperties = createValueProperties()
        .with(ValuePropertyNames.CURVE, getPayCurveName())
        .with(ValuePropertyNames.CURVE_CURRENCY, payCurrency)
        .with(ValuePropertyNames.CURRENCY, payCurrency)
        .get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), payCurveProperties);
  }

  private ValueSpecification getReceiveSpec(final ComputationTarget target, final String receiveCurrency) {
    final ValueProperties receiveCurveProperties = createValueProperties()
        .with(ValuePropertyNames.CURVE, getReceiveCurveName())
        .with(ValuePropertyNames.CURVE_CURRENCY, receiveCurrency)
        .with(ValuePropertyNames.CURRENCY, receiveCurrency)
        .get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), receiveCurveProperties);
  }

  private ValueRequirement getPayCurveSpecRequirement(final Currency currency, final String payCurveName) {
    final ValueRequirement payCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, payCurveName).get());
    return payCurveRequirement;
  }

  private ValueRequirement getReceiveCurveSpecRequirement(final Currency currency, final String receiveCurveName) {
    final ValueRequirement receiveCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, receiveCurveName).get());
    return receiveCurveRequirement;
  }

  private DoubleLabelledMatrix1D getSensitivitiesForCurve(final ComputationTarget target, final String curveDefinitionName, final String curveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivities, final Currency currency, final InterpolatedYieldCurveSpecificationWithSecurities spec) {
    final int n = sensitivities.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(spec, currency, curveDefinitionName);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivities.getEntry(i));
    }
    return labelledMatrix;
  }
}
