/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
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
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexOptionBlackYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexOptionBlackYieldCurveNodeSensitivitiesFunction.class);
  /** The name for a property indicating whether to get the sensitivities for the put currency leg or the call currency leg */
  public static final String PROPERTY_PUT_OR_CALL_CURRENCY = "CurrencyLeg"; //TODO rename this
  /** The value indicating the put currency */
  public static final String PUT_RESULT = "Put";
  /** The value indicating the call currency */
  public static final String CALL_RESULT = "Call";
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity || target.getSecurity() instanceof FXBarrierOptionSecurity || target.getSecurity() instanceof FXDigitalOptionSecurity;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String putCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_PUT_CURVE);
    final String putForwardCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE);
    final String putCurveCalculationMethod = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD);
    final String callCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_CALL_CURVE);
    final String callForwardCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE);
    final String callCurveCalculationMethod = desiredValue.getConstraint(ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final Currency fxSensitivityCurrency = Currency.of(ForexOptionBlackSingleValuedFunction.getResultCurrency(target));
    final String leg = desiredValue.getConstraint(PROPERTY_PUT_OR_CALL_CURRENCY);
    final String calculationMethod;
    final String forwardCurveName;
    final Currency curveCurrency;
    if (leg.equals(PUT_RESULT)) {
      calculationMethod = putCurveCalculationMethod;
      forwardCurveName = putForwardCurveName;
      curveCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    } else {
      calculationMethod = callCurveCalculationMethod;
      forwardCurveName = callForwardCurveName;
      curveCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    }
    final String fullCurveName = curveName + "_" + fxSensitivityCurrency.getCode();
    final Object forwardCurveObject = inputs.getValue(getCurveRequirement(forwardCurveName, forwardCurveName, curveName, calculationMethod, curveCurrency));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve called " + forwardCurveName);
    }
    final Object curveObject = inputs.getValue(getCurveRequirement(curveName, forwardCurveName, curveName, calculationMethod, curveCurrency));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle(new String[] {fullCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {curve, forwardCurve});
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(putCurveName, putForwardCurveName, putCurveCalculationMethod,
        callCurveName, callForwardCurveName, callCurveCalculationMethod, surfaceName, target));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(curveCurrency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final InterestRateCurveSensitivity curveSensitivities = (InterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivities();
    final ValueProperties properties = getResultProperties(fxSensitivityCurrency.getCode(), curveName, putCurveName, putForwardCurveName, putCurveCalculationMethod,
        callCurveName, callForwardCurveName, callCurveCalculationMethod, surfaceName, leg);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    //TODO convert FX sensitivities if necessary
    if (calculationMethod.equals(InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME)) {
      final DoubleMatrix1D result = CALCULATOR.calculateFromSimpleInterpolatedCurve(sensitivitiesForCurrency, interpolatedCurves);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurves, result, curveSpec, spec);
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    if (calculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)) {
      final DoubleMatrix1D result = CALCULATOR.calculateFromParRate(sensitivitiesForCurrency, interpolatedCurves, jacobian);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurves, result, curveSpec, spec);
    }
    final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(forwardCurveName, curveName, curveCurrency));
    if (couponSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    }
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
    final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(sensitivitiesForCurrency, interpolatedCurves, couponSensitivity, jacobian);
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurves, result, curveSpec, spec);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties());
    return Collections.singleton(resultSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final Set<String> results = constraints.getValues(PROPERTY_PUT_OR_CALL_CURRENCY);
    if (results == null || results.size() != 1) {
      s_logger.error("Did not specify a result for requirement {}", desiredValue);
      return null;
    }
    final Set<String> putCurveNames = constraints.getValues(ForexOptionBlackFunction.PROPERTY_PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(ForexOptionBlackFunction.PROPERTY_CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final String putCurveName = putCurveNames.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.error("Did not specify a curve to which this security is sensitive; asked for {}", curveName);
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> putForwardCurveNames = constraints.getValues(ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE);
    if (putForwardCurveNames == null || putForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callForwardCurveNames = constraints.getValues(ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE);
    if (callForwardCurveNames == null || callForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationMethods = constraints.getValues(ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD);
    if (putCurveCalculationMethods == null || putCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationMethods = constraints.getValues(ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD);
    if (callCurveCalculationMethods == null || callCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String result = results.iterator().next(); //TODO test whether put or call string
    final String putForwardCurveName = putForwardCurveNames.iterator().next();
    final String callForwardCurveName = callForwardCurveNames.iterator().next();
    final String putCurveCalculationMethod = putCurveCalculationMethods.iterator().next();
    final String callCurveCalculationMethod = callCurveCalculationMethods.iterator().next();
    final String curveCalculationMethod;
    final String forwardCurveName;
    final Currency curveCurrency;
    if (result.equals(PUT_RESULT)) {
      curveCalculationMethod = putCurveCalculationMethods.iterator().next();
      forwardCurveName = putForwardCurveNames.iterator().next();
      curveCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    } else {
      curveCalculationMethod = callCurveCalculationMethods.iterator().next();
      forwardCurveName = callForwardCurveNames.iterator().next();
      curveCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    }
    final String surfaceName = surfaceNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getCurveRequirement(curveName, forwardCurveName, curveName, curveCalculationMethod, curveCurrency));
    requirements.add(getCurveRequirement(forwardCurveName, forwardCurveName, curveName, curveCalculationMethod, curveCurrency));
    requirements.add(getCurveSpecRequirement(curveCurrency, curveName));
    requirements.add(getCurveSensitivitiesRequirement(putCurveName, putForwardCurveName, putCurveCalculationMethod, callCurveName, callForwardCurveName, callCurveCalculationMethod,
        surfaceName, target));
    if (!curveCalculationMethod.equals(InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME)) {
      requirements.add(getJacobianRequirement(curveName, forwardCurveName, curveCalculationMethod, curveCurrency));
      if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        requirements.add(getCouponSensitivityRequirement(forwardCurveName, curveName, curveCurrency));
      }
    }
    return requirements;
  }

  private ValueRequirement getCurveRequirement(final String curveName, final String forwardCurveName, final String fundingCurveName,
      final String calculationMethod, final Currency currency) {
    return YieldCurveFunction.getCurveRequirement(currency, curveName, forwardCurveName, fundingCurveName, calculationMethod);
  }

  private ValueProperties getResultProperties() {
    return createValueProperties()
        .withAny(PROPERTY_PUT_OR_CALL_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .withAny(ForexOptionBlackFunction.PROPERTY_PUT_CURVE)
        .withAny(ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE)
        .withAny(ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD)
        .withAny(ForexOptionBlackFunction.PROPERTY_CALL_CURVE)
        .withAny(ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE)
        .withAny(ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SURFACE).get();
  }

  private ValueProperties getResultProperties(final String ccy, final String curveName, final String putCurveName, final String putForwardCurveName,
      final String putCurveCalculationMethod, final String callCurveName, final String callForwardCurveName, final String callCurveCalculationMethod,
      final String surfaceName, final String leg) {
    return createValueProperties()
        .with(PROPERTY_PUT_OR_CALL_CURRENCY, leg)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy)
        .with(ValuePropertyNames.CURRENCY, ccy)
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_CURVE, putCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE, putForwardCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD, putCurveCalculationMethod)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_CURVE, callCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE, callForwardCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD, callCurveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName).get();
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  private ValueRequirement getJacobianRequirement(final String curveName, final String forwardCurveName, final String curveCalculationMethod, final Currency currency) {
    return YieldCurveFunction.getJacobianRequirement(currency, forwardCurveName, curveName, curveCalculationMethod);
  }

  private ValueRequirement getCouponSensitivityRequirement(final String forwardCurveName, final String fundingCurveName, final Currency currency) {
    return YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveName, fundingCurveName);
  }

  private ValueRequirement getCurveSensitivitiesRequirement(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_CURVE, putCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE, putForwardCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD, putCurveCalculationMethod)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_CURVE, callCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE, callForwardCurveName)
        .with(ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD, callCurveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURRENCY, ForexOptionBlackSingleValuedFunction.getResultCurrency(target)).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
