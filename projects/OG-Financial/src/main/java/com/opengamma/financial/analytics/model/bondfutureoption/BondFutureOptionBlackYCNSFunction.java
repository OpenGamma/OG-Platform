/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BondFutureOptionBlackYCNSFunction extends BondFutureOptionBlackCurveSpecificFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionBlackYCNSFunction.class);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivityBlackCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  public BondFutureOptionBlackYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, final MultiCurveCalculationConfig curveCalculationConfig,
      final ValueSpecification spec, final FunctionInputs inputs, final Set<ValueRequirement> desiredValues, final BondFutureOptionSecurity security) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final YieldCurveWithBlackCubeBundle fixedData = null; //TODO deal with fixed curves
    final Object curveSpecObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve specification for " + curveName);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivitiesObject = inputs.getValue(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      if (couponSensitivitiesObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = new DoubleMatrix1D(FunctionUtils.decodeCouponSensitivities(couponSensitivitiesObject));
      sensitivities = CALCULATOR.calculateFromPresentValue(bondFutureOption, fixedData, data, couponSensitivity, jacobian, NSC);
    }
    sensitivities = CALCULATOR.calculateFromParRate(bondFutureOption, fixedData, data, jacobian, NSC);
    final String fullCurveName = curveName + "_" + FinancialSecurityUtils.getCurrency(security).getCode();
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, data, sensitivities, curveSpec, spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Must specify a curve name");
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfigSource().getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final String[] yieldCurveNames = curveCalculationConfig.getYieldCurveNames();
    final String curve = curveNames.iterator().next();
    if (Arrays.binarySearch(yieldCurveNames, curve) < 0) {
      s_logger.error("Curve named {} is not available in curve calculation configuration called {}", curve, curveCalculationConfigName);
      return null;
    }
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final Trade trade = target.getTrade();
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    requirements.add(getCurveSpecRequirement(currency, curve));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    final Set<ValueRequirement> tsRequirements = getDataConverter().getConversionTimeSeriesRequirements(trade.getSecurity(), getTradeConverter().convert(trade));
    if (tsRequirements == null) {
      return null;
    }
    requirements.addAll(tsRequirements);
    return requirements;
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency), properties);
  }
}
