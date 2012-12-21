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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class EquityForwardCurveDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurveDefaults.class);
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from currency to curve configuration, curve name and currency */
  private final Map<String, Triple<String, String, String>> _perEquityConfig;

  /**
   * @param priority The priority, not null
   * @param perEquityConfig The default values per equity, not null
   */
  public EquityForwardCurveDefaults(final String priority, final String... perEquityConfig) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perEquityConfig, "per equity config");
    final int nPairs = perEquityConfig.length;
    ArgumentChecker.isTrue(nPairs % 4 == 0, "Must have one curve config, discounting curve name and currency per equity");
    _priority = PriorityClass.valueOf(priority);
    _perEquityConfig = new HashMap<String, Triple<String, String, String>>();
    for (int i = 0; i < perEquityConfig.length; i += 4) {
      final Triple<String, String, String> config = new Triple<String, String, String>(perEquityConfig[i + 1], perEquityConfig[i + 2], perEquityConfig[i + 3]);
      _perEquityConfig.put(perEquityConfig[i], config);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final String equityId = EquitySecurityUtils.getIndexOrEquityName(target.getUniqueId());
    return _perEquityConfig.containsKey(equityId);
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
    final String equityId = EquitySecurityUtils.getIndexOrEquityName(target.getUniqueId());
    if (!_perEquityConfig.containsKey(equityId)) {
      s_logger.error("Could not get config for equity " + equityId + "; should never happen");
      return null;
    }
    final Triple<String, String, String> config = _perEquityConfig.get(equityId);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(config.getFirst());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(config.getSecond());
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(config.getThird());
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

}
