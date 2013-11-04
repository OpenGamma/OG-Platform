/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CommodityFutureOptionBlackLognormalDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(CommodityFutureOptionBlackLognormalDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.VALUE_DELTA,
      ValueRequirementNames.VALUE_GAMMA,
      ValueRequirementNames.VALUE_THETA,
      ValueRequirementNames.VALUE_VEGA,
      ValueRequirementNames.FORWARD_DELTA,
      ValueRequirementNames.FORWARD_GAMMA,
      ValueRequirementNames.DELTA,
      ValueRequirementNames.GAMMA,
      ValueRequirementNames.VEGA,
      ValueRequirementNames.THETA
  };
  private final PriorityClass _priority;
  private final Map<String, Set<String>> _currencyToCurveName;
  private final Map<String, Set<String>> _currencyToCurveCalculationConfigName;
  private final Map<String, Set<String>> _currencyToSurfaceName;
  private final Map<String, Set<String>> _currencyToInterpolationMethod;
  private final Map<String, Set<String>> _currencyToForwardCurveName;
  private final Map<String, Set<String>> _currencyToForwardCurveCalculationMethod;

  public CommodityFutureOptionBlackLognormalDefaults(final String priority, final String... defaultsPerCurrency) {
    super(FinancialSecurityTypes.COMMODITY_FUTURE_OPTION_SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(defaultsPerCurrency, "defaults per currency");
    final int n = defaultsPerCurrency.length;
    ArgumentChecker.isTrue(n % 7 == 0, "Need one discounting curve name, discounting curve calculation config, surface name, surface interpolation method," +
        "forward curve name and forward curve calculation method per currency");
    _priority = PriorityClass.valueOf(priority);
    _currencyToCurveName = new LinkedHashMap<>();
    _currencyToCurveCalculationConfigName = new LinkedHashMap<>();
    _currencyToSurfaceName = new LinkedHashMap<>();
    _currencyToInterpolationMethod = new LinkedHashMap<>();
    _currencyToForwardCurveName = new LinkedHashMap<>();
    _currencyToForwardCurveCalculationMethod = new LinkedHashMap<>();
    for (int i = 0; i < n; i += 7) {
      final String currency = defaultsPerCurrency[i];
      _currencyToCurveName.put(currency, Collections.singleton(defaultsPerCurrency[i + 1]));
      _currencyToCurveCalculationConfigName.put(currency, Collections.singleton(defaultsPerCurrency[i + 2]));
      _currencyToSurfaceName.put(currency, Collections.singleton(defaultsPerCurrency[i + 3]));
      _currencyToInterpolationMethod.put(currency, Collections.singleton(defaultsPerCurrency[i + 4]));
      _currencyToForwardCurveName.put(currency, Collections.singleton(defaultsPerCurrency[i + 5]));
      _currencyToForwardCurveCalculationMethod.put(currency, Collections.singleton(defaultsPerCurrency[i + 6]));
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = ((CommodityFutureOptionSecurity) security).getCurrency().getCode();
    return _currencyToCurveCalculationConfigName.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME);
      defaults.addValuePropertyName(valueRequirement, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME);
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    if (!constraints.isDefined(ValuePropertyNames.CALCULATION_METHOD)) {
      return null;
    }
    final Set<String> values = constraints.getValues(ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    if ((values == null) || (!values.isEmpty() && !values.contains(BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL))) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Security security = target.getSecurity();
    final String currency = ((CommodityFutureOptionSecurity) security).getCurrency().getCode();
    switch (propertyName) {
      case EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME:
        return _currencyToCurveName.get(currency);
      case EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG:
        return _currencyToCurveCalculationConfigName.get(currency);
      case ValuePropertyNames.SURFACE:
        return _currencyToSurfaceName.get(currency);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME:
        return _currencyToForwardCurveName.get(currency);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return _currencyToForwardCurveCalculationMethod.get(currency);
      case BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR:
        return _currencyToInterpolationMethod.get(currency);
      default:
        s_logger.error("Could not find default value for {} in this function", propertyName);
        return null;
    }
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.INTERPOLATED_BLACK_LOGNORMAL_DEFAULTS;
  }

}
