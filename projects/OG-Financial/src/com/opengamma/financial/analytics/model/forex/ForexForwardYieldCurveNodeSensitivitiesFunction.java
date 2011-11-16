/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.HashSet;
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
import com.opengamma.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends ForexForwardFunction {
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();

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
    final Object payFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(payCurrency, getPayFundingCurveName()));
    if (payFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException(payCurrency + " funding curve " + getPayFundingCurveName() + "  specification was null");
    }
    final Object payForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(payCurrency, getPayForwardCurveName()));
    if (payForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException(payCurrency + " forward curve " + getPayForwardCurveName() + "  specification was null");
    }
    final Object receiveFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(receiveCurrency, getReceiveFundingCurveName()));
    if (receiveFundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException(receiveCurrency + " funding curve " + getReceiveFundingCurveName() + "  specification was null");
    }
    final Object receiveForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(receiveCurrency, getReceiveForwardCurveName()));
    if (receiveForwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException(receiveCurrency + " forward curve " + getReceiveForwardCurveName() + "  specification was null");
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
    final DoubleMatrix1D payCouponSensitivity = (DoubleMatrix1D) payCouponSensitivitiesObject;
    final DoubleMatrix1D receiveCouponSensitivity = (DoubleMatrix1D) receiveCouponSensitivitiesObject;
    final YieldCurveBundle payCurveBundle = new YieldCurveBundle(new String[] {payFundingCurveName, payForwardCurveName}, 
        new YieldAndDiscountCurve[] {payFundingCurve, payForwardCurve});
    final YieldCurveBundle receiveCurveBundle = new YieldCurveBundle(new String[] {receiveFundingCurveName, receiveForwardCurveName}, 
        new YieldAndDiscountCurve[] {receiveFundingCurve, receiveForwardCurve});
    final MultipleCurrencyInterestRateCurveSensitivity multipleSensitivity = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, DoubleMatrix1D> payResult, receiveResult;
    try {
      payResult = CALCULATOR.calculate(multipleSensitivity.getSensitivity(payCurrency).getSensitivities(), payCurveBundle, payCouponSensitivity, payJacobian);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + payCurrency + ", " + getPayFundingCurveName() + " and " + 
          getPayForwardCurveName() + ", error was: " + e.getMessage());
    }
    try {
      receiveResult = CALCULATOR.calculate(multipleSensitivity.getSensitivity(receiveCurrency).getSensitivities(), receiveCurveBundle, receiveCouponSensitivity, receiveJacobian);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + receiveCurrency + ", " + getReceiveFundingCurveName() + " and " + 
          getReceiveForwardCurveName() + ", error was: " + e.getMessage());
    }
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(payFundingCurveName, data, 
        payResult.get(payFundingCurveName), payFundingCurveSpec, getResultSpecForCurve(target, payCurrency.getCode(), getPayFundingCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(payForwardCurveName, data, 
        payResult.get(payForwardCurveName), payForwardCurveSpec, getResultSpecForCurve(target, payCurrency.getCode(), getPayForwardCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(receiveFundingCurveName, data, 
        receiveResult.get(receiveFundingCurveName), receiveFundingCurveSpec, getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveFundingCurveName())));
    result.addAll(YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(receiveForwardCurveName, data, 
        receiveResult.get(receiveForwardCurveName), receiveForwardCurveSpec, getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveForwardCurveName())));
    return result;
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
    result.add(getCurveSpecRequirement(payCurrency, payFundingCurveName));
    result.add(getCurveSpecRequirement(payCurrency, payForwardCurveName));
    result.add(getCurveSpecRequirement(receiveCurrency, receiveFundingCurveName));
    result.add(getCurveSpecRequirement(receiveCurrency, receiveForwardCurveName));
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
    ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName(), getPayForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName(), getReceiveForwardCurveName()).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), properties);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }
  
  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURRENCY, currency)
        .get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

}
