/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;



/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction { //extends AbstractFunction.NonCompiledInvoker {
  //  /** The pay funding curve property */
  //  public static final String PROPERTY_PAY_FUNDING_CURVE_NAME = "PayFundingCurve";
  //  /** The pay forward curve property */
  //  public static final String PROPERTY_PAY_FORWARD_CURVE_NAME = "PayForwardCurve";
  //  /** The receive funding curve property */
  //  public static final String PROPERTY_RECEIVE_FUNDING_CURVE_NAME = "ReceiveFundingCurve";
  //  /** The receive forward curve property */
  //  public static final String PROPERTY_RECEIVE_FORWARD_CURVE_NAME = "ReceiveForwardCurve";
  //  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();
  //  private ForexSecurityConverter _visitor;
  //  private SecuritySource _securitySource;
  //
  //  @Override
  //  public void init(final FunctionCompilationContext context) {
  //    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
  //    _visitor = new ForexSecurityConverter(_securitySource);
  //  }
  //
  //  @Override
  //  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target,
  //      final String payCurveName, final String receiveCurveName) {
  //    final Currency payCurrency = fxForward.getCurrency1();
  //    final Currency receiveCurrency = fxForward.getCurrency2();
  //    final String payFundingCurveName = getPayFundingCurveName() + "_" + payCurrency.getCode();
  //    final String payForwardCurveName = getPayForwardCurveName() + "_" + payCurrency.getCode();
  //    final String receiveFundingCurveName = getReceiveFundingCurveName() + "_" + receiveCurrency.getCode();
  //    final String receiveForwardCurveName = getReceiveForwardCurveName() + "_" + receiveCurrency.getCode();
  //    final Object payJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(payCurrency, getPayForwardCurveName(), getPayFundingCurveName(),
  //        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    if (payJacobianObject == null) {
  //      throw new OpenGammaRuntimeException("Pay currency curve Jacobian was null");
  //    }
  //    final Object receiveJacobianObject = inputs.getValue(YieldCurveFunction.getJacobianRequirement(receiveCurrency, getReceiveForwardCurveName(), getReceiveFundingCurveName(),
  //        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    if (receiveJacobianObject == null) {
  //      throw new OpenGammaRuntimeException("Receive currency curve Jacobian was null");
  //    }
  //    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(target));
  //    if (curveSensitivitiesObject == null) {
  //      throw new OpenGammaRuntimeException("Curve sensitivities were null");
  //    }
  //    final Object payCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, getPayForwardCurveName(), getPayFundingCurveName()));
  //    if (payCouponSensitivitiesObject == null) {
  //      throw new OpenGammaRuntimeException("Pay curve coupon sensitivities were null");
  //    }
  //    final Object receiveCouponSensitivitiesObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, getReceiveForwardCurveName(), getReceiveFundingCurveName()));
  //    if (receiveCouponSensitivitiesObject == null) {
  //      throw new OpenGammaRuntimeException("Receive curve coupon sensitivities were null");
  //    }
  //    final Object payFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(payCurrency, getPayFundingCurveName()));
  //    if (payFundingCurveSpecObject == null) {
  //      throw new OpenGammaRuntimeException(payCurrency + " funding curve " + getPayFundingCurveName() + "  specification was null");
  //    }
  //    final Object payForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(payCurrency, getPayForwardCurveName()));
  //    if (payForwardCurveSpecObject == null) {
  //      throw new OpenGammaRuntimeException(payCurrency + " forward curve " + getPayForwardCurveName() + "  specification was null");
  //    }
  //    final Object receiveFundingCurveSpecObject = inputs.getValue(getCurveSpecRequirement(receiveCurrency, getReceiveFundingCurveName()));
  //    if (receiveFundingCurveSpecObject == null) {
  //      throw new OpenGammaRuntimeException(receiveCurrency + " funding curve " + getReceiveFundingCurveName() + "  specification was null");
  //    }
  //    final Object receiveForwardCurveSpecObject = inputs.getValue(getCurveSpecRequirement(receiveCurrency, getReceiveForwardCurveName()));
  //    if (receiveForwardCurveSpecObject == null) {
  //      throw new OpenGammaRuntimeException(receiveCurrency + " forward curve " + getReceiveForwardCurveName() + "  specification was null");
  //    }
  //    final InterpolatedYieldCurveSpecificationWithSecurities payFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) payFundingCurveSpecObject;
  //    final InterpolatedYieldCurveSpecificationWithSecurities payForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) payForwardCurveSpecObject;
  //    final InterpolatedYieldCurveSpecificationWithSecurities receiveFundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) receiveFundingCurveSpecObject;
  //    final InterpolatedYieldCurveSpecificationWithSecurities receiveForwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) receiveForwardCurveSpecObject;
  //    final DoubleMatrix2D payJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(payJacobianObject));
  //    final DoubleMatrix2D receiveJacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(receiveJacobianObject));
  //    final YieldAndDiscountCurve payFundingCurve = data.getCurve(payFundingCurveName);
  //    final YieldAndDiscountCurve payForwardCurve = data.getCurve(payForwardCurveName);
  //    final YieldAndDiscountCurve receiveFundingCurve = data.getCurve(receiveFundingCurveName);
  //    final YieldAndDiscountCurve receiveForwardCurve = data.getCurve(receiveForwardCurveName);
  //    final DoubleMatrix1D payCouponSensitivity = (DoubleMatrix1D) payCouponSensitivitiesObject;
  //    final DoubleMatrix1D receiveCouponSensitivity = (DoubleMatrix1D) receiveCouponSensitivitiesObject;
  //    final YieldCurveBundle payCurveBundle = new YieldCurveBundle(new String[] {payFundingCurveName, payForwardCurveName},
  //        new YieldAndDiscountCurve[] {payFundingCurve, payForwardCurve});
  //    final YieldCurveBundle receiveCurveBundle = new YieldCurveBundle(new String[] {receiveFundingCurveName, receiveForwardCurveName},
  //        new YieldAndDiscountCurve[] {receiveFundingCurve, receiveForwardCurve});
  //    final MultipleCurrencyInterestRateCurveSensitivity multipleSensitivity = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
  //    final Map<String, DoubleMatrix1D> payResult, receiveResult;
  //    try {
  //      payResult = CALCULATOR.calculate(multipleSensitivity.getSensitivity(payCurrency).getSensitivities(), payCurveBundle, payCouponSensitivity, payJacobian);
  //    } catch (final Exception e) {
  //      throw new OpenGammaRuntimeException("Could not get sensitivities for " + payCurrency + ", " + getPayFundingCurveName() + " and " +
  //          getPayForwardCurveName() + ", error was: " + e.getMessage());
  //    }
  //    try {
  //      receiveResult = CALCULATOR.calculate(multipleSensitivity.getSensitivity(receiveCurrency).getSensitivities(), receiveCurveBundle, receiveCouponSensitivity, receiveJacobian);
  //    } catch (final Exception e) {
  //      throw new OpenGammaRuntimeException("Could not get sensitivities for " + receiveCurrency + ", " + getReceiveFundingCurveName() + " and " +
  //          getReceiveForwardCurveName() + ", error was: " + e.getMessage());
  //    }
  //    final Set<ComputedValue> result = new HashSet<ComputedValue>();
  //    result.addAll(YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(payFundingCurveName, data,
  //        payResult.get(payFundingCurveName), payFundingCurveSpec, getResultSpecForCurve(target, payCurrency.getCode(), getPayFundingCurveName())));
  //    result.addAll(YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(payForwardCurveName, data,
  //        payResult.get(payForwardCurveName), payForwardCurveSpec, getResultSpecForCurve(target, payCurrency.getCode(), getPayForwardCurveName())));
  //    result.addAll(YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(receiveFundingCurveName, data,
  //        receiveResult.get(receiveFundingCurveName), receiveFundingCurveSpec, getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveFundingCurveName())));
  //    result.addAll(YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(receiveForwardCurveName, data,
  //        receiveResult.get(receiveForwardCurveName), receiveForwardCurveSpec, getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveForwardCurveName())));
  //    return result;
  //  }
  //
  //  @Override
  //  public ComputationTargetType getTargetType() {
  //    return ComputationTargetType.SECURITY;
  //  }
  //
  //  @Override
  //  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
  //    if (target.getType() != ComputationTargetType.SECURITY) {
  //      return false;
  //    }
  //    return target.getSecurity() instanceof FXForwardSecurity;
  //  }
  //
  //  @Override
  //  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
  //    return Collections.singleton(getResultSpec(target));
  //  }
  //
  //  @Override
  //  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
  //    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
  //    if (curveName == null) {
  //      throw new OpenGammaRuntimeException("Must specify a curve against which to calculate the node sensitivities");
  //    }
  //    final Set<String> payForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PAY_FORWARD_CURVE_NAME);
  //    if (payForwardCurveNames == null || payForwardCurveNames.size() != 1) {
  //      return null;
  //    }
  //    final Set<String> payFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PAY_FUNDING_CURVE_NAME);
  //    if (payFundingCurveNames == null || payFundingCurveNames.size() != 1) {
  //      return null;
  //    }
  //    final Set<String> receiveForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_RECEIVE_FORWARD_CURVE_NAME);
  //    if (receiveForwardCurveNames == null || receiveForwardCurveNames.size() != 1) {
  //      return null;
  //    }
  //    final Set<String> receiveFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_RECEIVE_FUNDING_CURVE_NAME);
  //    if (receiveFundingCurveNames == null || receiveFundingCurveNames.size() != 1) {
  //      return null;
  //    }
  //    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
  //    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
  //    final String payFundingCurveName = payFundingCurveNames.iterator().next();
  //    final String payForwardCurveName = payForwardCurveNames.iterator().next();
  //    final String receiveFundingCurveName = receiveFundingCurveNames.iterator().next();
  //    final String receiveForwardCurveName = receiveForwardCurveNames.iterator().next();
  //    if (!curveName.equals(payFundingCurveName) && !curveName.equals(receiveFundingCurveName)) {
  //      throw new OpenGammaRuntimeException("Asked for sensitivities to a curve (" + curveName + ") to which this FX forward is not sensitive " +
  //          "(allowed " + payFundingCurveName + " and " + receiveFundingCurveName + ")");
  //    }
  //    final Currency payCurrency = fxForward.getPayCurrency();
  //    final Currency receiveCurrency = fxForward.getReceiveCurrency();
  //    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payFundingCurveName, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payForwardCurveName, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveFundingCurveName, receiveForwardCurveName, receiveFundingCurveName,
  //        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveForwardCurveName, receiveForwardCurveName, receiveFundingCurveName,
  //        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getJacobianRequirement(payCurrency, payForwardCurveName, payFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getJacobianRequirement(receiveCurrency, receiveForwardCurveName, receiveFundingCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
  //    result.add(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, payForwardCurveName, payFundingCurveName));
  //    result.add(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, receiveForwardCurveName, receiveFundingCurveName));
  //    result.add(getCurveSensitivitiesRequirement(target));
  //    result.add(getCurveSpecRequirement(payCurrency, payFundingCurveName));
  //    result.add(getCurveSpecRequirement(payCurrency, payForwardCurveName));
  //    result.add(getCurveSpecRequirement(receiveCurrency, receiveFundingCurveName));
  //    result.add(getCurveSpecRequirement(receiveCurrency, receiveForwardCurveName));
  //    return result;
  //  }
  //  //
  //  //  @Override
  //  //  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
  //  //    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
  //  //    final Currency payCurrency = fxForward.getPayCurrency();
  //  //    final Currency receiveCurrency = fxForward.getReceiveCurrency();
  //  //    return Sets.newHashSet(getResultSpecForCurve(target, payCurrency.getCode(), getPayFundingCurveName()),
  //  //        getResultSpecForCurve(target, payCurrency.getCode(), getPayForwardCurveName()),
  //  //        getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveFundingCurveName()),
  //  //        getResultSpecForCurve(target, receiveCurrency.getCode(), getReceiveForwardCurveName()));
  //  //  }
  //
  //  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target) {
  //    final ValueProperties properties = ValueProperties.builder()
  //        .with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName(), getPayForwardCurveName())
  //        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName(), getReceiveForwardCurveName()).get();
  //    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), properties);
  //  }
  //
  //  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
  //    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
  //    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  //  }
  //
  //  private ValueSpecification getResultSpecForCurve(final ComputationTarget target, final String currency, final String curveName) {
  //    final ValueProperties properties = createValueProperties()
  //        .with(ValuePropertyNames.CURVE, curveName)
  //        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
  //        .with(ValuePropertyNames.CURRENCY, currency)
  //        .get();
  //    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  //  }
  //
  //  private ValueSpecification getResultSpec(final ComputationTarget target) {
  //    final ValueProperties properties = createValueProperties()
  //        .withAny(ValuePropertyNames.CURRENCY)
  //        .withAny(ValuePropertyNames.CURVE_CURRENCY)
  //        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
  //        .withAny(ValuePropertyNames.CURVE)
  //        .withAny(PROPERTY_PAY_FORWARD_CURVE_NAME)
  //        .withAny(PROPERTY_PAY_FUNDING_CURVE_NAME)
  //        .withAny(PROPERTY_RECEIVE_FORWARD_CURVE_NAME)
  //        .withAny(PROPERTY_RECEIVE_FUNDING_CURVE_NAME).get();
  //    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  //  }

}
