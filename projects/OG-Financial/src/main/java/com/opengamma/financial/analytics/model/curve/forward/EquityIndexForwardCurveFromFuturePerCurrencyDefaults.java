/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityIndexForwardCurveFromFuturePerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityIndexForwardCurveFromFuturePerCurrencyDefaults.class);
  /** Value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE
  };
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from currency to forward curve name */
  private final Map<String, String> _forwardCurveNames;
  /** Map from currency to forward curve calculation method */
  private final Map<String, String> _forwardCurveCalculationMethods;

  /**
   * @param priority The priority, not null
   * @param perCurrencyConfig The default values of forward curve name, forward curve calculation method per currency
   */
  public EquityIndexForwardCurveFromFuturePerCurrencyDefaults(final String priority, final String... perCurrencyConfig) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyConfig, "per currency config");
    final int n = perCurrencyConfig.length;
    ArgumentChecker.isTrue(n % 3 == 0, "Must have one forward curve name and forward curve calculation method per currency");
    _priority = PriorityClass.valueOf(priority);
    _forwardCurveNames = new LinkedHashMap<>();
    _forwardCurveCalculationMethods = new LinkedHashMap<>();
    for (int i = 0; i < n; i += 3) {
      final String currency = perCurrencyConfig[i];
      _forwardCurveNames.put(currency, perCurrencyConfig[i + 1]);
      _forwardCurveCalculationMethods.put(currency, perCurrencyConfig[i + 2]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = target.getUniqueId().getValue();
    return _forwardCurveNames.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = target.getUniqueId().getValue();
    final String forwardCurveName = _forwardCurveNames.get(currency);
    if (forwardCurveName == null) {
      s_logger.error("Could not get defaults for {}; should never happen", currency);
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveNames.get(currency));
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethods.get(currency));
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.COMMODITY_FORWARD_CURVE_DEFAULTS;
  }
}
