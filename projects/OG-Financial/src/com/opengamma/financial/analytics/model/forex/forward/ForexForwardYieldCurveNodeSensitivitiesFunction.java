/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueYieldCurveNodeSensitivityForexCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
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
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;



/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexForwardYieldCurveNodeSensitivitiesFunction.class);
  private static final PresentValueYieldCurveNodeSensitivityForexCalculator CALCULATOR = PresentValueYieldCurveNodeSensitivityForexCalculator.getInstance();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String payForwardCurveName = desiredValue.getConstraint(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE);
    final String payCurveCalculationMethod = desiredValue.getConstraint(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String receiveForwardCurveName = desiredValue.getConstraint(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE);
    final String receiveCurveCalculationMethod = desiredValue.getConstraint(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final String calculationMethod;
    final String forwardCurveName;
    final Currency curveCurrency;
    final Currency foreignCurrency;
    if (currency.equals(security.getPayCurrency().getCode())) {
      calculationMethod = payCurveCalculationMethod;
      forwardCurveName = payForwardCurveName;
      curveCurrency = Currency.of(currency);
      foreignCurrency = security.getReceiveCurrency();
    } else {
      calculationMethod = receiveCurveCalculationMethod;
      forwardCurveName = receiveForwardCurveName;
      curveCurrency = Currency.of(currency);
      foreignCurrency = security.getPayCurrency();
    }
    final String fullCurveName = curveName + "_" + curveCurrency;
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
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle(new String[] {fullCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {curve, forwardCurve });
    final Object curveSensitivitiesObject = inputs.getValue(getCurveSensitivitiesRequirement(payCurveName, payForwardCurveName, payCurveCalculationMethod,
        receiveCurveName, receiveForwardCurveName, receiveCurveCalculationMethod, target));
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(curveCurrency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final ValueRequirement spotRequirement = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final Object spotFXObject = inputs.getValue(spotRequirement);
    if (spotFXObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    final double spotFX = (Double) spotFXObject;
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = getSensitivitiesForCurve(curveSensitivities, curveCurrency, foreignCurrency, spotFX);
    final ValueProperties properties = getResultProperties(curveCurrency.getCode(), curveName, payCurveName, payForwardCurveName, payCurveCalculationMethod,
        receiveCurveName, receiveForwardCurveName, receiveCurveCalculationMethod);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    return getResult(inputs, curveName, calculationMethod, forwardCurveName, curveCurrency, fullCurveName, interpolatedCurves, curveSpec, sensitivitiesForCurrency, spec);
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
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties());
    return Collections.singleton(resultSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final Set<String> currencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      s_logger.error("Did not specify a currency for requirement {}", desiredValue);
      return null;
    }
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final String payCurveName = payCurveNames.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    if (!(curveName.equals(payCurveName) || curveName.equals(receiveCurveName))) {
      s_logger.error("Did not specify a curve to which this security is sensitive; asked for {}", curveName);
      return null;
    }
    final Set<String> payForwardCurveNames = constraints.getValues(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE);
    if (payForwardCurveNames == null || payForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveForwardCurveNames = constraints.getValues(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE);
    if (receiveForwardCurveNames == null || receiveForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationMethods = constraints.getValues(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD);
    if (payCurveCalculationMethods == null || payCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationMethods = constraints.getValues(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    if (receiveCurveCalculationMethods == null || receiveCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String payForwardCurveName = payForwardCurveNames.iterator().next();
    final String receiveForwardCurveName = receiveForwardCurveNames.iterator().next();
    final String payCurveCalculationMethod = payCurveCalculationMethods.iterator().next();
    final String receiveCurveCalculationMethod = receiveCurveCalculationMethods.iterator().next();
    final Currency payCurrency = security.getPayCurrency();
    final String curveCalculationMethod;
    final String forwardCurveName;
    final Currency currency = Currency.of(currencies.iterator().next());
    if (currency.equals(payCurrency)) {
      curveCalculationMethod = payCurveCalculationMethods.iterator().next();
      forwardCurveName = payForwardCurveNames.iterator().next();
    } else {
      curveCalculationMethod = receiveCurveCalculationMethods.iterator().next();
      forwardCurveName = receiveForwardCurveNames.iterator().next();
    }
    final ValueRequirement spotRequirement = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(spotRequirement);
    requirements.add(getCurveRequirement(curveName, forwardCurveName, curveName, curveCalculationMethod, currency));
    requirements.add(getCurveRequirement(forwardCurveName, forwardCurveName, curveName, curveCalculationMethod, currency));
    requirements.add(getCurveSpecRequirement(currency, curveName));
    requirements.add(getCurveSensitivitiesRequirement(payCurveName, payForwardCurveName, payCurveCalculationMethod, receiveCurveName, receiveForwardCurveName,
        receiveCurveCalculationMethod, target));
    if (!curveCalculationMethod.equals(InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME)) {
      requirements.add(getJacobianRequirement(curveName, forwardCurveName, curveCalculationMethod, currency));
      if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        requirements.add(getCouponSensitivityRequirement(forwardCurveName, curveName, currency));
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currency = null;
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
        final ValueProperties constraints = key.getProperties();
        currency = key.getTargetSpecification().getUniqueId().getValue();
        curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
        break;
      }
    }
    assert currency != null;
    assert curveName != null;
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(),
        getResultProperties(currency, curveName));
    return Collections.singleton(resultSpec);
  }

  private ValueProperties getResultProperties() {
    return createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD).get();
  }

  private ValueProperties getResultProperties(final String currency, final String curveName) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE)
        .withAny(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD).get();
  }

  private ValueProperties getResultProperties(final String currency, final String curveName, final String payCurveName, final String payForwardCurveName,
      final String payCurveCalculationMethod, final String receiveCurveName, final String receiveForwardCurveName, final String receiveCurveCalculationMethod) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE, payForwardCurveName)
        .with(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD, payCurveCalculationMethod)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE, receiveForwardCurveName)
        .with(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD, receiveCurveCalculationMethod).get();
  }

  private ValueRequirement getCurveRequirement(final String curveName, final String forwardCurveName, final String fundingCurveName,
      final String calculationMethod, final Currency currency) {
    return YieldCurveFunction.getCurveRequirement(currency, curveName, forwardCurveName, fundingCurveName, calculationMethod);
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

  private ValueRequirement getCurveSensitivitiesRequirement(final String payCurveName, final String payForwardCurveName, final String payCurveCalculationMethod,
      final String receiveCurveName, final String receiveForwardCurveName, final String receiveCurveCalculationMethod, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE, payForwardCurveName)
        .with(ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD, payCurveCalculationMethod)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE, receiveForwardCurveName)
        .with(ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD, receiveCurveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private Map<String, List<DoublesPair>> getSensitivitiesForCurve(final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities, final Currency curveCurrency,
      final Currency foreignCurrency, final double spotFX) {
    final Currency sensitivityCurrency = curveSensitivities.getCurrencies().iterator().next();
    if (curveCurrency.equals(sensitivityCurrency)) {
      return curveSensitivities.getSensitivity(curveCurrency).getSensitivities();
    }
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    double conversionFX;
    if (FXUtils.isInBaseQuoteOrder(curveCurrency, foreignCurrency)) {
      conversionFX = 1. / spotFX;
    } else {
      conversionFX = spotFX;
    }
    for (final Map.Entry<String, List<DoublesPair>> entry : curveSensitivities.getSensitivity(sensitivityCurrency).getSensitivities().entrySet()) {
      final List<DoublesPair> convertedSensitivities = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : entry.getValue()) {
        final DoublesPair convertedPair = DoublesPair.of(pair.first, pair.second * conversionFX);
        convertedSensitivities.add(convertedPair);
      }
      result.put(entry.getKey(), convertedSensitivities);
    }
    return result;
  }

  private Set<ComputedValue> getResult(final FunctionInputs inputs, final String curveName, final String calculationMethod, final String forwardCurveName, final Currency curveCurrency,
      final String fullCurveName, final YieldCurveBundle interpolatedCurves, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
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
}
