/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityVarianceSwapDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityVarianceSwapDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE
  };
  private final PriorityClass _priority;
  private final Map<String, String> _discountingCurveNames;
  private final Map<String, String> _forwardCurveNames;
  private final Map<String, String> _forwardCurveConfigNames;
  private final Map<String, String> _forwardCurveCalculationMethodNames;
  private final Map<String, String> _curveCurrencyNames;
  private final Map<String, String> _surfaceNames;

  public EquityVarianceSwapDefaults(final String priority, final String... perEquityConfig) {
    super(FinancialSecurityTypes.EQUITY_VARIANCE_SWAP_SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perEquityConfig, "per equity config");
    final int n = perEquityConfig.length;
    ArgumentChecker.isTrue(n % 7 == 0, "Must have a discounting curve name, forward curve name, forward curve calculation config, " +
        "forward curve calculation method, currency and surface name per equity");
    _priority = PriorityClass.valueOf(priority);
    _discountingCurveNames = Maps.newLinkedHashMap();
    _forwardCurveNames = Maps.newLinkedHashMap();
    _forwardCurveConfigNames = Maps.newLinkedHashMap();
    _forwardCurveCalculationMethodNames = Maps.newLinkedHashMap();
    _curveCurrencyNames = Maps.newLinkedHashMap();
    _surfaceNames = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 7) {
      final String equity = perEquityConfig[i];
      _discountingCurveNames.put(equity, perEquityConfig[i + 1]);
      _forwardCurveNames.put(equity, perEquityConfig[i + 2]);
      _forwardCurveConfigNames.put(equity, perEquityConfig[i + 3]);
      _forwardCurveCalculationMethodNames.put(equity, perEquityConfig[i + 4]);
      _curveCurrencyNames.put(equity, perEquityConfig[i + 5]);
      _surfaceNames.put(equity, perEquityConfig[i + 6]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String underlyingEquity = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(security);
    return _discountingCurveNames.containsKey(underlyingEquity);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CURRENCY);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final String underlyingEquity = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(target.getSecurity());
    if (!_discountingCurveNames.containsKey(underlyingEquity)) {
      s_logger.error("Could not get config for underlying equity " + underlyingEquity + "; should never happen");
      return null;
    }
    if (PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_discountingCurveNames.get(underlyingEquity));
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveNames.get(underlyingEquity));
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_forwardCurveConfigNames.get(underlyingEquity));
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethodNames.get(underlyingEquity));
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(_curveCurrencyNames.get(underlyingEquity));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceNames.get(underlyingEquity));
    }
    s_logger.error("Could not get default values for " + propertyName);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_VARIANCE_SWAP_DEFAULTS;
  }

}
