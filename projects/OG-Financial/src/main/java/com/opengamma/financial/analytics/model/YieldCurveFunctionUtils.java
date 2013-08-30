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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertiesUtils;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;

/**
 *
 */
public class YieldCurveFunctionUtils {

  public static Set<ValueRequirement> getCurveRequirements(final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final Set<ValueRequirement> requirements = new HashSet<>();
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

  public static Set<ValueRequirement> getCurveRequirements(final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource,
      final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
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
    final ValueProperties constraints = desiredValue.getConstraints();
    final String[] yieldCurveNames = curveConfig.getYieldCurveNames();
    final String curveCalculationConfigName;
    if (constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG) == null) {
      curveCalculationConfigName = curveConfig.getCalculationConfigName();
    } else {
      curveCalculationConfigName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
    }
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

  public static ValueRequirement getCurveRequirementForFXOption(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final String curveCalculationMethod, final boolean isPut) {
    final ValueProperties properties;
    if (isPut) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
          .with(FXOptionBlackFunction.PUT_CURVE, yieldCurveName).withOptional(FXOptionBlackFunction.PUT_CURVE)
          .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, curveCalculationConfigName).withOptional(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
          .get();
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
          .with(FXOptionBlackFunction.CALL_CURVE, yieldCurveName).withOptional(FXOptionBlackFunction.CALL_CURVE)
          .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, curveCalculationConfigName).withOptional(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
          .get();
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, properties);
  }

  public static ValueRequirement getCurveRequirementForFXOption(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final boolean isPut, final ValueProperties optionalProperties) {
    final ValueProperties properties;
    if (isPut) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(FXOptionBlackFunction.PUT_CURVE, yieldCurveName).withOptional(FXOptionBlackFunction.PUT_CURVE)
          .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, curveCalculationConfigName).withOptional(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
          .get();
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(FXOptionBlackFunction.CALL_CURVE, yieldCurveName).withOptional(FXOptionBlackFunction.CALL_CURVE)
          .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, curveCalculationConfigName).withOptional(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
          .get();
    }
    final ValueProperties allProperties = ValuePropertiesUtils.addAllOptional(properties, optionalProperties).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, allProperties);
  }

  public static ValueRequirement getCurveRequirementForFXForward(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final String curveCalculationMethod, final boolean isPay) {
    final ValueProperties properties;
    if (isPay) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
          .with(ValuePropertyNames.PAY_CURVE, yieldCurveName).withOptional(ValuePropertyNames.PAY_CURVE)
          .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .get();
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
          .with(ValuePropertyNames.RECEIVE_CURVE, yieldCurveName).withOptional(ValuePropertyNames.RECEIVE_CURVE)
          .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .get();
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, properties);
  }

  public static ValueRequirement getCurveRequirementForFXForward(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final boolean isPay) {
    final ValueProperties properties;
    if (isPay) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.PAY_CURVE, yieldCurveName).withOptional(ValuePropertyNames.PAY_CURVE)
          .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .get();
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, yieldCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .with(ValuePropertyNames.RECEIVE_CURVE, yieldCurveName).withOptional(ValuePropertyNames.RECEIVE_CURVE)
          .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .get();
    }
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
          final String fullCurveName = curveName + "_" + target.getUniqueId().getValue();
          curves.setCurve(fullCurveName, curve);
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
      final String fullCurveName = curveName + "_" + target.getUniqueId().getValue();
      curves.setCurve(fullCurveName, curve);
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
      final String fullCurveName = curveName + "_" + target.getUniqueId().getValue();
      curves.setCurve(fullCurveName, curve);
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

  public static Set<String> intersection(final Set<String> as, final String[] bs) {
    final Set<String> i = Sets.newHashSetWithExpectedSize(as.size());
    for (final String b : bs) {
      if (as.contains(b)) {
        i.add(b);
      }
    }
    return i;
  }

}
