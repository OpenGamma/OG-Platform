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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends ForexForwardFunction {
  private static final PresentValueForexYieldCurveNodeSensitivityCalculator CALCULATOR = PresentValueForexYieldCurveNodeSensitivityCalculator.getInstance();

  public ForexForwardYieldCurveNodeSensitivitiesFunction(final String payFundingCurveName, final String payForwardCurveName, final String receiveFundingCurveName,
      final String receiveForwardCurveName) {
    super(payFundingCurveName, payForwardCurveName, receiveFundingCurveName, receiveForwardCurveName);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final Currency payCurrency = fxForward.getCurrency1();
    final Currency receiveCurrency = fxForward.getCurrency2();
    final String payFundingCurveName = getPayFundingCurveName() + "_" + payCurrency.getCode();
    final String payForwardCurveName = getPayForwardCurveName() + "_" + payCurrency.getCode();
    final String receiveFundingCurveName = getReceiveFundingCurveName() + "_" + receiveCurrency.getCode();
    final String receiveForwardCurveName = getReceiveForwardCurveName() + "_" + receiveCurrency.getCode();
    final Object payJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(payCurrency, getPayForwardCurveName(), getPayFundingCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (payJacobianObject == null) {
      throw new OpenGammaRuntimeException("Pay currency curve Jacobian was null");
    }
    final Object receiveJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(receiveCurrency, getReceiveForwardCurveName(), getReceiveFundingCurveName(),
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    if (receiveJacobianObject == null) {
      throw new OpenGammaRuntimeException("Receive currency curve Jacobian was null");
    }
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Curve sensitivities were null");
    }
    final Object payCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, getPayForwardCurveName(), getPayFundingCurveName()));
    if (payCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Pay curve coupon sensitivities were null");
    }
    final Object receiveCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, getReceiveForwardCurveName(), getReceiveFundingCurveName()));
    if (receiveCouponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Receive curve coupon sensitivities were null");
    }
    final Object payFundingCurveSpecObject = inputs.getValue(getPayCurveSpecRequirement(payCurrency, getPayFundingCurveName()));
    if (payFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Pay funding curve specification was null");
    }
    final Object payForwardCurveSpecObject = inputs.getValue(getPayCurveSpecRequirement(payCurrency, getPayForwardCurveName()));
    if (payForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Pay forward curve specification was null");
    }
    final Object receiveFundingCurveSpecObject = inputs.getValue(getReceiveCurveSpecRequirement(receiveCurrency, getReceiveFundingCurveName()));
    if (receiveFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Receive funding curve specification was null");
    }
    final Object receiveForwardCurveSpecObject = inputs.getValue(getReceiveCurveSpecRequirement(receiveCurrency, getReceiveForwardCurveName()));
    if (receiveForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Receive forward curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities payFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) payFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities payForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) payForwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities receiveFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) receiveFundingCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities receiveForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) receiveForwardCurveSpecObject;
    final DoubleMatrix2D payJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(payJacobianObject));
    final DoubleMatrix2D receiveJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(receiveJacobianObject));
    final YieldAndDiscountCurve payFundingCurve = data.getCurve(payFundingCurveName);
    final YieldAndDiscountCurve payForwardCurve = data.getCurve(payForwardCurveName);
    final YieldAndDiscountCurve receiveFundingCurve = data.getCurve(receiveFundingCurveName);
    final YieldAndDiscountCurve receiveForwardCurve = data.getCurve(receiveForwardCurveName);
    final Map<String, DoubleMatrix2D> jacobians = new HashMap<String, DoubleMatrix2D>();
    jacobians.put(payFundingCurveName, payJacobian);
    jacobians.put(receiveFundingCurveName, receiveJacobian);
    final Map<String, DoubleMatrix1D> couponSensitivities = new HashMap<String, DoubleMatrix1D>();
    couponSensitivities.put(payFundingCurveName, (DoubleMatrix1D) payCouponSensitivitiesObject);
    couponSensitivities.put(receiveFundingCurveName, (DoubleMatrix1D) receiveCouponSensitivitiesObject);
    final Map<String, YieldAndDiscountCurve> interpolatedCurves = new HashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(payFundingCurveName, payFundingCurve);
    interpolatedCurves.put(payForwardCurveName, payForwardCurve);
    interpolatedCurves.put(receiveFundingCurveName, receiveFundingCurve);
    interpolatedCurves.put(receiveForwardCurveName, receiveForwardCurve);
    final Map<String, List<DoublesPair>> curveSensitivities = ((PresentValueSensitivity) curveSensitivitiesObject).getSensitivities();
    final Map<String, DoubleMatrix1D> result = CALCULATOR.calculate(fxForward, curveSensitivities, interpolatedCurves, couponSensitivities, jacobians);
    final DoubleLabelledMatrix1D payFundingCurveResult = getSensitivitiesForCurve(target, getPayFundingCurveName(), payFundingCurveName, data, 
        result.get(payFundingCurveName), payCurrency, payFundingCurveSpec);
    final DoubleLabelledMatrix1D payForwardCurveResult = getSensitivitiesForCurve(target, getPayForwardCurveName(), payForwardCurveName, data, 
        result.get(payForwardCurveName), payCurrency, payForwardCurveSpec);
    final DoubleLabelledMatrix1D receiveFundingCurveResult = getSensitivitiesForCurve(target, getReceiveFundingCurveName(), receiveFundingCurveName, data, 
        result.get(receiveFundingCurveName), receiveCurrency, receiveFundingCurveSpec);
    final DoubleLabelledMatrix1D receiveForwardCurveResult = getSensitivitiesForCurve(target, getReceiveForwardCurveName(), receiveForwardCurveName, data, 
        result.get(receiveForwardCurveName), receiveCurrency, receiveForwardCurveSpec);
    return Sets.newHashSet(new ComputedValue(getResultSpecForCurve(target, payCurrency.getCode(), payFundingCurveName), payFundingCurveResult),
                           new ComputedValue(getResultSpecForCurve(target, payCurrency.getCode(), payForwardCurveName), payForwardCurveResult),
                           new ComputedValue(getResultSpecForCurve(target, receiveCurrency.getCode(), receiveFundingCurveName), receiveFundingCurveResult),
                           new ComputedValue(getResultSpecForCurve(target, receiveCurrency.getCode(), receiveForwardCurveName), receiveForwardCurveResult));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) getSecuritySource().getSecurity(ExternalIdBundle.of(fxForward.getUnderlyingId()));
    final String payFundingCurveName = getPayFundingCurveName();
    final String payForwardCurveName = getPayForwardCurveName();
    final String receiveFundingCurveName = getReceiveFundingCurveName();
    final String receiveForwardCurveName = getReceiveForwardCurveName();
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payFundingCurveName, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payForwardCurveName, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveFundingCurveName, receiveForwardCurveName, receiveFundingCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveForwardCurveName, receiveForwardCurveName, receiveFundingCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(payCurrency, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(receiveCurrency, receiveForwardCurveName, receiveFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, payForwardCurveName, payFundingCurveName));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, receiveForwardCurveName, receiveFundingCurveName));
    result.add(getCurveSensitivitiesRequirement(target));
    result.add(getPayCurveSpecRequirement(payCurrency, payFundingCurveName));
    result.add(getReceiveCurveSpecRequirement(receiveCurrency, receiveFundingCurveName));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) getSecuritySource().getSecurity(ExternalIdBundle.of(fxForward.getUnderlyingId()));
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    return Sets.newHashSet(getResultSpecForCurve(target, payCurrency.getCode(), getPayFundingCurveName()),
                           getResultSpecForCurve(target, payCurrency.getCode(), getPayForwardCurveName()),
                           getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveFundingCurveName()),
                           getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveForwardCurveName()));
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), 
        ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName(), getPayForwardCurveName())
                                 .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName(), getReceiveForwardCurveName()).get());
  }

  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURRENCY, currency)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
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
