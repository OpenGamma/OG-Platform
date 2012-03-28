/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;



/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();
  /** The pay funding curve property */
  public static final String PROPERTY_PAY_FUNDING_CURVE_NAME = "PayFundingCurve";
  /** The pay forward curve property */
  public static final String PROPERTY_PAY_FORWARD_CURVE_NAME = "PayForwardCurve";
  /** The receive funding curve property */
  public static final String PROPERTY_RECEIVE_FUNDING_CURVE_NAME = "ReceiveFundingCurve";
  /** The receive forward curve property */
  public static final String PROPERTY_RECEIVE_FORWARD_CURVE_NAME = "ReceiveForwardCurve";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
    final String fundingCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final String calculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD).iterator().next();
    final String currencyName = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY).iterator().next();
    final Currency currency = Currency.of(currencyName);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final ValueRequirement curveSensitivitiesRequirement = getCurveSensitivitiesRequirement(target, fundingCurveName, fundingCurveName,
        calculationMethod, calculationMethod); //TODO
    final Object curveSensitivitiesObject = inputs.getValue(curveSensitivitiesRequirement);
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSensitivitiesRequirement);
    }
    final InterestRateCurveSensitivity curveSensitivities = ((MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject).getSensitivity(currency);
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final YieldCurveBundle bundle = getYieldCurves(inputs, forwardCurveName, fundingCurveName, calculationMethod, currency);
    if (calculationMethod.equals(InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME)) {
      final DoubleMatrix1D sensitivities = CALCULATOR.calculateFromSimpleInterpolatedCurve(curveSensitivities.getSensitivities(), bundle);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, bundle, sensitivities, curveSpec,
          getResultSpec(target, currencyName, calculationMethod, curveName, forwardCurveName, fundingCurveName));
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (calculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveName, fundingCurveName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivities = CALCULATOR.calculateFromPresentValue(curveSensitivities.getSensitivities(), bundle, couponSensitivity, jacobian);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(curveSensitivities.getSensitivities(), bundle, jacobian);
    }
    final String fullCurveName = curveName + "_" + currency.getCode();
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, bundle, sensitivities, curveSpec,
        getResultSpec(target, currencyName, calculationMethod, curveName, forwardCurveName, fundingCurveName));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(getResultSpec(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Must specify a curve against which to calculate the node sensitivities");
    }
    final String curveCurrencyName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrencyName == null) {
      throw new OpenGammaRuntimeException("Must specify a curve currency");
    }
    final Currency requestedCurrency = Currency.of(curveCurrencyName);
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final Currency payCurrency = fxForward.getPayCurrency();
    final Currency receiveCurrency = fxForward.getReceiveCurrency();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    String forwardCurveName;
    String fundingCurveName;
    Currency currency = requestedCurrency;
    String curveCalculationMethod;
    final ValueProperties constraints = desiredValue.getConstraints();
    if (requestedCurrency.equals(payCurrency)) {
      final Set<String> payForwardCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      if (payForwardCurveNames == null || payForwardCurveNames.size() != 1) {
        return null;
      }
      final Set<String> payFundingCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      if (payFundingCurveNames == null || payFundingCurveNames.size() != 1) {
        return null;
      }
      final Set<String> curveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
        return null;
      }
      forwardCurveName = payForwardCurveNames.iterator().next();
      fundingCurveName = payFundingCurveNames.iterator().next();
      currency = payCurrency;
      curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    } else if (requestedCurrency.equals(receiveCurrency)) {
      final Set<String> receiveForwardCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      if (receiveForwardCurveNames == null || receiveForwardCurveNames.size() != 1) {
        return null;
      }
      final Set<String> receiveFundingCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      if (receiveFundingCurveNames == null || receiveFundingCurveNames.size() != 1) {
        return null;
      }
      final Set<String> curveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
        return null;
      }
      forwardCurveName = receiveForwardCurveNames.iterator().next();
      fundingCurveName = receiveFundingCurveNames.iterator().next();
      currency = receiveCurrency;
      curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    } else {
      throw new OpenGammaRuntimeException("Asked for a curve currency (" + requestedCurrency + ") that is neither the pay (" +
          payCurrency + ") nor receive (" + receiveCurrency + ") currency");
    }
    if (!curveName.equals(fundingCurveName)) {
      throw new OpenGammaRuntimeException("Asked for sensitivities to a curve (" + curveName + ") to which this FX forward is not sensitive " +
          "(allowed " + fundingCurveName + " for currency " + ")");
    }
    requirements.add(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(getCurveSpecRequirement(currency, curveName));
    //TODO this is not right
    requirements.add(getCurveSensitivitiesRequirement(target, fundingCurveName, fundingCurveName, curveCalculationMethod, curveCalculationMethod));
    ///////////////////////
    if (!curveCalculationMethod.equals(InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME)) {
      requirements.add(YieldCurveFunction.getJacobianRequirement(currency, forwardCurveName, fundingCurveName, curveCalculationMethod));
      if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        requirements.add(YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveName, fundingCurveName));
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currency = null;
    String curveName = null;
    String fundingCurveName = null;
    String forwardCurveName = null;
    String curveCalculationMethod = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueSpecification spec = input.getKey();
      if (spec.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        currency = spec.getTargetSpecification().getUniqueId().getValue();
        fundingCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
        forwardCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
        curveName = fundingCurveName;
        curveCalculationMethod = spec.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
        break;
      }
    }
    assert currency != null;
    assert curveName != null;
    assert fundingCurveName != null;
    assert forwardCurveName != null;
    assert curveCalculationMethod != null;
    return Collections.singleton(getResultSpec(target, currency, curveCalculationMethod, curveName, forwardCurveName, fundingCurveName));
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationMethod, final String receiveCurveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD, payCurveCalculationMethod)
        .with(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD, receiveCurveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private static YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName,
      final String calculationMethod, final Currency currency) {
    final ValueRequirement forwardCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName,
        calculationMethod);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName,
          calculationMethod);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    final String fullFundingCurveName = fundingCurveName + "_" + currency.getCode();
    final String fullForwardCurveName = forwardCurveName + "_" + currency.getCode();
    return new YieldCurveBundle(new String[] {fullFundingCurveName, fullForwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  private ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String currency, final String curveCalculationMethod,
      final String curveName, final String forwardCurveName, final String fundingCurveName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
