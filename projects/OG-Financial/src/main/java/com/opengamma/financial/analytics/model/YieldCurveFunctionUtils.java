/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;

/**
 *
 */
public class YieldCurveFunctionUtils {

  public static Set<ValueRequirement> getCurveRequirements(final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final ComputationTargetSpecification target = exogenousConfig.getTarget();
        final String curveCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String exogenousCurveName : entry.getValue()) {
          requirements.add(getCurveRequirement(target, exogenousCurveName, exogenousConfigName, curveCalculationMethod));
        }
        requirements.addAll(getCurveRequirements(exogenousConfig, configSource));
      }
    }
    final String[] yieldCurveNames = curveConfig.getYieldCurveNames();
    final String curveCalculationConfigName = curveConfig.getCalculationConfigName();
    final String curveCalculationMethod = curveConfig.getCalculationMethod();
    final ComputationTargetSpecification target = curveConfig.getTarget();
    for (final String yieldCurveName : yieldCurveNames) {
      requirements.add(getCurveRequirement(target, yieldCurveName, curveCalculationConfigName, curveCalculationMethod));
    }
    return requirements;
  }

  public static ValueRequirement getCurveRequirement(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, properties);
  }

  public static ValueRequirement getCurveRequirement(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, properties);
  }

  public static ValueRequirement getCurveRequirement(final ComputationTargetSpecification target) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target);
  }

  //TODO won't work if curves have different currencies
  public static YieldCurveBundle getAllYieldCurves(final FunctionInputs inputs, final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final YieldCurveBundle curves = new YieldCurveBundle();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final ComputationTargetSpecification target = exogenousConfig.getTarget();
        final String exogenousCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String curveName : entry.getValue()) {
          final ValueRequirement curveRequirement = YieldCurveFunctionUtils.getCurveRequirement(target, curveName, exogenousConfigName, exogenousCalculationMethod);
          final Object curveObject = inputs.getValue(curveRequirement);
          if (curveObject == null) {
            throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
          }
          final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
          curves.setCurve(curveName, curve);
        }
        curves.addAll(getAllYieldCurves(inputs, exogenousConfig, configSource));
      }
    }
    final String[] curveNames = curveConfig.getYieldCurveNames();
    final ComputationTargetSpecification target = curveConfig.getTarget();
    for (final String curveName : curveNames) {
      final ValueRequirement curveRequirement = YieldCurveFunctionUtils.getCurveRequirement(target, curveName, curveConfig.getCalculationConfigName(), curveConfig.getCalculationMethod());
      final Object curveObject = inputs.getValue(curveRequirement);
      if (curveObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
      }
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
      curves.setCurve(curveName, curve);
    }
    return curves;
  }

  //TODO won't work if curves have different currencies
  public static YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final MultiCurveCalculationConfig curveConfig) {
    final YieldCurveBundle curves = new YieldCurveBundle();
    final String[] curveNames = curveConfig.getYieldCurveNames();
    final ComputationTargetSpecification target = curveConfig.getTarget();
    for (final String curveName : curveNames) {
      final ValueRequirement curveRequirement = YieldCurveFunctionUtils.getCurveRequirement(target, curveName, curveConfig.getCalculationConfigName(), curveConfig.getCalculationMethod());
      final Object curveObject = inputs.getValue(curveRequirement);
      if (curveObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
      }
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
      curves.setCurve(curveName, curve);
    }
    return curves;
  }

  //TODO won't work if curves have different currencies
  public static YieldCurveBundle getFixedCurves(final FunctionInputs inputs, final MultiCurveCalculationConfig curveConfig,
      final ConfigDBCurveCalculationConfigSource configSource) {
    final YieldCurveBundle curves = new YieldCurveBundle();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final MultiCurveCalculationConfig config = configSource.getConfig(entry.getKey());
        curves.addAll(getYieldCurves(inputs, config));
      }
      return curves;
    }
    return null;
  }
}
