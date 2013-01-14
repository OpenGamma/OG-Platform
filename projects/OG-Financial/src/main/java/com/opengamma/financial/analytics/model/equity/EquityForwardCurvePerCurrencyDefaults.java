/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class EquityForwardCurvePerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurvePerCurrencyDefaults.class);
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from currency to curve configuration, curve name and currency */
  private final Map<String, Pair<String, String>> _perCurrencyConfig;

  /**
   * @param priority The priority, not null
   * @param perCurrencyConfig The default values per currency, not null
   */
  public EquityForwardCurvePerCurrencyDefaults(final String priority, final String... perCurrencyConfig) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyConfig, "per equity config");
    final int nPairs = perCurrencyConfig.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _priority = PriorityClass.valueOf(priority);
    _perCurrencyConfig = new HashMap<>();
    for (int i = 0; i < perCurrencyConfig.length; i += 3) {
      final Pair<String, String> config = Pair.of(perCurrencyConfig[i + 1], perCurrencyConfig[i + 2]);
      _perCurrencyConfig.put(perCurrencyConfig[i].toUpperCase(), config);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId id = target.getUniqueId();
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String currency = EquitySecurityUtils.getCurrency(securitySource, id);
    if (currency == null) {
      return false;
    }
    return _perCurrencyConfig.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE_CURRENCY);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String currency = EquitySecurityUtils.getCurrency(securitySource, target.getUniqueId());
    if (currency == null) {
      s_logger.error("Could not get currency for {}; should never happen", target.getUniqueId());
      return null;
    }
    final Pair<String, String> config = _perCurrencyConfig.get(currency);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(config.getFirst());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(config.getSecond());
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(currency);
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_FORWARD_CURVE_DEFAULTS;
  }

}
